package com.seu.emotionhub.service.impl;
import java.io.IOException;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.seu.emotionhub.model.entity.ContentEmotionTag;
import com.seu.emotionhub.model.entity.Post;
import com.seu.emotionhub.model.enums.EmotionStateEnum;
import com.seu.emotionhub.service.RankerService;
import com.seu.emotionhub.service.config.RankerProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * ML 排序服务实现 - 调用 Flask 预测服务
 *
 * @author EmotionHub Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RankerServiceImpl implements RankerService {

    private final RankerProperties rankerProperties;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(300))
            .build();

    @Override
    public List<Double> predict(List<Post> posts,
                                Map<Long, ContentEmotionTag> tagMap,
                                EmotionStateEnum state,
                                Map<Long, Double> baseScores,
                                Double userAvgScore,
                                Double userVolatility,
                                String trendType,
                                Map<Long, Double> authorInfluenceScores) {
        List<Map<String, Object>> candidates = buildCandidates(
                posts, tagMap, state, baseScores, userAvgScore, userVolatility, trendType, authorInfluenceScores);

        Map<String, Object> body = new HashMap<>();
        body.put("candidates", candidates);

        String url = rankerProperties.getUrl() + "/predict";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(rankerProperties.getTimeoutMs()))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(body)))
                .build();

        HttpResponse<String> response;

        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new RuntimeException("Ranker服务IO异常", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Ranker调用被中断", e);
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException("Ranker service returned HTTP " + response.statusCode());
        }

        JSONObject json = JSON.parseObject(response.body());
        List<Double> scores = json.getList("scores", Double.class);

        if (scores == null || scores.size() != posts.size()) {
            throw new RuntimeException("Ranker service returned unexpected scores size");
        }

        return scores;
    }

    private List<Map<String, Object>> buildCandidates(List<Post> posts,
                                                       Map<Long, ContentEmotionTag> tagMap,
                                                       EmotionStateEnum state,
                                                       Map<Long, Double> baseScores,
                                                       Double userAvgScore,
                                                       Double userVolatility,
                                                       String trendType,
                                                       Map<Long, Double> authorInfluenceScores) {
        List<Map<String, Object>> candidates = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 0; i < posts.size(); i++) {
            Post post = posts.get(i);
            ContentEmotionTag tag = tagMap.get(post.getId());
            Map<String, Object> c = new HashMap<>();

            // 帖子质量特征
            c.put("post_id", post.getId());
            c.put("post_emotion_score",
                    post.getEmotionScore() != null ? post.getEmotionScore().doubleValue() : 0.0);
            c.put("like_count", post.getLikeCount() != null ? post.getLikeCount() : 0);
            c.put("comment_count", post.getCommentCount() != null ? post.getCommentCount() : 0);
            c.put("view_count", post.getViewCount() != null ? post.getViewCount() : 0);
            double recencyHours = post.getCreatedAt() != null
                    ? Math.max(1, Duration.between(post.getCreatedAt(), now).toHours())
                    : 72.0;
            c.put("recency_hours", recencyHours);
            c.put("controversy_score",
                    tag != null && tag.getControversyScore() != null
                            ? tag.getControversyScore().doubleValue() : 0.0);
            c.put("cf_base_score", baseScores.getOrDefault(post.getId(), 0.5));
            c.put("position_original", i + 1);

            // 情感匹配特征（来自 2.1 + 2.2）
            c.put("emotion_state", state.getName());
            c.put("primary_tag", tag != null && tag.getPrimaryTag() != null
                    ? tag.getPrimaryTag() : "NEUTRAL_CALM");

            // 用户情感深度特征（来自 2.1 滑动窗口统计）
            c.put("user_avg_score", userAvgScore != null ? userAvgScore : 0.0);
            c.put("user_volatility", userVolatility != null ? userVolatility : 0.2);
            c.put("trend_type", trendType != null ? trendType : "STABLE");

            // 作者影响力特征（来自 2.3）
            c.put("author_influence",
                    authorInfluenceScores.getOrDefault(post.getUserId(), 0.5));

            candidates.add(c);
        }
        return candidates;
    }
}
