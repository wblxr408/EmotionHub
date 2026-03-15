package com.seu.emotionhub.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 点赞记录实体类
 * 对应数据库的like_record表
 *
 * @author EmotionHub Team
 */
@Data
@TableName("like_record")
public class LikeRecord {

    /**
     * 点赞记录ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（点赞者）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 目标ID（帖子或评论的ID）
     */
    @TableField("target_id")
    private Long targetId;

    /**
     * 目标类型：POST-帖子 / COMMENT-评论
     */
    @TableField("target_type")
    private String targetType;

    /**
     * 创建时间（点赞时间）
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
