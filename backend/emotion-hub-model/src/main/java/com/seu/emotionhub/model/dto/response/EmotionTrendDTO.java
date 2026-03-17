package com.seu.emotionhub.model.dto.response;

import com.seu.emotionhub.model.enums.TrendTypeEnum;
import lombok.Data;

/**
 * 情感趋势结果（响应DTO）
 */
@Data
public class EmotionTrendDTO {
    /** 时间窗口 */
    private String timeWindow;
    /** 趋势类型 */
    private TrendTypeEnum trendType;
    /** 趋势幅度（分值变化） */
    private Integer amplitude;
}