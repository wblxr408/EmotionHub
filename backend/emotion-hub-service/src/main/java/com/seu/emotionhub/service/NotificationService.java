package com.seu.emotionhub.service;

import com.seu.emotionhub.model.dto.response.NotificationVO;
import com.seu.emotionhub.model.dto.response.PageResult;

/**
 * 通知服务接口
 *
 * @author EmotionHub Team
 */
public interface NotificationService {

    /**
     * 创建通知（异步）
     *
     * @param userId    接收者ID
     * @param type      通知类型
     * @param title     通知标题
     * @param content   通知内容
     * @param relatedId 关联ID
     */
    void createNotification(Long userId, String type, String title, String content, Long relatedId);

    /**
     * 获取未读通知列表
     *
     * @param page 页码
     * @param size 每页数量
     * @return 通知列表
     */
    PageResult<NotificationVO> listUnreadNotifications(Integer page, Integer size);

    /**
     * 获取所有通知列表
     *
     * @param page 页码
     * @param size 每页数量
     * @return 通知列表
     */
    PageResult<NotificationVO> listAllNotifications(Integer page, Integer size);

    /**
     * 标记通知为已读
     *
     * @param notificationId 通知ID
     */
    void markAsRead(Long notificationId);

    /**
     * 标记所有通知为已读
     */
    void markAllAsRead();

    /**
     * 获取未读通知数量
     *
     * @return 未读数量
     */
    long getUnreadCount();

    /**
     * 删除通知
     *
     * @param notificationId 通知ID
     */
    void deleteNotification(Long notificationId);
}
