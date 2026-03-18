package com.seu.emotionhub.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 情感推荐请求
 *
 * @author EmotionHub Team
 */
@Data
public class EmotionalRecommendationRequest {

    /**
     * 用户ID
     */
    @NotNull
    private Long userId;

    /**
     * 推荐策略：complementary|similar
     */
    private String strategy;

    /**
     * 推荐条数
     */
    private Integer limit;
}
