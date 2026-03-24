package com.seu.emotionhub.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seu.emotionhub.common.enums.ErrorCode;
import com.seu.emotionhub.common.exception.BusinessException;
import com.seu.emotionhub.dao.mapper.AdminOperationLogMapper;
import com.seu.emotionhub.dao.mapper.CommentMapper;
import com.seu.emotionhub.dao.mapper.PostMapper;
import com.seu.emotionhub.dao.mapper.ReportMapper;
import com.seu.emotionhub.dao.mapper.UserMapper;
import com.seu.emotionhub.model.dto.request.ReportHandleRequest;
import com.seu.emotionhub.model.dto.response.AdminDashboardOverviewVO;
import com.seu.emotionhub.model.dto.response.AdminOperationLogVO;
import com.seu.emotionhub.model.dto.response.AdminReportVO;
import com.seu.emotionhub.model.dto.response.PageResult;
import com.seu.emotionhub.model.dto.response.PostVO;
import com.seu.emotionhub.model.dto.response.UserInfoVO;
import com.seu.emotionhub.model.entity.AdminOperationLog;
import com.seu.emotionhub.model.entity.Comment;
import com.seu.emotionhub.model.entity.Post;
import com.seu.emotionhub.model.entity.Report;
import com.seu.emotionhub.model.entity.User;
import com.seu.emotionhub.model.enums.PostStatus;
import com.seu.emotionhub.model.enums.ReportStatus;
import com.seu.emotionhub.model.enums.TargetType;
import com.seu.emotionhub.model.enums.UserStatus;
import com.seu.emotionhub.service.AdminService;
import com.seu.emotionhub.service.InteractionService;
import com.seu.emotionhub.service.cache.CacheService;
import com.seu.emotionhub.service.cache.HotPostCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理后台服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private static final String TARGET_USER = "USER";
    private static final String TARGET_POST = "POST";
    private static final String TARGET_COMMENT = "COMMENT";
    private static final String TARGET_REPORT = "REPORT";

    private static final String ACTION_BAN_USER = "BAN_USER";
    private static final String ACTION_UNBAN_USER = "UNBAN_USER";
    private static final String ACTION_HIDE_POST = "HIDE_POST";
    private static final String ACTION_RESTORE_POST = "RESTORE_POST";
    private static final String ACTION_DELETE_COMMENT = "DELETE_COMMENT";
    private static final String ACTION_HANDLE_REPORT = "HANDLE_REPORT";

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final ReportMapper reportMapper;
    private final AdminOperationLogMapper adminOperationLogMapper;
    private final InteractionService interactionService;
    private final CacheService cacheService;
    private final HotPostCacheService hotPostCacheService;

    @Override
    public PageResult<UserInfoVO> listUsers(Integer page, Integer size, String keyword, String status) {
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            String trimmedKeyword = keyword.trim();
            query.and(wrapper -> wrapper
                    .like(User::getUsername, trimmedKeyword)
                    .or()
                    .like(User::getNickname, trimmedKeyword)
                    .or()
                    .like(User::getEmail, trimmedKeyword));
        }

        if (StringUtils.hasText(status)) {
            query.eq(User::getStatus, normalizeUpper(status));
        }

        query.orderByDesc(User::getCreatedAt);

        Page<User> userPage = userMapper.selectPage(new Page<>(page, size), query);
        List<UserInfoVO> records = userPage.getRecords().stream()
                .map(this::convertToUserInfoVO)
                .collect(Collectors.toList());

        return new PageResult<>(records, userPage.getTotal(), page, size);
    }

    @Override
    public UserInfoVO getUserDetail(Long userId) {
        return convertToUserInfoVO(requireUser(userId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(Long userId, String status) {
        String targetStatus = normalizeUpper(status);
        if (!UserStatus.ACTIVE.getCode().equals(targetStatus) && !UserStatus.BANNED.getCode().equals(targetStatus)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的用户状态");
        }

        Long operatorId = getCurrentUserId();
        User user = requireUser(userId);

        if (operatorId.equals(userId) && UserStatus.BANNED.getCode().equals(targetStatus)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "管理员不能封禁自己");
        }

        String beforeState = "status=" + user.getStatus();
        user.setStatus(targetStatus);
        if (userMapper.updateById(user) == 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新用户状态失败");
        }

        cacheService.delete(CacheService.CacheKey.USER_INFO + userId);
        recordOperation(
                UserStatus.BANNED.getCode().equals(targetStatus) ? ACTION_BAN_USER : ACTION_UNBAN_USER,
                TARGET_USER,
                userId,
                beforeState,
                "status=" + targetStatus,
                null
        );
    }

    @Override
    public PageResult<PostVO> listPosts(Integer page, Integer size, String keyword, String status, Long userId) {
        LambdaQueryWrapper<Post> query = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(status)) {
            query.eq(Post::getStatus, normalizeUpper(status));
        }

        if (userId != null) {
            query.eq(Post::getUserId, userId);
        }

        if (StringUtils.hasText(keyword)) {
            String trimmedKeyword = keyword.trim();
            Set<Long> matchedUserIds = findUserIdsByKeyword(trimmedKeyword);
            query.and(wrapper -> {
                wrapper.like(Post::getContent, trimmedKeyword);
                if (!matchedUserIds.isEmpty()) {
                    wrapper.or().in(Post::getUserId, matchedUserIds);
                }
            });
        }

        query.orderByDesc(Post::getCreatedAt);

        Page<Post> postPage = postMapper.selectPage(new Page<>(page, size), query);
        List<PostVO> records = postPage.getRecords().stream()
                .map(this::convertToPostVO)
                .collect(Collectors.toList());

        return new PageResult<>(records, postPage.getTotal(), page, size);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePostStatus(Long postId, String status) {
        updatePostStatusInternal(postId, status, null, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        deleteCommentInternal(commentId, null, true);
    }

    @Override
    public PageResult<AdminReportVO> listReports(Integer page, Integer size, String status, String targetType) {
        LambdaQueryWrapper<Report> query = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(status)) {
            query.eq(Report::getStatus, normalizeUpper(status));
        }

        if (StringUtils.hasText(targetType)) {
            query.eq(Report::getTargetType, normalizeUpper(targetType));
        }

        query.orderByDesc(Report::getCreatedAt);

        Page<Report> reportPage = reportMapper.selectPage(new Page<>(page, size), query);
        List<AdminReportVO> records = reportPage.getRecords().stream()
                .map(this::convertToAdminReportVO)
                .collect(Collectors.toList());

        return new PageResult<>(records, reportPage.getTotal(), page, size);
    }

    @Override
    public AdminReportVO getReportDetail(Long reportId) {
        return convertToAdminReportVO(requireReport(reportId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleReport(Long reportId, ReportHandleRequest request) {
        Report report = requireReport(reportId);
        String targetStatus = normalizeUpper(request.getStatus());
        String targetAction = StringUtils.hasText(request.getAction()) ? normalizeUpper(request.getAction()) : "NONE";

        if (!ReportStatus.isValid(targetStatus)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的举报状态");
        }

        if (!ReportStatus.PROCESSED.getCode().equals(targetStatus) && !"NONE".equals(targetAction)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "只有已处理举报才允许联动处置内容");
        }

        if (ReportStatus.PROCESSED.getCode().equals(targetStatus)) {
            if ("HIDE_POST".equals(targetAction)) {
                if (!TargetType.POST.getCode().equals(report.getTargetType())) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "当前举报目标不支持下架帖子");
                }
                updatePostStatusInternal(report.getTargetId(), PostStatus.HIDDEN.getCode(), request.getRemark(), true);
            } else if ("DELETE_COMMENT".equals(targetAction)) {
                if (!TargetType.COMMENT.getCode().equals(report.getTargetType())) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "当前举报目标不支持删除评论");
                }
                deleteCommentInternal(report.getTargetId(), request.getRemark(), true);
            } else if (!"NONE".equals(targetAction)) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的举报处理动作");
            }
        }

        String beforeState = "status=" + report.getStatus() + ",action=" + valueOrDash(report.getAction());
        Long operatorId = getCurrentUserId();

        report.setStatus(targetStatus);
        report.setAction(targetAction);
        report.setRemark(request.getRemark());
        if (ReportStatus.PENDING.getCode().equals(targetStatus)) {
            report.setHandlerId(null);
            report.setHandledAt(null);
        } else {
            report.setHandlerId(operatorId);
            report.setHandledAt(LocalDateTime.now());
        }

        if (reportMapper.updateById(report) == 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "处理举报失败");
        }

        recordOperation(
                ACTION_HANDLE_REPORT,
                TARGET_REPORT,
                reportId,
                beforeState,
                "status=" + report.getStatus() + ",action=" + valueOrDash(report.getAction()),
                request.getRemark()
        );
    }

    @Override
    public AdminDashboardOverviewVO getDashboardOverview() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        AdminDashboardOverviewVO overview = new AdminDashboardOverviewVO();
        overview.setTotalUsers(userMapper.selectCount(null));
        overview.setTodayUsers(userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getCreatedAt, todayStart)
        ));
        overview.setTotalPosts(postMapper.selectCount(
                new LambdaQueryWrapper<Post>().ne(Post::getStatus, PostStatus.DELETED.getCode())
        ));
        overview.setTodayPosts(postMapper.selectCount(
                new LambdaQueryWrapper<Post>()
                        .ne(Post::getStatus, PostStatus.DELETED.getCode())
                        .ge(Post::getCreatedAt, todayStart)
        ));
        overview.setPendingReports(reportMapper.selectCount(
                new LambdaQueryWrapper<Report>().eq(Report::getStatus, ReportStatus.PENDING.getCode())
        ));
        overview.setBannedUsers(userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getStatus, UserStatus.BANNED.getCode())
        ));
        return overview;
    }

    @Override
    public PageResult<AdminOperationLogVO> listOperationLogs(Integer page, Integer size, Long operatorId, String action) {
        LambdaQueryWrapper<AdminOperationLog> query = new LambdaQueryWrapper<>();

        if (operatorId != null) {
            query.eq(AdminOperationLog::getOperatorId, operatorId);
        }

        if (StringUtils.hasText(action)) {
            query.eq(AdminOperationLog::getAction, normalizeUpper(action));
        }

        query.orderByDesc(AdminOperationLog::getCreatedAt);

        Page<AdminOperationLog> logPage = adminOperationLogMapper.selectPage(new Page<>(page, size), query);
        List<AdminOperationLogVO> records = logPage.getRecords().stream()
                .map(this::convertToAdminOperationLogVO)
                .collect(Collectors.toList());

        return new PageResult<>(records, logPage.getTotal(), page, size);
    }

    private void updatePostStatusInternal(Long postId, String status, String remark, boolean recordLog) {
        String targetStatus = normalizeUpper(status);
        if (!PostStatus.PUBLISHED.getCode().equals(targetStatus) && !PostStatus.HIDDEN.getCode().equals(targetStatus)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的帖子状态");
        }

        Post post = requirePost(postId);
        String beforeState = "status=" + post.getStatus();
        post.setStatus(targetStatus);
        if (postMapper.updateById(post) == 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新帖子状态失败");
        }

        cacheService.delete(CacheService.CacheKey.POST_DETAIL + postId);
        hotPostCacheService.invalidatePostCache(postId);

        if (recordLog) {
            recordOperation(
                    PostStatus.HIDDEN.getCode().equals(targetStatus) ? ACTION_HIDE_POST : ACTION_RESTORE_POST,
                    TARGET_POST,
                    postId,
                    beforeState,
                    "status=" + targetStatus,
                    remark
            );
        }
    }

    private void deleteCommentInternal(Long commentId, String remark, boolean recordLog) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
        }
        if (Boolean.TRUE.equals(comment.getDeleted())) {
            throw new BusinessException(ErrorCode.COMMENT_DELETED);
        }

        interactionService.adminDeleteComment(commentId);

        if (recordLog) {
            recordOperation(
                    ACTION_DELETE_COMMENT,
                    TARGET_COMMENT,
                    commentId,
                    "deleted=false",
                    "deleted=true",
                    remark
            );
        }
    }

    private AdminReportVO convertToAdminReportVO(Report report) {
        AdminReportVO vo = new AdminReportVO();
        BeanUtils.copyProperties(report, vo);

        User reporter = userMapper.selectById(report.getReporterId());
        if (reporter != null) {
            vo.setReporterUsername(reporter.getUsername());
            vo.setReporterNickname(reporter.getNickname());
        }

        if (report.getHandlerId() != null) {
            User handler = userMapper.selectById(report.getHandlerId());
            if (handler != null) {
                vo.setHandlerNickname(handler.getNickname());
            }
        }

        vo.setTargetPreview(resolveTargetPreview(report.getTargetType(), report.getTargetId()));
        return vo;
    }

    private AdminOperationLogVO convertToAdminOperationLogVO(AdminOperationLog logRecord) {
        AdminOperationLogVO vo = new AdminOperationLogVO();
        BeanUtils.copyProperties(logRecord, vo);

        User operator = userMapper.selectById(logRecord.getOperatorId());
        if (operator != null) {
            vo.setOperatorUsername(operator.getUsername());
            vo.setOperatorNickname(operator.getNickname());
        }
        return vo;
    }

    private UserInfoVO convertToUserInfoVO(User user) {
        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    private PostVO convertToPostVO(Post post) {
        PostVO vo = new PostVO();
        BeanUtils.copyProperties(post, vo);
        if (StringUtils.hasText(post.getImages())) {
            vo.setImages(JSON.parseArray(post.getImages(), String.class));
        }

        User author = userMapper.selectById(post.getUserId());
        if (author != null) {
            vo.setUsername(author.getUsername());
            vo.setNickname(author.getNickname());
            vo.setAvatar(author.getAvatar());
        }
        return vo;
    }

    private Set<Long> findUserIdsByKeyword(String keyword) {
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.select(User::getId)
                .and(wrapper -> wrapper
                        .like(User::getUsername, keyword)
                        .or()
                        .like(User::getNickname, keyword)
                        .or()
                        .like(User::getEmail, keyword));
        List<User> users = userMapper.selectList(query);
        if (users == null || users.isEmpty()) {
            return Collections.emptySet();
        }
        return users.stream().map(User::getId).collect(Collectors.toSet());
    }

    private String resolveTargetPreview(String targetType, Long targetId) {
        if (TargetType.POST.getCode().equals(targetType)) {
            Post post = postMapper.selectById(targetId);
            return preview(post != null ? post.getContent() : null);
        }
        if (TargetType.COMMENT.getCode().equals(targetType)) {
            Comment comment = commentMapper.selectById(targetId);
            return preview(comment != null ? comment.getContent() : null);
        }
        return "-";
    }

    private String preview(String content) {
        if (!StringUtils.hasText(content)) {
            return "-";
        }
        String normalized = content.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 80) {
            return normalized;
        }
        return normalized.substring(0, 80) + "...";
    }

    private void recordOperation(String action, String targetType, Long targetId, String beforeState, String afterState, String remark) {
        AdminOperationLog logRecord = new AdminOperationLog();
        logRecord.setOperatorId(getCurrentUserId());
        logRecord.setAction(action);
        logRecord.setTargetType(targetType);
        logRecord.setTargetId(targetId);
        logRecord.setBeforeState(beforeState);
        logRecord.setAfterState(afterState);
        logRecord.setRemark(remark);
        adminOperationLogMapper.insert(logRecord);
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private Post requirePost(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }
        return post;
    }

    private Report requireReport(Long reportId) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "举报不存在");
        }
        return report;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }
        throw new BusinessException(ErrorCode.TOKEN_INVALID);
    }

    private String normalizeUpper(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String valueOrDash(String value) {
        return StringUtils.hasText(value) ? value : "-";
    }
}
