package com.seu.emotionhub.web.controller;

import com.seu.emotionhub.common.result.Result;
import com.seu.emotionhub.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 统计Controller
 * 处理用户和平台的统计数据查询
 *
 * @author EmotionHub Team
 */
@Slf4j
@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
@Tag(name = "统计分析", description = "用户和平台统计数据")
public class StatsController {

    private final StatsService statsService;

    /**
     * 获取我的统计信息
     */
    @GetMapping("/my")
    @Operation(summary = "我的统计", description = "获取当前用户的统计信息")
    public Result<Map<String, Object>> getMyStats() {
        log.info("获取我的统计信息");
        Map<String, Object> stats = statsService.getMyStats();
        return Result.success(stats);
    }

    /**
     * 获取指定用户的统计信息
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "用户统计", description = "获取指定用户的统计信息")
    public Result<Map<String, Object>> getUserStats(@PathVariable Long userId) {
        log.info("获取用户统计信息: userId={}", userId);
        Map<String, Object> stats = statsService.getUserStats(userId);
        return Result.success(stats);
    }

    /**
     * 获取平台统计信息
     */
    @GetMapping("/platform")
    @Operation(summary = "平台统计", description = "获取平台整体统计数据")
    public Result<Map<String, Object>> getPlatformStats() {
        log.info("获取平台统计信息");
        Map<String, Object> stats = statsService.getPlatformStats();
        return Result.success(stats);
    }
}
