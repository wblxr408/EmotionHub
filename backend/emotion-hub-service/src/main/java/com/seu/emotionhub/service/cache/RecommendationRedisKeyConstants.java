package com.seu.emotionhub.service.cache;

/**
 * 推荐系统Redis Key常量
 *
 * @author EmotionHub Team
 */
public class RecommendationRedisKeyConstants {

    /**
     * 用户相似度ZSet（key: rec:sim:{userId}）
     */
    public static final String USER_SIMILARITY = "rec:sim:%s";

    /**
     * 用户推荐列表ZSet（key: rec:user:{userId}）
     */
    public static final String USER_RECOMMENDATIONS = "rec:user:%s";

    /**
     * 推荐缓存TTL（秒）
     */
    public static final long TTL_SECONDS = 24 * 3600;

    private RecommendationRedisKeyConstants() {
    }
}
