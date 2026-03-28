package com.seu.emotionhub.service.cache;

/**
 * Feed Redis Key 常量
 *
 * @author EmotionHub Team
 */
public class FeedRedisKeyConstants {

    /**
     * Feed 缓存（key: feed:{userId}:{strategy}:{page}）
     * 存储分页后的帖子ID列表，短 TTL 保证新鲜度
     */
    public static final String FEED_CACHE = "feed:%s:%s:%d";

    /**
     * A/B 测试分组（key: ab:feed:{userId}）
     * 存储用户被分配到的策略，保证同一用户体验一致
     */
    public static final String AB_TEST_BUCKET = "ab:feed:%s";

    /** Feed 缓存 TTL：5分钟 */
    public static final long FEED_TTL_SECONDS = 5 * 60;

    /** A/B 分组 TTL：7天 */
    public static final long AB_TTL_SECONDS = 7 * 24 * 3600;

    private FeedRedisKeyConstants() {
    }
}
