package com.seu.emotionhub.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户情感统计实体类
 * 对应数据库的user_emotion_stats表
 * 用于每日情感数据汇总和趋势分析
 *
 * @author EmotionHub Team
 */
@Data
@TableName("user_emotion_stats")
public class UserEmotionStats {

    /**
     * 统计ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 统计日期
     */
    private LocalDate date;

    /**
     * 积极情绪数
     */
    @TableField("positive_count")
    private Integer positiveCount;

    /**
     * 中性情绪数
     */
    @TableField("neutral_count")
    private Integer neutralCount;

    /**
     * 消极情绪数
     */
    @TableField("negative_count")
    private Integer negativeCount;

    /**
     * 平均情感分数
     */
    @TableField("avg_emotion_score")
    private BigDecimal avgEmotionScore;

    /**
     * 总帖子数
     */
    @TableField("total_posts")
    private Integer totalPosts;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
