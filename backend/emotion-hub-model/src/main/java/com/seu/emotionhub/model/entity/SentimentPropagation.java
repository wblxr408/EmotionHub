package com.seu.emotionhub.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 情感传播关系实体类
 * 对应数据库的sentiment_propagation表
 * 用于追踪帖子和评论之间的情感传播路径
 *
 * @author EmotionHub Team
 */
@Data
@TableName("sentiment_propagation")
public class SentimentPropagation {

    /**
     * 传播记录ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 原始帖子ID
     */
    @TableField("post_id")
    private Long postId;

    /**
     * 评论ID
     */
    @TableField("comment_id")
    private Long commentId;

    /**
     * 评论用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 父评论ID（用于追踪评论链）
     * null表示一级评论
     */
    @TableField("parent_comment_id")
    private Long parentCommentId;

    /**
     * 评论层级（1表示一级评论）
     */
    @TableField("depth_level")
    private Integer depthLevel;

    /**
     * 原帖情感分数
     */
    @TableField("post_sentiment_score")
    private BigDecimal postSentimentScore;

    /**
     * 评论情感分数
     */
    @TableField("comment_sentiment_score")
    private BigDecimal commentSentimentScore;

    /**
     * 情感一致性（-1到1，1表示完全一致）
     * 计算方式：1 - |post_score - comment_score| / 2
     */
    @TableField("sentiment_consistency")
    private BigDecimal sentimentConsistency;

    /**
     * 情感放大系数（评论强度/原帖强度）
     * 用于判断情感是否被放大或衰减
     */
    @TableField("sentiment_amplification")
    private BigDecimal sentimentAmplification;

    /**
     * 是否发生情感转折
     * 0-否，1-是（从正面转负面或从负面转正面）
     */
    @TableField("is_sentiment_shift")
    private Boolean isSentimentShift;

    /**
     * 转折方向
     * POSITIVE_TO_NEGATIVE / NEGATIVE_TO_POSITIVE / NULL
     */
    @TableField("shift_direction")
    private String shiftDirection;

    /**
     * 记录创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
