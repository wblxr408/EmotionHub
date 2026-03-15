package com.seu.emotionhub.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评论实体类
 * 对应数据库的comment表
 * 支持树形结构（嵌套评论）
 *
 * @author EmotionHub Team
 */
@Data
@TableName("comment")
public class Comment {

    /**
     * 评论ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 帖子ID
     */
    @TableField("post_id")
    private Long postId;

    /**
     * 用户ID（评论者）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 父评论ID（支持嵌套）
     * 为null表示一级评论，不为null表示回复某条评论
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 情感分数：-1.00 到 1.00
     */
    @TableField("emotion_score")
    private BigDecimal emotionScore;

    /**
     * 情感标签：POSITIVE-积极 / NEUTRAL-中性 / NEGATIVE-消极
     */
    @TableField("emotion_label")
    private String emotionLabel;

    /**
     * 点赞数
     */
    @TableField("like_count")
    private Integer likeCount;

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
