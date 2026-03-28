package com.seu.emotionhub.service;

/**
 * A/B 测试服务接口
 *
 * @author EmotionHub Team
 */
public interface ABTestService {

    /**
     * 为用户分配 Feed 推荐策略
     * <p>
     * 同一用户在 TTL 周期内保持分组稳定，保证体验一致性。
     *
     * @param userId 用户ID
     * @return 分配到的策略：emotional_adaptive 或 traditional
     */
    String assignFeedStrategy(Long userId);

    /**
     * 解析请求策略：若客户端显式传入则优先，否则走 A/B 分配
     *
     * @param userId          用户ID
     * @param requestStrategy 请求参数中的策略，可为 null
     * @return 最终使用的策略
     */
    String resolveStrategy(Long userId, String requestStrategy);
}
