package com.seu.emotionhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seu.emotionhub.common.enums.ErrorCode;
import com.seu.emotionhub.common.exception.BusinessException;
import com.seu.emotionhub.dao.mapper.PostMapper;
import com.seu.emotionhub.dao.mapper.UserMapper;
import com.seu.emotionhub.model.dto.request.PostCreateRequest;
import com.seu.emotionhub.model.dto.request.PostQueryRequest;
import com.seu.emotionhub.model.dto.response.PageResult;
import com.seu.emotionhub.model.dto.response.PostVO;
import com.seu.emotionhub.model.entity.Post;
import com.seu.emotionhub.model.entity.User;
import com.seu.emotionhub.model.enums.PostStatus;
import com.seu.emotionhub.service.EmotionAnalysisService;
import com.seu.emotionhub.service.PostService;
import com.seu.emotionhub.service.cache.CacheService;
import com.seu.emotionhub.service.cache.HotPostCacheService;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 帖子服务实现类
 *
 * @author EmotionHub Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final EmotionAnalysisService emotionAnalysisService;
    private final CacheService cacheService;
    private final HotPostCacheService hotPostCacheService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostVO createPost(PostCreateRequest request) {
        Long userId = getCurrentUserId();

        // 校验内容长度
        if (request.getContent().length() > 5000) {
            throw new BusinessException(ErrorCode.CONTENT_TOO_LONG);
        }

        // 创建帖子
        Post post = new Post();
        post.setUserId(userId);
        post.setContent(request.getContent());
        // 将图片列表转换为JSON字符串
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            post.setImages(JSON.toJSONString(request.getImages()));
        }
        post.setStatus(PostStatus.ANALYZING.getCode()); // 默认分析中
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setCommentCount(0);

        int rows = postMapper.insert(post);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "发帖失败");
        }

        log.info("用户发帖成功: userId={}, postId={}", userId, post.getId());

        // 异步触发情感分析
        emotionAnalysisService.analyzePostAsync(post.getId());

        return convertToPostVO(post);
    }

    @Override
    public PageResult<PostVO> listPosts(PostQueryRequest request) {
        // 构建查询条件
        LambdaQueryWrapper<Post> query = new LambdaQueryWrapper<>();

        // 只查询已发布的帖子
        query.eq(Post::getStatus, PostStatus.PUBLISHED.getCode());

        // 情感标签过滤
        if (StringUtils.hasText(request.getEmotionLabel())) {
            query.eq(Post::getEmotionLabel, request.getEmotionLabel());
        }

        // 用户ID过滤
        if (request.getUserId() != null) {
            query.eq(Post::getUserId, request.getUserId());
        }

        // 排序
        if ("HOT".equals(request.getOrderBy())) {
            // 热度排序（点赞数 + 评论数）
            query.orderByDesc(Post::getLikeCount, Post::getCommentCount);
        } else {
            // 默认最新排序
            query.orderByDesc(Post::getCreatedAt);
        }

        // 分页查询
        Page<Post> page = new Page<>(request.getPage(), request.getSize());
        Page<Post> postPage = postMapper.selectPage(page, query);

        // 转换为VO
        List<PostVO> postVOList = postPage.getRecords().stream()
                .map(this::convertToPostVO)
                .collect(Collectors.toList());

        return new PageResult<>(
                postVOList,
                postPage.getTotal(),
                (int) postPage.getCurrent(),
                (int) postPage.getSize()
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostVO getPostDetail(Long postId) {
        String cacheKey = CacheService.CacheKey.POST_DETAIL + postId;
        Post post = cacheService.get(cacheKey, Post.class);
        if (post == null) {
            post = postMapper.selectById(postId);
            if (post != null) {
                cacheService.setWithBloom(cacheKey, post, CacheService.CacheTTL.POST_DETAIL, TimeUnit.SECONDS);
            }
        }

        if (post == null) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        if (PostStatus.DELETED.getCode().equals(post.getStatus())) {
            throw new BusinessException(ErrorCode.POST_DELETED);
        }

        // 浏览量+1
        postMapper.incrementViewCount(postId, 1);
        post.setViewCount(post.getViewCount() + 1);
        cacheService.set(cacheKey, post, CacheService.CacheTTL.POST_DETAIL, TimeUnit.SECONDS);
        hotPostCacheService.updateHotScore(postId, "view");

        return convertToPostVO(post);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long postId) {
        Long userId = getCurrentUserId();

        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new BusinessException(ErrorCode.POST_NOT_FOUND);
        }

        // 权限校验：只能删除自己的帖子
        if (!post.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.POST_NOT_OWNER);
        }

        // 软删除
        post.setStatus(PostStatus.DELETED.getCode());
        int rows = postMapper.updateById(post);

        if (rows == 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除帖子失败");
        }

        log.info("用户删除帖子: userId={}, postId={}", userId, postId);
        hotPostCacheService.invalidatePostCache(postId);
    }

    @Override
    public PageResult<PostVO> getUserPosts(Long userId, Integer page, Integer size) {
        LambdaQueryWrapper<Post> query = new LambdaQueryWrapper<>();
        query.eq(Post::getUserId, userId);
        query.ne(Post::getStatus, PostStatus.DELETED.getCode()); // 排除已删除
        query.orderByDesc(Post::getCreatedAt);

        Page<Post> postPage = postMapper.selectPage(new Page<>(page, size), query);

        List<PostVO> postVOList = postPage.getRecords().stream()
                .map(this::convertToPostVO)
                .collect(Collectors.toList());

        return new PageResult<>(
                postVOList,
                postPage.getTotal(),
                (int) postPage.getCurrent(),
                (int) postPage.getSize()
        );
    }

    /**
     * 获取当前登录用户ID
     */
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

    /**
     * 转换为PostVO
     */
    private PostVO convertToPostVO(Post post) {
        PostVO vo = new PostVO();
        BeanUtils.copyProperties(post, vo);

        // 解析图片JSON字符串
        if (StringUtils.hasText(post.getImages())) {
            vo.setImages(JSON.parseArray(post.getImages(), String.class));
        }

        // 查询作者信息
        User user = userMapper.selectById(post.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }

        return vo;
    }
}
