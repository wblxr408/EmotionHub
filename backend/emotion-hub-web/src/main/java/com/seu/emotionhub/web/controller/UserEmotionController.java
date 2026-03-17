package com.seu.emotionhub.web.controller;

import com.seu.emotionhub.common.result.Result;
import com.seu.emotionhub.model.dto.response.EmotionStatsDTO;
import com.seu.emotionhub.model.entity.UserEmotionHistory;
import com.seu.emotionhub.service.UserEmotionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户情感状态追踪 Controller
 * Sprint 2.1 - 用户情感状态追踪功能
 *
 * 提供以下功能：
 * 1. 实时情感状态查询
 * 2. 滑动窗口统计（1小时、24小时、7天）
 * 3. 情感趋势判断（上升/下降/稳定）
 * 4. 情感历史记录查询
 *
 * @author EmotionHub Team
 */
@Slf4j
@RestController
@RequestMapping("/api/emotion")
@RequiredArgsConstructor
@Tag(name = "用户情感追踪", description = "用户情感状态追踪与分析")
public class UserEmotionController {

    private final UserEmotionService userEmotionService;

    /**
     * 获取用户当前情感状态
     *
     * API路径: GET /api/emotion/state/{userId}
     *
     * @param userId 用户ID
     * @return 当前情感状态（HAPPY/CALM/LOW/ANXIOUS/FLUCTUANT）
     */
    @GetMapping("/state/{userId}")
    @Operation(summary = "获取用户当前情感状态", description = "返回用户当前的情感状态（基于最近1小时数据计算）")
    public Result<Map<String, Object>> getUserEmotionState(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId) {
        log.info("[用户情感状态查询] userId={}", userId);

        String emotionState = userEmotionService.matchEmotionState(userId).getName();
        EmotionStatsDTO stats = userEmotionService.calculateSlidingWindowStats(userId, "1h");

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("emotionState", emotionState);
        result.put("avgScore", stats.getAvgScore());
        result.put("volatility", stats.getVolatility());
        result.put("updateTime", System.currentTimeMillis());

        return Result.success(result);
    }

    /**
     * 获取用户情感统计（滑动窗口）
     *
     * API路径: GET /api/emotion/stats/{userId}?window=1h|24h|7d
     *
     * @param userId 用户ID
     * @param window 时间窗口（1h-1小时，24h-24小时，7d-7天，默认24h）
     * @return 滑动窗口统计结果
     */
    @GetMapping("/stats/{userId}")
    @Operation(summary = "获取用户情感统计", description = "基于滑动窗口计算用户情感统计数据")
    public Result<EmotionStatsDTO> getUserEmotionStats(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "时间窗口: 1h(1小时) | 24h(24小时) | 7d(7天)", example = "24h")
            @RequestParam(defaultValue = "24h") String window) {
        log.info("[用户情感统计查询] userId={}, window={}", userId, window);

        // 验证窗口参数
        if (!window.matches("^(1h|24h|7d)$")) {
            return Result.error("时间窗口参数错误，仅支持: 1h, 24h, 7d");
        }

        EmotionStatsDTO stats = userEmotionService.calculateSlidingWindowStats(userId, window);
        return Result.success(stats);
    }

    /**
     * 获取用户情感趋势
     *
     * API路径: GET /api/emotion/trend/{userId}?window=24h|7d
     *
     * @param userId 用户ID
     * @param window 时间窗口（24h-24小时，7d-7天，默认24h）
     * @return 情感趋势（上升/下降/稳定）及趋势说明
     */
    @GetMapping("/trend/{userId}")
    @Operation(summary = "获取用户情感趋势", description = "分析用户情感变化趋势（上升/下降/稳定）")
    public Result<Map<String, Object>> getUserEmotionTrend(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "时间窗口: 24h(24小时) | 7d(7天)", example = "24h")
            @RequestParam(defaultValue = "24h") String window) {
        log.info("[用户情感趋势查询] userId={}, window={}", userId, window);

        // 验证窗口参数
        if (!window.matches("^(1h|24h|7d)$")) {
            return Result.error("时间窗口参数错误，仅支持: 1h, 24h, 7d");
        }

        String trend = userEmotionService.judgeEmotionTrend(userId, window);
        EmotionStatsDTO stats = userEmotionService.calculateSlidingWindowStats(userId, window);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("timeWindow", window);
        result.put("trend", trend);
        result.put("avgScore", stats.getAvgScore());
        result.put("trendDescription", generateTrendDescription(trend, stats));

        return Result.success(result);
    }

    /**
     * 获取用户情感历史记录
     *
     * API路径: GET /api/emotion/history/{userId}?startTime=xxx&endTime=xxx
     *
     * @param userId 用户ID
     * @param startTime 开始时间戳（毫秒，可选）
     * @param endTime 结束时间戳（毫秒，可选）
     * @return 情感历史记录列表
     */
    @GetMapping("/history/{userId}")
    @Operation(summary = "获取用户情感历史", description = "查询指定时间范围内的情感历史记录")
    public Result<List<UserEmotionHistory>> getUserEmotionHistory(
            @Parameter(description = "用户ID", required = true)
            @PathVariable Long userId,
            @Parameter(description = "开始时间戳（毫秒）", example = "1710518400000")
            @RequestParam(required = false) Long startTime,
            @Parameter(description = "结束时间戳（毫秒）", example = "1710604800000")
            @RequestParam(required = false) Long endTime) {
        log.info("[用户情感历史查询] userId={}, startTime={}, endTime={}", userId, startTime, endTime);

        // 默认查询最近7天
        if (startTime == null || endTime == null) {
            endTime = System.currentTimeMillis();
            startTime = endTime - (7 * 24 * 3600 * 1000L);
        }

        List<UserEmotionHistory> history = userEmotionService.getEmotionHistory(userId, startTime, endTime);
        return Result.success(history);
    }

    /**
     * 保存情感记录（内部调用接口）
     *
     * API路径: POST /api/emotion/record
     *
     * @param history 情感历史记录
     * @return 操作结果
     */
    @PostMapping("/record")
    @Operation(summary = "保存情感记录", description = "记录用户的情感数据（内部调用）")
    public Result<Void> saveEmotionRecord(@RequestBody UserEmotionHistory history) {
        log.info("[保存情感记录] userId={}, score={}, source={}",
                history.getUserId(), history.getSentimentScore(), history.getSource());

        // 设置时间戳
        if (history.getTimestamp() == null) {
            history.setTimestamp(System.currentTimeMillis());
        }

        userEmotionService.saveEmotionRecord(history);
        return Result.success();
    }

    /**
     * 批量获取用户情感状态
     *
     * API路径: POST /api/emotion/states/batch
     *
     * @param userIds 用户ID列表
     * @return 用户ID到情感状态的映射
     */
    @PostMapping("/states/batch")
    @Operation(summary = "批量获取用户情感状态", description = "批量查询多个用户的当前情感状态")
    public Result<Map<Long, String>> batchGetUserEmotionStates(
            @Parameter(description = "用户ID列表", required = true)
            @RequestBody List<Long> userIds) {
        log.info("[批量查询用户情感状态] userIds={}", userIds);

        Map<Long, String> stateMap = new HashMap<>();
        for (Long userId : userIds) {
            String state = userEmotionService.matchEmotionState(userId).getName();
            stateMap.put(userId, state);
        }

        return Result.success(stateMap);
    }

    /**
     * 生成趋势描述
     */
    private String generateTrendDescription(String trend, EmotionStatsDTO stats) {
        String stateDesc = stats.getEmotionState();
        double avgScore = stats.getAvgScore() != null ? stats.getAvgScore() : 0.0;

        switch (trend) {
            case "上升":
                return String.format("情感趋势向好，当前状态: %s，平均分: %.2f", stateDesc, avgScore);
            case "下降":
                return String.format("情感趋势下滑，当前状态: %s，平均分: %.2f", stateDesc, avgScore);
            case "稳定":
            default:
                return String.format("情感保持稳定，当前状态: %s，平均分: %.2f", stateDesc, avgScore);
        }
    }
}
