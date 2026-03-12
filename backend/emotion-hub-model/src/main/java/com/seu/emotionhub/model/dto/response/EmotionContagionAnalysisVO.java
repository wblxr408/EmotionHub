package com.seu.emotionhub.model.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 情感传染分析响应VO（占位符）
 * TODO: 后续实现完整的情感传染分析功能
 *
 * @author EmotionHub Team
 */
@Data
public class EmotionContagionAnalysisVO {

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 情感传染得分
     */
    private BigDecimal contagionScore;

    /**
     * 传播趋势
     */
    private String trend;
}
