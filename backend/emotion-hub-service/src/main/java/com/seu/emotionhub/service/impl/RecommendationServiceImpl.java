package com.seu.emotionhub.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seu.emotionhub.dao.mapper.CommentMapper;
import com.seu.emotionhub.dao.mapper.LikeRecordMapper;
import com.seu.emotionhub.dao.mapper.PostMapper;
import com.seu.emotionhub.dao.mapper.UserMapper;
import com.seu.emotionhub.model.dto.request.EmotionalRecommendationRequest;
import com.seu.emotionhub.model.dto.response.EmotionalRecommendationResponse;
import com.seu.emotionhub.model.dto.response.EmotionStatsDTO;
import com.seu.emotionhub.model.dto.response.PostVO;
import com.seu.emotionhub.model.entity.Comment;
import com.seu.emotionhub.model.entity.ContentEmotionTag;
import com.seu.emotionhub.model.entity.LikeRecord;
import com.seu.emotionhub.model.entity.Post;
import com.seu.emotionhub.model.entity.User;
import com.seu.emotionhub.model.enums.EmotionStateEnum;
import com.seu.emotionhub.model.enums.PostStatus;
import com.seu.emotionhub.model.enums.TargetType;
import com.seu.emotionhub.service.ContentEmotionTagService;
import com.seu.emotionhub.service.RecommendationService;
import com.seu.emotionhub.service.UserEmotionService;
import com.seu.emotionhub.service.cache.HotPostCacheService;
import com.seu.emotionhub.service.cache.RecommendationRedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐服务实现
 *
 * @author EmotionHub Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final LikeRecordMapper likeRecordMapper;
    private final CommentMapper commentMapper;
    private final UserEmotionService userEmotionService;
    private final ContentEmotionTagService contentEmotionTagService;
    private final HotPostCacheService hotPostCacheService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final int DEFAULT_LIMIT = 20;
    private static final int MAX_LIMIT = 50;
    private static final int CANDIDATE_MULTIPLIER = 5;

    @Override
    public EmotionalRecommendationResponse recommendEmotional(EmotionalRecommendationRequest request) {
        Long userId = request.getUserId();
        int limit = normalizeLimit(request.getLimit());
        String strategy = normalizeStrategy(request.getStrategy());

        EmotionStateEnum state = userEmotionService.matchEmotionState(userId);
        EmotionStatsDTO stats = userEmotionService.calculateSlidingWindowStats(userId, "24h");
        Double userAvgScore = stats.getAvgScore();

        List<PostCandidate> candidates = loadCandidates(userId, limit * CANDIDATE_MULTIPLIER);
        Map<Long, ContentEmotionTag> tagMap = contentEmotionTagService.getTagsByPostIds(
                candidates.stream().map(c -> c.post.getId()).collect(Collectors.toSet())
        );

        List<PostCandidate> filtered = filterByEmotion(candidates, tagMap, state, strategy, userAvgScore);
        filtered = filterByInterest(filtered, userId);
        scoreCandidates(filtered, tagMap, state, strategy, userAvgScore);
        filtered.sort((a, b) -> Double.compare(b.finalScore, a.finalScore));

        List<PostCandidate> diversified = diversify(filtered, limit);
        List<PostVO> items = diversified.stream()
                .map(c -> convertToPostVO(c.post))
                .collect(Collectors.toList());

        EmotionalRecommendationResponse response = new EmotionalRecommendationResponse();
        response.setUserId(userId);
        response.setStrategy(strategy);
        response.setEmotionState(state.getName());
        response.setLimit(limit);
        response.setCandidateCount(candidates.size());
        response.setItems(items);
        return response;
    }

    private int normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private String normalizeStrategy(String strategy) {
        if (!StringUtils.hasText(strategy)) {
            return "complementary";
        }
        String value = strategy.trim().toLowerCase(Locale.ROOT);
        if ("similar".equals(value)) {
            return "similar";
        }
        return "complementary";
    }

    private List<PostCandidate> loadCandidates(Long userId, int limit) {
        List<PostCandidate> candidates = new ArrayList<>();

        String recKey = String.format(RecommendationRedisKeyConstants.USER_RECOMMENDATIONS, userId);
        Set<ZSetOperations.TypedTuple<Object>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(recKey, 0, limit - 1);

        if (tuples != null && !tuples.isEmpty()) {
            Map<Long, Double> scoreMap = new LinkedHashMap<>();
            for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
                if (tuple.getValue() == null) continue;
                Long postId = Long.parseLong(tuple.getValue().toString());
                scoreMap.put(postId, tuple.getScore() == null ? 0.0 : tuple.getScore());
            }
            candidates.addAll(loadPostsByIds(scoreMap));
            return candidates;
        }

        Set<Object> hotPostIds = hotPostCacheService.getHotPostIds(limit);
        if (hotPostIds != null && !hotPostIds.isEmpty()) {
            Map<Long, Double> scoreMap = new LinkedHashMap<>();
            for (Object id : hotPostIds) {
                Long postId = Long.parseLong(id.toString());
                scoreMap.put(postId, 1.0);
            }
            candidates.addAll(loadPostsByIds(scoreMap));
            return candidates;
        }

        List<Post> latestPosts = postMapper.selectList(
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getStatus, PostStatus.PUBLISHED.getCode())
                        .orderByDesc(Post::getCreatedAt)
                        .last("LIMIT " + limit)
        );
        for (Post post : latestPosts) {
            candidates.add(new PostCandidate(post, 1.0));
        }

        return candidates;
    }

    private List<PostCandidate> loadPostsByIds(Map<Long, Double> scoreMap) {
        if (scoreMap.isEmpty()) {
            return Collections.emptyList();
        }
        List<Post> posts = postMapper.selectBatchIds(scoreMap.keySet());
        Map<Long, Post> postMap = posts.stream()
                .collect(Collectors.toMap(Post::getId, p -> p));

        List<PostCandidate> candidates = new ArrayList<>();
        for (Map.Entry<Long, Double> entry : scoreMap.entrySet()) {
            Post post = postMap.get(entry.getKey());
            if (post == null) {
                continue;
            }
            if (!PostStatus.PUBLISHED.getCode().equals(post.getStatus())) {
                continue;
            }
            candidates.add(new PostCandidate(post, entry.getValue()));
        }
        return candidates;
    }

    private List<PostCandidate> filterByEmotion(List<PostCandidate> candidates,
                                                Map<Long, ContentEmotionTag> tagMap,
                                                EmotionStateEnum state,
                                                String strategy,
                                                Double userAvgScore) {
        if ("similar".equals(strategy)) {
            if (userAvgScore == null) {
                return candidates;
            }
            return candidates.stream()
                    .filter(c -> c.post.getEmotionScore() != null)
                    .filter(c -> Math.abs(c.post.getEmotionScore().doubleValue() - userAvgScore) <= 0.4)
                    .collect(Collectors.toList());
        }

        return candidates.stream()
                .filter(c -> matchComplementary(c.post, tagMap.get(c.post.getId()), state))
                .collect(Collectors.toList());
    }

    private boolean matchComplementary(Post post, ContentEmotionTag tag, EmotionStateEnum state) {
        String primaryTag = tag != null ? tag.getPrimaryTag() : null;
        BigDecimal score = post.getEmotionScore();

        switch (state) {
            case LOW:
                return "POSITIVE_ENERGY".equals(primaryTag) || "HEALING".equals(primaryTag)
                        || (score != null && score.compareTo(BigDecimal.valueOf(0.6)) >= 0);
            case ANXIOUS:
                return "NEUTRAL_CALM".equals(primaryTag)
                        || (score != null && score.compareTo(BigDecimal.valueOf(-0.2)) > 0
                        && score.compareTo(BigDecimal.valueOf(0.2)) < 0);
            case HAPPY:
                return true;
            case CALM:
                return "NEUTRAL_CALM".equals(primaryTag) || "WARM".equals(primaryTag)
                        || (score != null && score.compareTo(BigDecimal.valueOf(-0.1)) > 0
                        && score.compareTo(BigDecimal.valueOf(0.5)) <= 0);
            case FLUCTUANT:
            default:
                return "NEUTRAL_CALM".equals(primaryTag)
                        || (score != null && score.abs().compareTo(BigDecimal.valueOf(0.3)) <= 0);
        }
    }

    private List<PostCandidate> filterByInterest(List<PostCandidate> candidates, Long userId) {
        if (candidates.isEmpty()) {
            return candidates;
        }

        Set<Long> interacted = loadInteractedPostIds(userId);

        return candidates.stream()
                .filter(c -> !userId.equals(c.post.getUserId()))
                .filter(c -> !interacted.contains(c.post.getId()))
                .collect(Collectors.toList());
    }

    private Set<Long> loadInteractedPostIds(Long userId) {
        Set<Long> interacted = new HashSet<>();

        List<LikeRecord> likes = likeRecordMapper.selectList(
                new LambdaQueryWrapper<LikeRecord>()
                        .eq(LikeRecord::getUserId, userId)
                        .eq(LikeRecord::getTargetType, TargetType.POST.getCode())
        );
        for (LikeRecord like : likes) {
            interacted.add(like.getTargetId());
        }

        List<Comment> comments = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>().eq(Comment::getUserId, userId)
        );
        for (Comment comment : comments) {
            interacted.add(comment.getPostId());
        }

        List<Post> posts = postMapper.selectList(
                new LambdaQueryWrapper<Post>().eq(Post::getUserId, userId)
        );
        for (Post post : posts) {
            interacted.add(post.getId());
        }

        return interacted;
    }

    private void scoreCandidates(List<PostCandidate> candidates,
                                 Map<Long, ContentEmotionTag> tagMap,
                                 EmotionStateEnum state,
                                 String strategy,
                                 Double userAvgScore) {
        if (candidates.isEmpty()) {
            return;
        }
        double maxBase = candidates.stream().mapToDouble(c -> c.baseScore).max().orElse(1.0);
        double minBase = candidates.stream().mapToDouble(c -> c.baseScore).min().orElse(0.0);

        for (PostCandidate candidate : candidates) {
            double base = normalize(candidate.baseScore, minBase, maxBase);
            double emotionMatch = calculateEmotionMatch(candidate.post, tagMap.get(candidate.post.getId()), state, strategy, userAvgScore);
            double recency = calculateRecencyScore(candidate.post.getCreatedAt());
            candidate.finalScore = 0.5 * base + 0.3 * emotionMatch + 0.2 * recency;
        }
    }

    private double calculateEmotionMatch(Post post,
                                         ContentEmotionTag tag,
                                         EmotionStateEnum state,
                                         String strategy,
                                         Double userAvgScore) {
        BigDecimal score = post.getEmotionScore();
        if (score == null) {
            return 0.5;
        }

        if ("similar".equals(strategy) && userAvgScore != null) {
            double diff = Math.abs(score.doubleValue() - userAvgScore);
            return Math.max(0, 1 - diff);
        }

        switch (state) {
            case LOW:
                return clamp((score.doubleValue() + 1) / 2);
            case ANXIOUS:
                return clamp(1 - Math.abs(score.doubleValue()));
            case HAPPY:
                return 0.5;
            case CALM:
                return clamp(1 - Math.abs(score.doubleValue() - 0.2));
            case FLUCTUANT:
            default:
                if (tag != null && "NEUTRAL_CALM".equals(tag.getPrimaryTag())) {
                    return 0.8;
                }
                return clamp(1 - Math.abs(score.doubleValue()));
        }
    }

    private double calculateRecencyScore(LocalDateTime createdAt) {
        if (createdAt == null) {
            return 0.5;
        }
        long hours = Math.max(1, Duration.between(createdAt, LocalDateTime.now()).toHours());
        return Math.exp(-hours / 72.0);
    }

    private double normalize(double value, double min, double max) {
        if (max - min < 1e-6) {
            return 0.5;
        }
        return (value - min) / (max - min);
    }

    private double clamp(double value) {
        if (value < 0) return 0;
        if (value > 1) return 1;
        return value;
    }

    private List<PostCandidate> diversify(List<PostCandidate> sorted, int limit) {
        if (sorted.size() <= limit) {
            return sorted;
        }
        List<PostCandidate> result = new ArrayList<>();
        Map<Long, Integer> authorCount = new HashMap<>();
        Map<String, Integer> labelCount = new HashMap<>();
        int maxPerAuthor = Math.max(1, limit / 4);
        int maxSameLabel = Math.max(1, (int) Math.floor(limit * 0.6));

        for (PostCandidate candidate : sorted) {
            Long authorId = candidate.post.getUserId();
            String label = candidate.post.getEmotionLabel();
            int authorUsed = authorCount.getOrDefault(authorId, 0);
            int labelUsed = labelCount.getOrDefault(label, 0);

            if (authorUsed >= maxPerAuthor || labelUsed >= maxSameLabel) {
                continue;
            }

            result.add(candidate);
            authorCount.put(authorId, authorUsed + 1);
            if (label != null) {
                labelCount.put(label, labelUsed + 1);
            }

            if (result.size() >= limit) {
                break;
            }
        }

        if (result.size() < limit) {
            for (PostCandidate candidate : sorted) {
                if (result.contains(candidate)) {
                    continue;
                }
                result.add(candidate);
                if (result.size() >= limit) {
                    break;
                }
            }
        }

        return result;
    }

    private PostVO convertToPostVO(Post post) {
        PostVO vo = new PostVO();
        vo.setId(post.getId());
        vo.setUserId(post.getUserId());
        vo.setContent(post.getContent());
        vo.setEmotionScore(post.getEmotionScore());
        vo.setEmotionLabel(post.getEmotionLabel());
        vo.setViewCount(post.getViewCount());
        vo.setLikeCount(post.getLikeCount());
        vo.setCommentCount(post.getCommentCount());
        vo.setStatus(post.getStatus());
        vo.setCreatedAt(post.getCreatedAt());
        vo.setUpdatedAt(post.getUpdatedAt());

        if (StringUtils.hasText(post.getImages())) {
            vo.setImages(JSON.parseArray(post.getImages(), String.class));
        }

        User user = userMapper.selectById(post.getUserId());
        if (user != null) {
            vo.setUsername(user.getUsername());
            vo.setNickname(user.getNickname());
            vo.setAvatar(user.getAvatar());
        }
        return vo;
    }

    private static class PostCandidate {
        private final Post post;
        private final double baseScore;
        private double finalScore;

        private PostCandidate(Post post, double baseScore) {
            this.post = post;
            this.baseScore = baseScore;
        }
    }
}
