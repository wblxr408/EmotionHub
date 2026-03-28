package com.seu.emotionhub.service;

import com.seu.emotionhub.model.dto.response.FeedResponse;

/**
 * Feed 流服务接口
 *
 * @author EmotionHub Team
 */
public interface FeedService {

    /**
     * 生成个性化 Feed 流
     *
     * @param userId   用户ID
     * @param strategy 策略（emotional_adaptive / traditional），传 null 则由 A/B 测试决定
     * @param page     页码（从0开始）
     * @param size     每页数量
     * @return Feed 响应
     */
    FeedResponse generateFeed(Long userId, String strategy, int page, int size);

    /**
     * 记录帖子点击（供 A/B 测试 CTR 统计）
     *
     * @param logId 推荐日志ID
     */
    void recordClick(Long logId);
}
