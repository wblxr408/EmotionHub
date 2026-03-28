package com.seu.emotionhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seu.emotionhub.common.enums.ErrorCode;
import com.seu.emotionhub.common.exception.BusinessException;
import com.seu.emotionhub.dao.mapper.NotificationMapper;
import com.seu.emotionhub.model.dto.response.NotificationVO;
import com.seu.emotionhub.model.dto.response.PageResult;
import com.seu.emotionhub.model.entity.Notification;
import com.seu.emotionhub.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;
import java.util.List;
import java.util.ArrayList;
/**
 * 通知服务实现类
 *
 * @author EmotionHub Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    @Async("taskExecutor")
    public void createNotification(Long userId, String type, String title, String content, Long relatedId) {
        try {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setType(type);
            notification.setTitle(title);
            notification.setContent(content);
            notification.setRelatedId(relatedId);
            notification.setIsRead(0); // 未读

            notificationMapper.insert(notification);

            log.info("创建通知成功: userId={}, type={}, title={}", userId, type, title);
        } catch (Exception e) {
            log.error("创建通知失败: userId={}, type={}", userId, type, e);
        }
    }

    @Override
    public PageResult<NotificationVO> listUnreadNotifications(Integer page, Integer size) {
        Long userId = getCurrentUserId();

        LambdaQueryWrapper<Notification> query = new LambdaQueryWrapper<>();
        query.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0)
                .orderByDesc(Notification::getCreatedAt);

        Page<Notification> notificationPage = notificationMapper.selectPage(new Page<>(page, size), query);

        List<NotificationVO> voList = notificationPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, notificationPage.getTotal(),
                (int) notificationPage.getCurrent(), (int) notificationPage.getSize());
    }

    @Override
    public PageResult<NotificationVO> listAllNotifications(Integer page, Integer size) {
        Long userId = getCurrentUserId();

        LambdaQueryWrapper<Notification> query = new LambdaQueryWrapper<>();
        query.eq(Notification::getUserId, userId)
                .orderByDesc(Notification::getCreatedAt);

        Page<Notification> notificationPage = notificationMapper.selectPage(new Page<>(page, size), query);

        List<NotificationVO> voList = notificationPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(voList, notificationPage.getTotal(),
                (int) notificationPage.getCurrent(), (int) notificationPage.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsRead(Long notificationId) {
        Long userId = getCurrentUserId();

        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "通知不存在");
        }

        // 权限校验
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        notification.setIsRead(1);
        notificationMapper.updateById(notification);

        log.info("标记通知已读: userId={}, notificationId={}", userId, notificationId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllAsRead() {
        Long userId = getCurrentUserId();

        Notification notification = new Notification();
        notification.setIsRead(1);
        LambdaQueryWrapper<Notification> query = new LambdaQueryWrapper<>();
        query.eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0);
        notificationMapper.update(notification, query);

        log.info("标记所有通知已读: userId={}", userId);
    }

    @Override
    public long getUnreadCount() {
        Long userId = getCurrentUserId();
        return notificationMapper.countUnread(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteNotification(Long notificationId) {
        Long userId = getCurrentUserId();

        Notification notification = notificationMapper.selectById(notificationId);
        if (notification == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "通知不存在");
        }

        // 权限校验
        if (!notification.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        notificationMapper.deleteById(notificationId);

        log.info("删除通知: userId={}, notificationId={}", userId, notificationId);
    }

    /**
     * 转换为VO
     */
    private NotificationVO convertToVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        BeanUtils.copyProperties(notification, vo);
        return vo;
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }

        throw new BusinessException(ErrorCode.TOKEN_INVALID);
    }
}
