package com.seu.emotionhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seu.emotionhub.common.enums.ErrorCode;
import com.seu.emotionhub.common.exception.BusinessException;
import com.seu.emotionhub.dao.mapper.CommentMapper;
import com.seu.emotionhub.dao.mapper.LikeRecordMapper;
import com.seu.emotionhub.dao.mapper.PostMapper;
import com.seu.emotionhub.dao.mapper.UserMapper;
import com.seu.emotionhub.model.entity.Comment;
import com.seu.emotionhub.model.entity.Post;
import com.seu.emotionhub.model.entity.User;
import com.seu.emotionhub.model.enums.EmotionLabel;
import com.seu.emotionhub.model.enums.PostStatus;
import com.seu.emotionhub.service.StatsService;
import com.seu.emotionhub.service.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 统计服务实现类
 *
 * @author EmotionHub Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final UserMapper userMapper;
    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final LikeRecordMapper likeRecordMapper;
    private final CacheService cacheService;

    @Override
    public Map<String, Object> getUserStats(Long userId) {
        String cacheKey = CacheService.CacheKey.STATS_USER + userId;
        Map<String, Object> cached = cacheService.get(cacheKey, Map.class);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }

        Map<String, Object> stats = new HashMap<>();

        // 用户基本信息
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 发帖数
        LambdaQueryWrapper<Post> postQuery = new LambdaQueryWrapper<>();
        postQuery.eq(Post::getUserId, userId)
                .ne(Post::getStatus, PostStatus.DELETED.getCode());
        long postCount = postMapper.selectCount(postQuery);

        // 评论数
        LambdaQueryWrapper<Comment> commentQuery = new LambdaQueryWrapper<>();
        commentQuery.eq(Comment::getUserId, userId);
        long commentCount = commentMapper.selectCount(commentQuery);

        // 获赞数（帖子+评论）
        int totalLikes = 0;

        // 统计帖子被点赞数
        LambdaQueryWrapper<Post> userPostQuery = new LambdaQueryWrapper<>();
        userPostQuery.eq(Post::getUserId, userId)
                .select(Post::getLikeCount);
        List<Post> userPosts = postMapper.selectList(userPostQuery);
        totalLikes += userPosts.stream()
                .mapToInt(Post::getLikeCount)
                .sum();

        // 情感统计
        LambdaQueryWrapper<Post> positiveQuery = new LambdaQueryWrapper<>();
        positiveQuery.eq(Post::getUserId, userId)
                .eq(Post::getEmotionLabel, EmotionLabel.POSITIVE.getCode());
        long positiveCount = postMapper.selectCount(positiveQuery);

        LambdaQueryWrapper<Post> negativeQuery = new LambdaQueryWrapper<>();
        negativeQuery.eq(Post::getUserId, userId)
                .eq(Post::getEmotionLabel, EmotionLabel.NEGATIVE.getCode());
        long negativeCount = postMapper.selectCount(negativeQuery);

        LambdaQueryWrapper<Post> neutralQuery = new LambdaQueryWrapper<>();
        neutralQuery.eq(Post::getUserId, userId)
                .eq(Post::getEmotionLabel, EmotionLabel.NEUTRAL.getCode());
        long neutralCount = postMapper.selectCount(neutralQuery);

        stats.put("userId", userId);
        stats.put("username", user.getUsername());
        stats.put("nickname", user.getNickname());
        stats.put("postCount", postCount);
        stats.put("commentCount", commentCount);
        stats.put("totalLikes", totalLikes);
        stats.put("emotionStats", Map.of(
                "positive", positiveCount,
                "negative", negativeCount,
                "neutral", neutralCount
        ));

        cacheService.set(cacheKey, stats, CacheService.CacheTTL.STATS, TimeUnit.SECONDS);
        return stats;
    }

    @Override
    public Map<String, Object> getPlatformStats() {
        String cacheKey = CacheService.CacheKey.STATS_PLATFORM;
        Map<String, Object> cached = cacheService.get(cacheKey, Map.class);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }

        Map<String, Object> stats = new HashMap<>();

        // 总用户数
        long totalUsers = userMapper.selectCount(null);

        // 总帖子数
        LambdaQueryWrapper<Post> postQuery = new LambdaQueryWrapper<>();
        postQuery.ne(Post::getStatus, PostStatus.DELETED.getCode());
        long totalPosts = postMapper.selectCount(postQuery);

        // 总评论数
        long totalComments = commentMapper.selectCount(null);

        // 总点赞数
        long totalLikes = likeRecordMapper.selectCount(null);

        // 情感分布
        LambdaQueryWrapper<Post> positiveQuery = new LambdaQueryWrapper<>();
        positiveQuery.eq(Post::getEmotionLabel, EmotionLabel.POSITIVE.getCode());
        long positiveCount = postMapper.selectCount(positiveQuery);

        LambdaQueryWrapper<Post> negativeQuery = new LambdaQueryWrapper<>();
        negativeQuery.eq(Post::getEmotionLabel, EmotionLabel.NEGATIVE.getCode());
        long negativeCount = postMapper.selectCount(negativeQuery);

        LambdaQueryWrapper<Post> neutralQuery = new LambdaQueryWrapper<>();
        neutralQuery.eq(Post::getEmotionLabel, EmotionLabel.NEUTRAL.getCode());
        long neutralCount = postMapper.selectCount(neutralQuery);

        stats.put("totalUsers", totalUsers);
        stats.put("totalPosts", totalPosts);
        stats.put("totalComments", totalComments);
        stats.put("totalLikes", totalLikes);
        stats.put("emotionDistribution", Map.of(
                "positive", positiveCount,
                "negative", negativeCount,
                "neutral", neutralCount
        ));

        cacheService.set(cacheKey, stats, CacheService.CacheTTL.STATS, TimeUnit.SECONDS);
        return stats;
    }

    @Override
    public Map<String, Object> getMyStats() {
        Long userId = getCurrentUserId();
        return getUserStats(userId);
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
}
