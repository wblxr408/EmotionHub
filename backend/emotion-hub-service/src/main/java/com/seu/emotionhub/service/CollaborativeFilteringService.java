package com.seu.emotionhub.service;

/**
 * 协同过滤服务接口
 *
 * @author EmotionHub Team
 */
public interface CollaborativeFilteringService {

    /**
     * 重建用户相似度与推荐结果
     */
    void rebuildRecommendations();
}
