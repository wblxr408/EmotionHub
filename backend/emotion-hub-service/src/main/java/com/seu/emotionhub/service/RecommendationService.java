package com.seu.emotionhub.service;

import com.seu.emotionhub.model.dto.request.EmotionalRecommendationRequest;
import com.seu.emotionhub.model.dto.response.EmotionalRecommendationResponse;

/**
 * 推荐服务接口
 *
 * @author EmotionHub Team
 */
public interface RecommendationService {

    /**
     * 情感互补推荐
     */
    EmotionalRecommendationResponse recommendEmotional(EmotionalRecommendationRequest request);
}
