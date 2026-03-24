package com.seu.emotionhub.model.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知详情VO
 *
 * @author EmotionHub Team
 */
@Data
public class NotificationVO {

    /**
     * 通知ID
     */
    private Long id;

    /**
     * 通知类型
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
     */
    private Long relatedId;

    /**
     * 是否已读
     */
    private Integer isRead;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
