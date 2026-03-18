package com.seu.emotionhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seu.emotionhub.dao.mapper.CommentMapper;
import com.seu.emotionhub.dao.mapper.ContentEmotionTagMapper;
import com.seu.emotionhub.dao.mapper.PostMapper;
import com.seu.emotionhub.model.entity.Comment;
import com.seu.emotionhub.model.entity.ContentEmotionTag;
import com.seu.emotionhub.model.entity.Post;
import com.seu.emotionhub.model.enums.PostStatus;
import com.seu.emotionhub.service.ContentEmotionTagService;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 内容情感标签服务实现
 *
 * @author EmotionHub Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContentEmotionTagServiceImpl implements ContentEmotionTagService {

    private final PostMapper postMapper;
    private final CommentMapper commentMapper;
    private final ContentEmotionTagMapper contentEmotionTagMapper;

    @Override
    public Map<Long, ContentEmotionTag> getTagsByPostIds(Collection<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ContentEmotionTag> tags = contentEmotionTagMapper.selectList(
                new LambdaQueryWrapper<ContentEmotionTag>()
                        .in(ContentEmotionTag::getPostId, postIds)
        );
        return tags.stream().collect(Collectors.toMap(ContentEmotionTag::getPostId, t -> t));
    }

    /**
     * 定时刷新内容标签（每小时）
     */
    @Scheduled(cron = "0 10 * * * ?")
    @Override
    public void refreshContentTags() {
        try {
            LocalDateTime since = LocalDateTime.now().minusDays(30);
            List<Post> posts = postMapper.selectList(
                    new LambdaQueryWrapper<Post>()
                            .eq(Post::getStatus, PostStatus.PUBLISHED.getCode())
                            .ge(Post::getCreatedAt, since)
            );

            if (posts.isEmpty()) {
                return;
            }

            for (Post post : posts) {
                List<Comment> comments = commentMapper.selectList(
                        new LambdaQueryWrapper<Comment>().eq(Comment::getPostId, post.getId())
                );
                ContentEmotionTag tag = buildTag(post, comments);
                upsertTag(tag);
            }

            log.info("内容情感标签刷新完成，共 {} 条", posts.size());
        } catch (Exception e) {
            log.error("内容情感标签刷新失败", e);
        }
    }

    private ContentEmotionTag buildTag(Post post, List<Comment> comments) {
        BigDecimal sentimentScore = post.getEmotionScore() == null ? BigDecimal.ZERO : post.getEmotionScore();
        BigDecimal controversyScore = calculateStdDev(comments);

        List<String> tags = new ArrayList<>();
        String primaryTag = determinePrimaryTag(sentimentScore);
        tags.add(primaryTag);

        if (sentimentScore.compareTo(BigDecimal.valueOf(0.6)) >= 0 && controversyScore.compareTo(BigDecimal.valueOf(0.3)) < 0) {
            tags.add("HEALING");
        }
        if (controversyScore.compareTo(BigDecimal.valueOf(0.5)) >= 0) {
            tags.add("CONTROVERSIAL");
        }

        ContentEmotionTag tag = new ContentEmotionTag();
        tag.setPostId(post.getId());
        tag.setPrimaryTag(primaryTag);
        tag.setTags(JSON.toJSONString(tags));
        tag.setSentimentScore(sentimentScore);
        tag.setControversyScore(controversyScore);
        return tag;
    }

    private String determinePrimaryTag(BigDecimal sentimentScore) {
        if (sentimentScore.compareTo(BigDecimal.valueOf(0.6)) >= 0) {
            return "POSITIVE_ENERGY";
        }
        if (sentimentScore.compareTo(BigDecimal.valueOf(0.2)) >= 0) {
            return "WARM";
        }
        if (sentimentScore.compareTo(BigDecimal.valueOf(-0.2)) > 0) {
            return "NEUTRAL_CALM";
        }
        if (sentimentScore.compareTo(BigDecimal.valueOf(-0.6)) > 0) {
            return "LOW_MOOD";
        }
        return "NEGATIVE_INTENSE";
    }

    private BigDecimal calculateStdDev(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<BigDecimal> scores = comments.stream()
                .map(Comment::getEmotionScore)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (scores.isEmpty()) {
            return BigDecimal.ZERO;
        }
        double avg = scores.stream().mapToDouble(BigDecimal::doubleValue).average().orElse(0.0);
        double variance = scores.stream()
                .mapToDouble(s -> Math.pow(s.doubleValue() - avg, 2))
                .average()
                .orElse(0.0);
        double stdDev = Math.sqrt(variance);
        return BigDecimal.valueOf(stdDev).setScale(4, RoundingMode.HALF_UP);
    }

    private void upsertTag(ContentEmotionTag tag) {
        ContentEmotionTag existing = contentEmotionTagMapper.selectByPostId(tag.getPostId());
        if (existing == null) {
            contentEmotionTagMapper.insert(tag);
        } else {
            tag.setId(existing.getId());
            contentEmotionTagMapper.updateById(tag);
        }
    }
}
