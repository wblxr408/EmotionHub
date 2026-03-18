package com.seu.emotionhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seu.emotionhub.dao.mapper.CommentMapper;
import com.seu.emotionhub.dao.mapper.LikeRecordMapper;
import com.seu.emotionhub.dao.mapper.PostMapper;
import com.seu.emotionhub.model.entity.Comment;
import com.seu.emotionhub.model.entity.LikeRecord;
import com.seu.emotionhub.model.entity.Post;
import com.seu.emotionhub.model.enums.PostStatus;
import com.seu.emotionhub.model.enums.TargetType;
import com.seu.emotionhub.service.CollaborativeFilteringService;
import com.seu.emotionhub.service.cache.RecommendationRedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 协同过滤服务实现
 *
 * @author EmotionHub Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollaborativeFilteringServiceImpl implements CollaborativeFilteringService {

    private final LikeRecordMapper likeRecordMapper;
    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final double LIKE_WEIGHT = 2.0;
    private static final double COMMENT_WEIGHT = 3.0;
    private static final double POST_WEIGHT = 1.0;

    private static final int TOP_SIMILAR_USERS = 20;
    private static final int PRECOMPUTE_RECOMMEND_LIMIT = 200;
    private static final int ALS_FACTORS = 10;
    private static final int ALS_ITERATIONS = 8;
    private static final double ALS_LAMBDA = 0.1;
    private static final int ALS_TOP_N = 200;
    private static final int LOOKBACK_DAYS = 30;

    /**
     * 每日凌晨计算相似度矩阵并预生成推荐列表
     */
    @Scheduled(cron = "0 30 2 * * ?")
    @Override
    public void rebuildRecommendations() {
        try {
            log.info("开始协同过滤相似度计算...");

            LocalDateTime since = LocalDateTime.now().minusDays(LOOKBACK_DAYS);
            Map<Long, Map<Long, Double>> userItemMatrix = buildUserItemMatrix(since);
            Map<Long, double[]> emotionVectors = buildUserEmotionVectors();

            if (userItemMatrix.isEmpty()) {
                log.info("无交互数据，跳过协同过滤计算");
                return;
            }

            Map<Long, Map<Long, Double>> similarityMap = computeUserSimilarities(userItemMatrix, emotionVectors);
            Set<Long> validPostIds = loadValidPostIds();
            Map<Long, Map<Long, Double>> alsRecommendations = computeAlsRecommendations(userItemMatrix, validPostIds);

            for (Map.Entry<Long, Map<Long, Double>> entry : similarityMap.entrySet()) {
                Long userId = entry.getKey();
                Map<Long, Double> simUsers = entry.getValue();

                // 写入相似用户ZSet
                String simKey = String.format(RecommendationRedisKeyConstants.USER_SIMILARITY, userId);
                redisTemplate.delete(simKey);
                simUsers.forEach((otherId, sim) ->
                        redisTemplate.opsForZSet().add(simKey, otherId.toString(), sim)
                );
                redisTemplate.expire(simKey, RecommendationRedisKeyConstants.TTL_SECONDS, TimeUnit.SECONDS);

                // 生成推荐列表
                Map<Long, Double> recommendations = buildRecommendationsForUser(userId, simUsers, userItemMatrix, validPostIds);
                Map<Long, Double> alsRec = alsRecommendations.getOrDefault(userId, Collections.emptyMap());
                alsRec.forEach((postId, score) ->
                        recommendations.merge(postId, score * 0.3, Double::sum)
                );
                Map<Long, Double> finalRecommendations = trimTopK(recommendations, PRECOMPUTE_RECOMMEND_LIMIT);
                String recKey = String.format(RecommendationRedisKeyConstants.USER_RECOMMENDATIONS, userId);
                redisTemplate.delete(recKey);
                finalRecommendations.forEach((postId, score) ->
                        redisTemplate.opsForZSet().add(recKey, postId.toString(), score)
                );
                redisTemplate.expire(recKey, RecommendationRedisKeyConstants.TTL_SECONDS, TimeUnit.SECONDS);
            }

            log.info("协同过滤计算完成，用户数={}", similarityMap.size());
        } catch (Exception e) {
            log.error("协同过滤计算失败", e);
        }
    }

    private Map<Long, Map<Long, Double>> buildUserItemMatrix(LocalDateTime since) {
        Map<Long, Map<Long, Double>> matrix = new HashMap<>();

        List<LikeRecord> likes = likeRecordMapper.selectList(
                new LambdaQueryWrapper<LikeRecord>()
                        .eq(LikeRecord::getTargetType, TargetType.POST.getCode())
                        .ge(LikeRecord::getCreatedAt, since)
        );
        for (LikeRecord like : likes) {
            matrix.computeIfAbsent(like.getUserId(), k -> new HashMap<>())
                    .merge(like.getTargetId(), LIKE_WEIGHT, Double::sum);
        }

        List<Comment> comments = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>().ge(Comment::getCreatedAt, since)
        );
        for (Comment comment : comments) {
            matrix.computeIfAbsent(comment.getUserId(), k -> new HashMap<>())
                    .merge(comment.getPostId(), COMMENT_WEIGHT, Double::sum);
        }

        List<Post> posts = postMapper.selectList(
                new LambdaQueryWrapper<Post>().eq(Post::getStatus, PostStatus.PUBLISHED.getCode())
        );
        for (Post post : posts) {
            matrix.computeIfAbsent(post.getUserId(), k -> new HashMap<>())
                    .merge(post.getId(), POST_WEIGHT, Double::sum);
        }

        return matrix;
    }

    private Map<Long, double[]> buildUserEmotionVectors() {
        Map<Long, double[]> vectors = new HashMap<>();

        List<Post> posts = postMapper.selectList(
                new LambdaQueryWrapper<Post>()
                        .eq(Post::getStatus, PostStatus.PUBLISHED.getCode())
                        .isNotNull(Post::getEmotionLabel)
        );
        for (Post post : posts) {
            double[] vec = vectors.computeIfAbsent(post.getUserId(), k -> new double[3]);
            accumulateEmotionVector(vec, post.getEmotionLabel());
        }

        List<Comment> comments = commentMapper.selectList(
                new LambdaQueryWrapper<Comment>().isNotNull(Comment::getEmotionLabel)
        );
        for (Comment comment : comments) {
            double[] vec = vectors.computeIfAbsent(comment.getUserId(), k -> new double[3]);
            accumulateEmotionVector(vec, comment.getEmotionLabel());
        }

        // 归一化
        for (Map.Entry<Long, double[]> entry : vectors.entrySet()) {
            double[] vec = entry.getValue();
            double sum = vec[0] + vec[1] + vec[2];
            if (sum > 0) {
                vec[0] /= sum;
                vec[1] /= sum;
                vec[2] /= sum;
            }
        }

        return vectors;
    }

    private void accumulateEmotionVector(double[] vec, String label) {
        if ("POSITIVE".equalsIgnoreCase(label)) {
            vec[0] += 1.0;
        } else if ("NEUTRAL".equalsIgnoreCase(label)) {
            vec[1] += 1.0;
        } else if ("NEGATIVE".equalsIgnoreCase(label)) {
            vec[2] += 1.0;
        }
    }

    private Map<Long, Map<Long, Double>> computeUserSimilarities(
            Map<Long, Map<Long, Double>> matrix,
            Map<Long, double[]> emotionVectors) {

        Map<Long, Map<Long, Double>> result = new HashMap<>();
        List<Long> users = new ArrayList<>(matrix.keySet());

        for (int i = 0; i < users.size(); i++) {
            Long u1 = users.get(i);
            for (int j = i + 1; j < users.size(); j++) {
                Long u2 = users.get(j);
                double interestSim = cosine(matrix.get(u1), matrix.get(u2));
                double emotionSim = cosine(emotionVectors.get(u1), emotionVectors.get(u2));
                double sim = 0.6 * interestSim + 0.4 * emotionSim;
                if (sim <= 0) {
                    continue;
                }
                result.computeIfAbsent(u1, k -> new HashMap<>()).put(u2, sim);
                result.computeIfAbsent(u2, k -> new HashMap<>()).put(u1, sim);
            }
        }

        // 截断只保留TopK相似用户
        for (Map.Entry<Long, Map<Long, Double>> entry : result.entrySet()) {
            Map<Long, Double> top = trimTopK(entry.getValue(), TOP_SIMILAR_USERS);
            entry.setValue(top);
        }

        return result;
    }

    private Map<Long, Double> trimTopK(Map<Long, Double> scores, int k) {
        return scores.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(k)
                .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), LinkedHashMap::putAll);
    }

    private Map<Long, Double> buildRecommendationsForUser(Long userId,
                                                          Map<Long, Double> simUsers,
                                                          Map<Long, Map<Long, Double>> matrix,
                                                          Set<Long> validPostIds) {
        Map<Long, Double> result = new HashMap<>();
        Set<Long> userItems = matrix.getOrDefault(userId, Collections.emptyMap()).keySet();

        for (Map.Entry<Long, Double> entry : simUsers.entrySet()) {
            Long otherId = entry.getKey();
            double sim = entry.getValue();
            Map<Long, Double> otherItems = matrix.getOrDefault(otherId, Collections.emptyMap());
            for (Map.Entry<Long, Double> item : otherItems.entrySet()) {
                Long postId = item.getKey();
                if (userItems.contains(postId)) {
                    continue;
                }
                if (!validPostIds.contains(postId)) {
                    continue;
                }
                result.merge(postId, sim * item.getValue(), Double::sum);
            }
        }

        return result.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(PRECOMPUTE_RECOMMEND_LIMIT)
                .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue()), LinkedHashMap::putAll);
    }

    private Set<Long> loadValidPostIds() {
        List<Post> posts = postMapper.selectList(
                new LambdaQueryWrapper<Post>().eq(Post::getStatus, PostStatus.PUBLISHED.getCode())
        );
        Set<Long> ids = new HashSet<>();
        for (Post post : posts) {
            ids.add(post.getId());
        }
        return ids;
    }

    private double cosine(Map<Long, Double> v1, Map<Long, Double> v2) {
        if (v1 == null || v2 == null || v1.isEmpty() || v2.isEmpty()) {
            return 0.0;
        }
        double dot = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        for (Map.Entry<Long, Double> entry : v1.entrySet()) {
            double x = entry.getValue();
            norm1 += x * x;
            Double y = v2.get(entry.getKey());
            if (y != null) {
                dot += x * y;
            }
        }
        for (double y : v2.values()) {
            norm2 += y * y;
        }
        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    private double cosine(double[] v1, double[] v2) {
        if (v1 == null || v2 == null || v1.length == 0 || v2.length == 0) {
            return 0.0;
        }
        double dot = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        int len = Math.min(v1.length, v2.length);
        for (int i = 0; i < len; i++) {
            dot += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }
        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * ALS矩阵分解生成推荐
     */
    private Map<Long, Map<Long, Double>> computeAlsRecommendations(
            Map<Long, Map<Long, Double>> matrix,
            Set<Long> validPostIds) {

        if (matrix.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> userIds = new ArrayList<>(matrix.keySet());
        Set<Long> itemSet = new HashSet<>();
        for (Map<Long, Double> items : matrix.values()) {
            itemSet.addAll(items.keySet());
        }
        itemSet.retainAll(validPostIds);
        List<Long> itemIds = new ArrayList<>(itemSet);

        Map<Long, Integer> userIndex = new HashMap<>();
        Map<Long, Integer> itemIndex = new HashMap<>();
        for (int i = 0; i < userIds.size(); i++) {
            userIndex.put(userIds.get(i), i);
        }
        for (int i = 0; i < itemIds.size(); i++) {
            itemIndex.put(itemIds.get(i), i);
        }

        List<List<Rating>> userRatings = new ArrayList<>();
        List<List<Rating>> itemRatings = new ArrayList<>();
        for (int i = 0; i < userIds.size(); i++) {
            userRatings.add(new ArrayList<>());
        }
        for (int i = 0; i < itemIds.size(); i++) {
            itemRatings.add(new ArrayList<>());
        }

        for (Map.Entry<Long, Map<Long, Double>> entry : matrix.entrySet()) {
            int u = userIndex.get(entry.getKey());
            for (Map.Entry<Long, Double> item : entry.getValue().entrySet()) {
                Integer i = itemIndex.get(item.getKey());
                if (i == null) {
                    continue;
                }
                double rating = item.getValue();
                userRatings.get(u).add(new Rating(i, rating));
                itemRatings.get(i).add(new Rating(u, rating));
            }
        }

        double[][] userFactors = initFactors(userIds.size(), ALS_FACTORS);
        double[][] itemFactors = initFactors(itemIds.size(), ALS_FACTORS);

        for (int iter = 0; iter < ALS_ITERATIONS; iter++) {
            // 更新用户因子
            for (int u = 0; u < userIds.size(); u++) {
                userFactors[u] = solveFactors(itemFactors, userRatings.get(u));
            }
            // 更新物品因子
            for (int i = 0; i < itemIds.size(); i++) {
                itemFactors[i] = solveFactors(userFactors, itemRatings.get(i));
            }
        }

        Map<Long, Map<Long, Double>> result = new HashMap<>();
        for (int u = 0; u < userIds.size(); u++) {
            Map<Long, Double> scores = predictTopN(u, userFactors, itemFactors, itemIds, userRatings.get(u));
            result.put(userIds.get(u), scores);
        }

        return result;
    }

    private double[][] initFactors(int rows, int factors) {
        Random random = new Random(42);
        double[][] mat = new double[rows][factors];
        for (int i = 0; i < rows; i++) {
            for (int f = 0; f < factors; f++) {
                mat[i][f] = 0.1 * random.nextDouble();
            }
        }
        return mat;
    }

    private double[] solveFactors(double[][] fixedFactors, List<Rating> ratings) {
        int k = ALS_FACTORS;
        double[][] A = new double[k][k];
        double[] b = new double[k];

        for (int i = 0; i < k; i++) {
            A[i][i] = ALS_LAMBDA;
        }

        for (Rating rating : ratings) {
            double[] vec = fixedFactors[rating.index];
            for (int i = 0; i < k; i++) {
                b[i] += vec[i] * rating.value;
                for (int j = 0; j < k; j++) {
                    A[i][j] += vec[i] * vec[j];
                }
            }
        }

        return solveLinearSystem(A, b);
    }

    private double[] solveLinearSystem(double[][] A, double[] b) {
        int n = b.length;
        double[][] aug = new double[n][n + 1];

        for (int i = 0; i < n; i++) {
            System.arraycopy(A[i], 0, aug[i], 0, n);
            aug[i][n] = b[i];
        }

        for (int i = 0; i < n; i++) {
            int pivot = i;
            for (int r = i + 1; r < n; r++) {
                if (Math.abs(aug[r][i]) > Math.abs(aug[pivot][i])) {
                    pivot = r;
                }
            }
            if (Math.abs(aug[pivot][i]) < 1e-9) {
                return new double[n];
            }
            if (pivot != i) {
                double[] tmp = aug[i];
                aug[i] = aug[pivot];
                aug[pivot] = tmp;
            }

            double div = aug[i][i];
            for (int j = i; j <= n; j++) {
                aug[i][j] /= div;
            }

            for (int r = 0; r < n; r++) {
                if (r == i) continue;
                double factor = aug[r][i];
                for (int j = i; j <= n; j++) {
                    aug[r][j] -= factor * aug[i][j];
                }
            }
        }

        double[] x = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = aug[i][n];
        }
        return x;
    }

    private Map<Long, Double> predictTopN(int userIndex,
                                          double[][] userFactors,
                                          double[][] itemFactors,
                                          List<Long> itemIds,
                                          List<Rating> userRatings) {

        Set<Integer> interacted = userRatings.stream().map(r -> r.index).collect(Collectors.toSet());
        PriorityQueue<Map.Entry<Long, Double>> heap = new PriorityQueue<>(Map.Entry.comparingByValue());

        for (int i = 0; i < itemIds.size(); i++) {
            if (interacted.contains(i)) {
                continue;
            }
            double score = dot(userFactors[userIndex], itemFactors[i]);
            if (heap.size() < ALS_TOP_N) {
                heap.offer(Map.entry(itemIds.get(i), score));
            } else if (heap.peek().getValue() < score) {
                heap.poll();
                heap.offer(Map.entry(itemIds.get(i), score));
            }
        }

        List<Map.Entry<Long, Double>> entries = new ArrayList<>(heap);
        entries.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
        Map<Long, Double> result = new LinkedHashMap<>();
        for (Map.Entry<Long, Double> entry : entries) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private double dot(double[] a, double[] b) {
        double sum = 0.0;
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    private static class Rating {
        private final int index;
        private final double value;

        private Rating(int index, double value) {
            this.index = index;
            this.value = value;
        }
    }
}
