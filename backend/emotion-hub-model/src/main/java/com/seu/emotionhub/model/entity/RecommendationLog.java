package com.seu.emotionhub.model.entity;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 推荐日志实体 - 用于 A/B 测试指标采集
 *
 * @author EmotionHub Team
 */
@Data
public class RecommendationLog {

    private Long id;

    /** 用户ID */
    private Long userId;

    /** 推荐的帖子ID */
    private Long postId;

    /** 推荐策略: emotional_adaptive / traditional */
    private String strategy;

    /** 推荐时用户情感状态 */
    private String emotionState;

    /** 排序分数 */
    private Double score;

    /** 在 Feed 中的位置（从1开始） */
    private Integer position;

    /** 曝光时间 */
    private LocalDateTime impressedAt;

    /** 是否点击 */
    private Boolean clicked;

    /** 点击时间 */
    private LocalDateTime clickedAt;
}
