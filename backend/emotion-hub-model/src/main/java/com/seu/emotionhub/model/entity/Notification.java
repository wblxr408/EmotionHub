package com.seu.emotionhub.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知实体类
 * 对应数据库的notification表
 * 支持点赞、评论、系统通知等多种类型
 *
 * @author EmotionHub Team
 */
@Data
@TableName("notification")
public class Notification {

    /**
     * 通知ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 接收者ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 通知类型：LIKE-点赞 / COMMENT-评论 / SYSTEM-系统 / ANALYSIS_COMPLETE-分析完成
     */
    private String type;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 关联ID（帖子或评论的ID）
     * 用于跳转到具体内容
     */
    @TableField("related_id")
    private Long relatedId;

    /**
     * 是否已读：0-未读，1-已读
     */
    @TableField("is_read")
    private Integer isRead;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
