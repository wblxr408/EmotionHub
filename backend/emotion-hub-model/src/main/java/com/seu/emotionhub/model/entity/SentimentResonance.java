package com.seu.emotionhub.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 情感共鸣关系实体类
 * 对应数据库的sentiment_resonance表
 * 用于存储用户之间的情感相似度和共鸣关系
 *
 * @author EmotionHub Team
 */
@Data
@TableName("sentiment_resonance")
public class SentimentResonance {

    /**
     * 共鸣关系ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户A的ID
     * 注意：user_a_id < user_b_id（避免重复记录）
     */
    @TableField("user_a_id")
    private Long userAId;

    /**
     * 用户B的ID
     */
    @TableField("user_b_id")
    private Long userBId;

    /**
     * 共鸣分数（0到1）
     * 基于余弦相似度计算
     */
    @TableField("resonance_score")
    private BigDecimal resonanceScore;

    /**
     * 互动次数
     * 包括评论、点赞等互动行为
     */
    @TableField("interaction_count")
    private Integer interactionCount;

    /**
     * 情感相似度
     * 基于历史情感向量计算（0到1）
     */
    @TableField("sentiment_similarity")
    private BigDecimal sentimentSimilarity;

    /**
     * 平均情感差异
     * 两个用户情感分数的平均绝对值差异
     */
    @TableField("avg_sentiment_diff")
    private BigDecimal avgSentimentDiff;

    /**
     * 共同主导情感标签
     * 如果两个用户都倾向于某种情感，记录该标签
     * POSITIVE / NEUTRAL / NEGATIVE
     */
    @TableField("common_emotion_label")
    private String commonEmotionLabel;

    /**
     * 所属情感社区ID
     * 通过Louvain算法进行社区发现
     * 相同community_id的用户属于同一个情感社区
     */
    @TableField("community_id")
    private Integer communityId;

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
