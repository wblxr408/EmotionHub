package com.seu.emotionhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seu.emotionhub.common.enums.ErrorCode;
import com.seu.emotionhub.common.exception.BusinessException;
import com.seu.emotionhub.dao.mapper.CommentMapper;
import com.seu.emotionhub.dao.mapper.PostMapper;
import com.seu.emotionhub.dao.mapper.ReportMapper;
import com.seu.emotionhub.model.dto.request.ReportCreateRequest;
import com.seu.emotionhub.model.entity.Comment;
import com.seu.emotionhub.model.entity.Post;
import com.seu.emotionhub.model.entity.Report;
import com.seu.emotionhub.model.enums.PostStatus;
import com.seu.emotionhub.model.enums.ReportStatus;
import com.seu.emotionhub.model.enums.TargetType;
import com.seu.emotionhub.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * 举报服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportMapper reportMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createReport(ReportCreateRequest request) {
        String targetType = request.getTargetType().trim().toUpperCase(Locale.ROOT);
        Long reporterId = getCurrentUserId();

        validateTarget(targetType, request.getTargetId());

        LambdaQueryWrapper<Report> duplicateQuery = new LambdaQueryWrapper<>();
        duplicateQuery.eq(Report::getReporterId, reporterId)
                .eq(Report::getTargetType, targetType)
                .eq(Report::getTargetId, request.getTargetId())
                .eq(Report::getStatus, ReportStatus.PENDING.getCode());
        if (reportMapper.selectCount(duplicateQuery) > 0) {
            throw new BusinessException(ErrorCode.DUPLICATE_OPERATION, "请勿重复举报同一内容");
        }

        Report report = new Report();
        report.setReporterId(reporterId);
        report.setTargetType(targetType);
        report.setTargetId(request.getTargetId());
        report.setReason(request.getReason().trim());
        report.setStatus(ReportStatus.PENDING.getCode());

        int rows = reportMapper.insert(report);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "提交举报失败");
        }

        log.info("举报提交成功: reporterId={}, targetType={}, targetId={}", reporterId, targetType, request.getTargetId());
    }

    private void validateTarget(String targetType, Long targetId) {
        if (TargetType.POST.getCode().equals(targetType)) {
            Post post = postMapper.selectById(targetId);
            if (post == null) {
                throw new BusinessException(ErrorCode.POST_NOT_FOUND);
            }
            if (PostStatus.DELETED.getCode().equals(post.getStatus())) {
                throw new BusinessException(ErrorCode.POST_DELETED);
            }
            return;
        }

        if (TargetType.COMMENT.getCode().equals(targetType)) {
            Comment comment = commentMapper.selectById(targetId);
            if (comment == null) {
                throw new BusinessException(ErrorCode.COMMENT_NOT_FOUND);
            }
            if (Boolean.TRUE.equals(comment.getDeleted())) {
                throw new BusinessException(ErrorCode.COMMENT_DELETED);
            }
            return;
        }

        throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的举报目标类型");
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
}
