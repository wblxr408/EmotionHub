package com.seu.emotionhub.web.controller;

import com.seu.emotionhub.common.result.Result;
import com.seu.emotionhub.model.dto.response.SentimentPropagationVO;
import com.seu.emotionhub.model.dto.response.SentimentTimelineVO;
import com.seu.emotionhub.service.SentimentPropagationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 情感传播分析Controller
 * 提供情感传播路径分析和时间序列分析的API接口
 *
 * @author EmotionHub Team
 */
@Slf4j
@RestController
@RequestMapping("/api/sentiment")
@RequiredArgsConstructor
@Tag(name = "情感传播分析", description = "情感传染分析、传播路径追踪、时间序列分析")
public class SentimentController {

    private final SentimentPropagationService propagationService;

    /**
     * 获取帖子的情感传播分析
     *
     * API路径: GET /api/sentiment/propagation/{postId}
     *
     * 功能说明：
     * - 分析评论区的情感传播路径
     * - 计算情感一致性（正能量帖子是否带来正面评论）
     * - 识别情感转折点（从正面转负面的节点）
     * - 统计情感放大系数和衰减率
     *
     * 返回数据包括：
     * - 传播类型（一致传播、放大传播、衰减传播、争议传播）
     * - 平均情感一致性
     * - 平均情感放大系数
     * - 情感转折点列表
     * - 完整的传播节点树
     */
    @GetMapping("/propagation/{postId}")
    @Operation(
        summary = "获取帖子的情感传播分析",
        description = "分析帖子评论区的情感传播路径，包括情感一致性、放大系数、转折点等指标"
    )
    public Result<SentimentPropagationVO> getPropagationAnalysis(
            @Parameter(description = "帖子ID", required = true)
            @PathVariable Long postId) {

        log.info("查询帖子情感传播分析: postId={}", postId);

        try {
            SentimentPropagationVO analysis = propagationService.getPropagationAnalysis(postId);

            log.info("情感传播分析完成: postId={}, propagationType={}, totalComments={}, shiftCount={}",
                    postId, analysis.getPropagationType(), analysis.getTotalComments(), analysis.getShiftCount());

            return Result.success("查询成功", analysis);

        } catch (RuntimeException e) {
            log.error("查询情感传播分析失败: postId={}", postId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取帖子的情感时间线
     *
     * API路径: GET /api/sentiment/timeline/{postId}
     *
     * 功能说明：
     * - 展示情感随时间的演变趋势
     * - 分析情感在评论链中的传播（1级评论 → 2级评论...）
     * - 计算情感波动率和趋势方向
     * - 统计各层级评论的情感特征
     *
     * 返回数据包括：
     * - 情感趋势（上升、下降、稳定、波动）
     * - 情感波动率（标准差）
     * - 时间序列数据点（每条评论的时间和情感）
     * - 按层级统计的情感数据
     */
    @GetMapping("/timeline/{postId}")
    @Operation(
        summary = "获取帖子的情感时间线",
        description = "展示帖子情感随时间的演变趋势，包括情感波动、趋势方向、层级分析等"
    )
    public Result<SentimentTimelineVO> getSentimentTimeline(
            @Parameter(description = "帖子ID", required = true)
            @PathVariable Long postId) {

        log.info("查询帖子情感时间线: postId={}", postId);

        try {
            SentimentTimelineVO timeline = propagationService.getSentimentTimeline(postId);

            log.info("情感时间线查询完成: postId={}, trend={}, volatility={}, pointsCount={}",
                    postId, timeline.getSentimentTrend(), timeline.getVolatility(),
                    timeline.getTimelinePoints() != null ? timeline.getTimelinePoints().size() : 0);

            return Result.success("查询成功", timeline);

        } catch (RuntimeException e) {
            log.error("查询情感时间线失败: postId={}", postId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 重新计算帖子的情感传播数据
     *
     * API路径: POST /api/sentiment/recalculate/{postId}
     *
     * 功能说明：
     * - 删除现有的传播记录
     * - 重新分析所有评论的情感传播
     * - 用于修复历史数据或更新算法后重新计算
     *
     * 注意：此操作会重新分析该帖子的所有评论，可能需要较长时间
     */
    @PostMapping("/recalculate/{postId}")
    @Operation(
        summary = "重新计算帖子的情感传播数据",
        description = "删除现有传播记录并重新分析，用于修复历史数据或算法更新后重新计算"
    )
    public Result<String> recalculatePropagation(
            @Parameter(description = "帖子ID", required = true)
            @PathVariable Long postId) {

        log.info("重新计算帖子情感传播: postId={}", postId);

        try {
            propagationService.recalculatePropagation(postId);

            log.info("情感传播重新计算完成: postId={}", postId);

            return Result.success("重新计算成功");

        } catch (RuntimeException e) {
            log.error("重新计算情感传播失败: postId={}", postId, e);
            return Result.error(e.getMessage());
        }
    }
}
