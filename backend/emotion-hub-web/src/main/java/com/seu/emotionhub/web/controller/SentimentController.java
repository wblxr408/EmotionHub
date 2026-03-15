package com.seu.emotionhub.web.controller;

import com.seu.emotionhub.common.result.Result;
import com.seu.emotionhub.model.dto.response.SentimentCommunityVO;
import com.seu.emotionhub.model.dto.response.SentimentNetworkVO;
import com.seu.emotionhub.model.dto.response.SentimentPropagationVO;
import com.seu.emotionhub.model.dto.response.SentimentTimelineVO;
import com.seu.emotionhub.service.SentimentPropagationService;
import com.seu.emotionhub.service.SentimentResonanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 情感传播分析Controller
 * 提供情感传播路径分析、时间序列分析、共鸣网络图、社区发现的 API 接口
 *
 * @author EmotionHub Team
 */
@Slf4j
@RestController
@RequestMapping("/api/sentiment")
@RequiredArgsConstructor
@Tag(name = "情感传播分析", description = "情感传染分析、传播路径追踪、时间序列分析、共鸣网络图、社区发现")
public class SentimentController {

    private final SentimentPropagationService propagationService;
    private final SentimentResonanceService resonanceService;

    // ==================== 情感传播分析（1.2） ====================

    @GetMapping("/propagation/{postId}")
    @Operation(summary = "获取帖子的情感传播分析",
               description = "分析帖子评论区的情感传播路径，包括情感一致性、放大系数、转折点等指标")
    public Result<SentimentPropagationVO> getPropagationAnalysis(
            @Parameter(description = "帖子ID", required = true) @PathVariable Long postId) {

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

    @GetMapping("/timeline/{postId}")
    @Operation(summary = "获取帖子的情感时间线",
               description = "展示帖子情感随时间的演变趋势，包括情感波动、趋势方向、层级分析等")
    public Result<SentimentTimelineVO> getSentimentTimeline(
            @Parameter(description = "帖子ID", required = true) @PathVariable Long postId) {

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

    @PostMapping("/recalculate/{postId}")
    @Operation(summary = "重新计算帖子的情感传播数据",
               description = "删除现有传播记录并重新分析，用于修复历史数据或算法更新后重新计算")
    public Result<String> recalculatePropagation(
            @Parameter(description = "帖子ID", required = true) @PathVariable Long postId) {

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

    // ==================== 情感共鸣网络图（1.4） ====================

    /**
     * 获取用户情感共鸣网络图
     *
     * API路径: GET /api/sentiment/network?userId={id}&depth=2
     *
     * 功能说明：
     * - 以指定用户为中心，BFS 扩展至 depth 层邻居
     * - 每条边代表两用户之间的情感共鸣关系（余弦相似度归一化后 >= 0.3）
     * - 节点包含用户昵称、主导情感、所属社区等信息
     * - 边包含共鸣分数、情感相似度等信息
     *
     * 返回数据供 D3.js / ECharts 直接渲染：
     * - nodes: 用户节点列表
     * - edges: 共鸣关系边列表
     * - stats: 网络整体统计
     */
    @GetMapping("/network")
    @Operation(summary = "获取用户情感共鸣网络图",
               description = "以指定用户为中心，BFS 扩展生成情感共鸣关系图，可供 D3.js/ECharts 直接渲染")
    public Result<SentimentNetworkVO> getResonanceNetwork(
            @Parameter(description = "中心用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "展开深度（1-3，默认 2）") @RequestParam(defaultValue = "2") Integer depth) {

        log.info("查询情感共鸣网络图: userId={}, depth={}", userId, depth);
        try {
            SentimentNetworkVO network = resonanceService.getResonanceNetwork(userId, depth);
            log.info("情感共鸣网络查询完成: userId={}, nodes={}, edges={}",
                    userId,
                    network.getNodes() != null ? network.getNodes().size() : 0,
                    network.getEdges() != null ? network.getEdges().size() : 0);
            return Result.success("查询成功", network);
        } catch (RuntimeException e) {
            log.error("查询情感共鸣网络失败: userId={}", userId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取情感社区列表
     *
     * API路径: GET /api/sentiment/communities
     *
     * 功能说明：
     * - 返回通过 Louvain 算法发现的所有情感社区
     * - 每个社区包含：社区类型（乐观派/理性派/悲观派）、成员列表、统计信息
     * - 社区发现基于最新一次计算结果（每周日 2:00 自动更新）
     */
    @GetMapping("/communities")
    @Operation(summary = "获取情感社区列表",
               description = "返回通过 Louvain 算法发现的情感用户社区，含社区类型标签和成员信息")
    public Result<List<SentimentCommunityVO>> getSentimentCommunities() {
        log.info("查询情感社区列表");
        try {
            List<SentimentCommunityVO> communities = resonanceService.getSentimentCommunities();
            log.info("情感社区查询完成，共 {} 个社区", communities.size());
            return Result.success("查询成功", communities);
        } catch (RuntimeException e) {
            log.error("查询情感社区失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 手动触发全量情感共鸣重新计算
     *
     * API路径: POST /api/sentiment/resonance/recalculate
     */
    @PostMapping("/resonance/recalculate")
    @Operation(summary = "手动触发情感共鸣重新计算",
               description = "全量计算用户情感相似度和社区划分，正常情况由每周定时任务自动完成")
    public Result<String> recalculateResonance() {
        log.info("手动触发情感共鸣重新计算");
        try {
            int count = resonanceService.recalculateResonance();
            log.info("情感共鸣重新计算完成，共创建 {} 对共鸣关系", count);
            return Result.success("重新计算成功，共创建 " + count + " 对共鸣关系");
        } catch (RuntimeException e) {
            log.error("情感共鸣重新计算失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户的情感伙伴列表
     *
     * API路径: GET /api/sentiment/partners?userId={id}&limit=10
     */
    @GetMapping("/partners")
    @Operation(summary = "获取用户情感伙伴",
               description = "返回与指定用户情感最相似的 Top-N 用户，按共鸣分数降序排列")
    public Result<List<SentimentNetworkVO.NetworkNodeVO>> getEmotionalPartners(
            @Parameter(description = "用户ID", required = true) @RequestParam Long userId,
            @Parameter(description = "返回数量上限（默认 10）") @RequestParam(defaultValue = "10") Integer limit) {

        log.info("查询情感伙伴: userId={}, limit={}", userId, limit);
        try {
            List<SentimentNetworkVO.NetworkNodeVO> partners =
                    resonanceService.getEmotionalPartners(userId, limit);
            log.info("情感伙伴查询完成: userId={}, partnerCount={}", userId, partners.size());
            return Result.success("查询成功", partners);
        } catch (RuntimeException e) {
            log.error("查询情感伙伴失败: userId={}", userId, e);
            return Result.error(e.getMessage());
        }
    }
}
