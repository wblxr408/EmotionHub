package com.seu.emotionhub.web.controller;

import com.seu.emotionhub.common.result.Result;
import com.seu.emotionhub.model.dto.request.ReportHandleRequest;
import com.seu.emotionhub.model.dto.request.StatusUpdateRequest;
import com.seu.emotionhub.model.dto.response.AdminDashboardOverviewVO;
import com.seu.emotionhub.model.dto.response.AdminOperationLogVO;
import com.seu.emotionhub.model.dto.response.AdminReportVO;
import com.seu.emotionhub.model.dto.response.PageResult;
import com.seu.emotionhub.model.dto.response.PostVO;
import com.seu.emotionhub.model.dto.response.UserInfoVO;
import com.seu.emotionhub.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理后台接口
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "管理后台", description = "管理员后台相关接口")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    @Operation(summary = "用户列表", description = "管理员分页查询用户")
    public Result<PageResult<UserInfoVO>> listUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return Result.success(adminService.listUsers(page, size, keyword, status));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "用户详情", description = "管理员查看用户详情")
    public Result<UserInfoVO> getUserDetail(@PathVariable Long userId) {
        return Result.success(adminService.getUserDetail(userId));
    }

    @PutMapping("/users/{userId}/status")
    @Operation(summary = "更新用户状态", description = "管理员封禁或解封用户")
    public Result<Void> updateUserStatus(@PathVariable Long userId, @Valid @RequestBody StatusUpdateRequest request) {
        adminService.updateUserStatus(userId, request.getStatus());
        return Result.success("用户状态更新成功", null);
    }

    @GetMapping("/posts")
    @Operation(summary = "帖子列表", description = "管理员分页查询帖子")
    public Result<PageResult<PostVO>> listPosts(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId) {
        return Result.success(adminService.listPosts(page, size, keyword, status, userId));
    }

    @PutMapping("/posts/{postId}/status")
    @Operation(summary = "更新帖子状态", description = "管理员下架或恢复帖子")
    public Result<Void> updatePostStatus(@PathVariable Long postId, @Valid @RequestBody StatusUpdateRequest request) {
        adminService.updatePostStatus(postId, request.getStatus());
        return Result.success("帖子状态更新成功", null);
    }

    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "删除评论", description = "管理员删除违规评论")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        adminService.deleteComment(commentId);
        return Result.success("评论删除成功", null);
    }

    @GetMapping("/reports")
    @Operation(summary = "举报列表", description = "管理员分页查询举报")
    public Result<PageResult<AdminReportVO>> listReports(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String targetType) {
        return Result.success(adminService.listReports(page, size, status, targetType));
    }

    @GetMapping("/reports/{reportId}")
    @Operation(summary = "举报详情", description = "管理员查看举报详情")
    public Result<AdminReportVO> getReportDetail(@PathVariable Long reportId) {
        return Result.success(adminService.getReportDetail(reportId));
    }

    @PutMapping("/reports/{reportId}/handle")
    @Operation(summary = "处理举报", description = "管理员处理举报并可联动内容处置")
    public Result<Void> handleReport(@PathVariable Long reportId, @Valid @RequestBody ReportHandleRequest request) {
        adminService.handleReport(reportId, request);
        return Result.success("举报处理成功", null);
    }

    @GetMapping("/dashboard/overview")
    @Operation(summary = "后台概览", description = "管理员查看后台核心统计")
    public Result<AdminDashboardOverviewVO> getDashboardOverview() {
        return Result.success(adminService.getDashboardOverview());
    }

    @GetMapping("/operation-logs")
    @Operation(summary = "操作日志", description = "管理员分页查询关键操作日志")
    public Result<PageResult<AdminOperationLogVO>> listOperationLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) String action) {
        return Result.success(adminService.listOperationLogs(page, size, operatorId, action));
    }
}
