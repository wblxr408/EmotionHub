package com.seu.emotionhub.web.controller;

import com.seu.emotionhub.common.result.Result;
import com.seu.emotionhub.model.dto.response.NotificationVO;
import com.seu.emotionhub.model.dto.response.PageResult;
import com.seu.emotionhub.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知Controller
 * 处理用户通知的查询、标记已读等操作
 *
 * @author EmotionHub Team
 */
@Slf4j
@RestController
@RequestMapping("/api/notification")
@RequiredArgsConstructor
@Tag(name = "通知管理", description = "用户通知相关功能")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 获取未读通知列表
     */
    @GetMapping("/unread")
    @Operation(summary = "获取未读通知", description = "分页获取当前用户的未读通知")
    public Result<PageResult<NotificationVO>> listUnreadNotifications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("获取未读通知: page={}, size={}", page, size);
        PageResult<NotificationVO> result = notificationService.listUnreadNotifications(page, size);
        return Result.success(result);
    }

    /**
     * 获取所有通知列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有通知", description = "分页获取当前用户的所有通知")
    public Result<PageResult<NotificationVO>> listAllNotifications(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("获取所有通知: page={}, size={}", page, size);
        PageResult<NotificationVO> result = notificationService.listAllNotifications(page, size);
        return Result.success(result);
    }

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unread/count")
    @Operation(summary = "获取未读数量", description = "获取当前用户的未读通知数量")
    public Result<Map<String, Long>> getUnreadCount() {
        long count = notificationService.getUnreadCount();
        Map<String, Long> data = new HashMap<>();
        data.put("count", count);
        return Result.success(data);
    }

    /**
     * 标记通知为已读
     */
    @PutMapping("/read/{notificationId}")
    @Operation(summary = "标记已读", description = "将指定通知标记为已读")
    public Result<Void> markAsRead(@PathVariable Long notificationId) {
        log.info("标记通知已读: notificationId={}", notificationId);
        notificationService.markAsRead(notificationId);
        return Result.success("标记成功", null);
    }

    /**
     * 标记所有通知为已读
     */
    @PutMapping("/read/all")
    @Operation(summary = "全部已读", description = "将所有通知标记为已读")
    public Result<Void> markAllAsRead() {
        log.info("标记所有通知已读");
        notificationService.markAllAsRead();
        return Result.success("标记成功", null);
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{notificationId}")
    @Operation(summary = "删除通知", description = "删除指定通知")
    public Result<Void> deleteNotification(@PathVariable Long notificationId) {
        log.info("删除通知: notificationId={}", notificationId);
        notificationService.deleteNotification(notificationId);
        return Result.success("删除成功", null);
    }
}
