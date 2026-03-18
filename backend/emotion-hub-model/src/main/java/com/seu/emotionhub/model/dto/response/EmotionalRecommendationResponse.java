package com.seu.emotionhub.model.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 情感推荐响应
 *
 * @author EmotionHub Team
 */
@Data
public class EmotionalRecommendationResponse {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 使用的推荐策略
     */
    private String strategy;

    /**
     * 用户当前情感状态
     */
    private String emotionState;

    /**
     * 推荐条数
     */
    private Integer limit;

    /**
     * 候选数量
     */
    private Integer candidateCount;

    /**
     * 推荐结果
     */
    private List<PostVO> items;
}
