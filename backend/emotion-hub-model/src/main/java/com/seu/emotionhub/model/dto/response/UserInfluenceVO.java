package com.seu.emotionhub.model.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户影响力响应VO（占位符）
 * TODO: 后续实现完整的用户影响力分析功能
 *
 * @author EmotionHub Team
 */
@Data
public class UserInfluenceVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 影响力得分
     */
    private BigDecimal influenceScore;

    /**
     * 排名
     */
    private Integer rank;
}
