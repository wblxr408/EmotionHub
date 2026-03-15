package com.seu.emotionhub.web.controller;

import com.seu.emotionhub.common.result.Result;
import com.seu.emotionhub.model.dto.response.UserInfluenceVO;
import com.seu.emotionhub.service.UserInfluenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户情感影响力Controller
 * 提供用户影响力评分查询、排行榜、趋势分析等API接口
 *
 * @author EmotionHub Team
 */
@Slf4j
@RestController
@RequestMapping("/api/influence")
@RequiredArgsConstructor
@Tag(name = "用户情感影响力", description = "用户影响力评分、排行榜、趋势分析")
public class UserInfluenceController {

    private final UserInfluenceService influenceService;

    // ==================== 用户影响力查询 ====================

    /**
     * 获取用户最新的影响力评分
     *
     * API路径: GET /api/influence/user/{userId}
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户最新影响力评分",
               description = "返回用户最新一次计算的影响力评分及各项指标")
    public Result<UserInfluenceVO> getLatestInfluence(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {

        log.info("查询用户最新影响力评分: userId={}", userId);
        try {
            UserInfluenceVO influence = influenceService.getLatestInfluence(userId);
            if (influence == null) {
                return Result.error("用户影响力数据不存在，请等待系统计算");
            }
            log.info("查询成功: userId={}, influenceScore={}", userId, influence.getInfluenceScore());
            return Result.success("查询成功", influence);
        } catch (Exception e) {
            log.error("查询用户影响力失败: userId={}", userId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户指定日期的影响力评分
     *
     * API路径: GET /api/influence/user/{userId}/history?date=2024-03-15
     */
    @GetMapping("/user/{userId}/history")
    @Operation(summary = "获取用户指定日期的影响力评分",
               description = "返回用户在指定日期的影响力评分")
    public Result<UserInfluenceVO> getInfluenceByDate(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "计算日期(yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        log.info("查询用户历史影响力: userId={}, date={}", userId, date);
        try {
            UserInfluenceVO influence = influenceService.getInfluenceByDate(userId, date);
            if (influence == null) {
                return Result.error("指定日期的影响力数据不存在");
            }
            return Result.success("查询成功", influence);
        } catch (Exception e) {
            log.error("查询用户历史影响力失败: userId={}, date={}", userId, date, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户影响力历史趋势
     *
     * API路径: GET /api/influence/user/{userId}/trend?startDate=2024-03-01&endDate=2024-03-31
     */
    @GetMapping("/user/{userId}/trend")
    @Operation(summary = "获取用户影响力历史趋势",
               description = "返回用户在指定时间范围内的影响力变化趋势")
    public Result<List<UserInfluenceVO>> getInfluenceTrend(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId,
            @Parameter(description = "开始日期(yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Parameter(description = "结束日期(yyyy-MM-dd)")
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {

        log.info("查询用户影响力趋势: userId={}, startDate={}, endDate={}", userId, startDate, endDate);
        try {
            List<UserInfluenceVO> trend = influenceService.getInfluenceTrend(userId, startDate, endDate);
            log.info("查询成功: userId={}, dataPoints={}", userId, trend.size());
            return Result.success("查询成功", trend);
        } catch (Exception e) {
            log.error("查询用户影响力趋势失败: userId={}", userId, e);
            return Result.error(e.getMessage());
        }
    }

    // ==================== 影响力排行榜 ====================

    /**
     * 获取综合影响力排行榜
     *
     * API路径: GET /api/influence/ranking/overall?limit=20
     */
    @GetMapping("/ranking/overall")
    @Operation(summary = "获取综合影响力排行榜",
               description = "返回综合影响力Top N用户列表，基于PageRank算法计算")
    public Result<List<UserInfluenceVO>> getTopInfluential(
            @Parameter(description = "返回数量限制（默认20）")
            @RequestParam(defaultValue = "20") Integer limit) {

        log.info("查询综合影响力排行榜: limit={}", limit);
        try {
            List<UserInfluenceVO> ranking = influenceService.getTopInfluential(limit);
            log.info("查询成功: resultCount={}", ranking.size());
            return Result.success("查询成功", ranking);
        } catch (Exception e) {
            log.error("查询综合影响力排行榜失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取正能量影响力排行榜
     *
     * API路径: GET /api/influence/ranking/positive?limit=20
     */
    @GetMapping("/ranking/positive")
    @Operation(summary = "获取正能量影响力排行榜",
               description = "返回引发正面评论最多的Top N用户列表")
    public Result<List<UserInfluenceVO>> getTopPositiveInfluence(
            @Parameter(description = "返回数量限制（默认20）")
            @RequestParam(defaultValue = "20") Integer limit) {

        log.info("查询正能量影响力排行榜: limit={}", limit);
        try {
            List<UserInfluenceVO> ranking = influenceService.getTopPositiveInfluence(limit);
            log.info("查询成功: resultCount={}", ranking.size());
            return Result.success("查询成功", ranking);
        } catch (Exception e) {
            log.error("查询正能量影响力排行榜失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取话题制造者排行榜（争议性）
     *
     * API路径: GET /api/influence/ranking/controversial?limit=20
     */
    @GetMapping("/ranking/controversial")
    @Operation(summary = "获取话题制造者排行榜",
               description = "返回引发评论区情感分化最大的Top N用户列表（争议性高）")
    public Result<List<UserInfluenceVO>> getTopControversial(
            @Parameter(description = "返回数量限制（默认20）")
            @RequestParam(defaultValue = "20") Integer limit) {

        log.info("查询话题制造者排行榜: limit={}", limit);
        try {
            List<UserInfluenceVO> ranking = influenceService.getTopControversial(limit);
            log.info("查询成功: resultCount={}", ranking.size());
            return Result.success("查询成功", ranking);
        } catch (Exception e) {
            log.error("查询话题制造者排行榜失败", e);
            return Result.error(e.getMessage());
        }
    }

    // ==================== 管理功能 ====================

    /**
     * 手动触发全量影响力计算
     *
     * API路径: POST /api/influence/recalculate
     *
     * 注意：此操作会重新计算所有活跃用户的影响力，耗时较长
     */
    @PostMapping("/recalculate")
    @Operation(summary = "手动触发全量影响力计算",
               description = "重新计算所有活跃用户的影响力评分，正常情况由每日定时任务自动完成")
    public Result<String> recalculateInfluence() {
        log.info("手动触发全量影响力计算");
        try {
            int count = influenceService.calculateAllUserInfluence();
            log.info("影响力计算完成，成功计算 {} 个用户", count);
            return Result.success("影响力计算完成，成功计算 " + count + " 个用户");
        } catch (Exception e) {
            log.error("影响力计算失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 计算单个用户的影响力
     *
     * API路径: POST /api/influence/calculate/{userId}
     */
    @PostMapping("/calculate/{userId}")
    @Operation(summary = "计算单个用户的影响力",
               description = "重新计算指定用户的影响力评分")
    public Result<UserInfluenceVO> calculateUserInfluence(
            @Parameter(description = "用户ID", required = true) @PathVariable Long userId) {

        log.info("计算单个用户影响力: userId={}", userId);
        try {
            influenceService.calculateUserInfluence(userId);
            UserInfluenceVO influence = influenceService.getLatestInfluence(userId);
            log.info("计算成功: userId={}, influenceScore={}", userId,
                    influence != null ? influence.getInfluenceScore() : null);
            return Result.success("计算成功", influence);
        } catch (Exception e) {
            log.error("计算用户影响力失败: userId={}", userId, e);
            return Result.error(e.getMessage());
        }
    }
}
