package com.seu.emotionhub.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 内容情感标签实体类
 * 对应数据库的content_emotion_tags表
 *
 * @author EmotionHub Team
 */
@Data
@TableName("content_emotion_tags")
public class ContentEmotionTag {

    /**
     * 标签记录ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 帖子ID
     */
    @TableField("post_id")
    private Long postId;

    /**
     * 主标签
     */
    @TableField("primary_tag")
    private String primaryTag;

    /**
     * 标签列表（JSON数组字符串）
     */
    private String tags;

    /**
     * 帖子情感分数
     */
    @TableField("sentiment_score")
    private BigDecimal sentimentScore;

    /**
     * 争议性分数（标准差）
     */
    @TableField("controversy_score")
    private BigDecimal controversyScore;

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
