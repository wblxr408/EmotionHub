package com.seu.emotionhub.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户情感影响力实体类
 * 对应数据库的user_influence_score表
 * 用于存储用户的情感影响力评分
 *
 * @author EmotionHub Team
 */
@Data
@TableName("user_influence_score")
public class UserInfluenceScore {

    /**
     * 影响力记录ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 综合影响力分数
     * 通过PageRank变体算法计算
     */
    @TableField("influence_score")
    private BigDecimal influenceScore;

    /**
     * 正面影响力
     * 引发正面评论的能力
     */
    @TableField("positive_impact")
    private BigDecimal positiveImpact;

    /**
     * 负面影响力
     * 引发负面评论的能力
     */
    @TableField("negative_impact")
    private BigDecimal negativeImpact;

    /**
     * 争议性分数
     * 引发情感分化的程度（评论区情感分歧大）
     */
    @TableField("controversial_score")
    private BigDecimal controversialScore;

    /**
     * 统计期内帖子数
     */
    @TableField("post_count")
    private Integer postCount;

    /**
     * 统计期内评论数
     */
    @TableField("comment_count")
    private Integer commentCount;

    /**
     * 平均互动深度
     * 评论链平均层级（越深说明引发的讨论越激烈）
     */
    @TableField("avg_engagement_depth")
    private BigDecimal avgEngagementDepth;

    /**
     * 情感改变率
     * 能够改变他人情感的比例
     */
    @TableField("sentiment_change_rate")
    private BigDecimal sentimentChangeRate;

    /**
     * 计算日期
     */
    @TableField("calculation_date")
    private LocalDate calculationDate;

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
