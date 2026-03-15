package com.emotionhub.model.dto.response;

import lombok.Data;

/**
 * 滑动窗口情感统计结果（响应DTO）
 */
@Data
public class EmotionStatsDTO {
    /** 用户ID */
    private Long userId;
    /** 时间窗口（1h/24h/7d） */
    private String timeWindow;
    /** 平均分 */
    private Double avgScore;
    /** 最高分 */
    private Integer maxScore;
    /** 最低分 */
    private Integer minScore;
    /** 波动性（标准差/均值） */
    private Double volatility;
    /** 情感状态 */
    private String emotionState;
    /** 趋势类型（上升/下降/稳定） */
    private String trendType;
}