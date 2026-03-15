package com.seu.emotionhub.service;

import com.seu.emotionhub.model.dto.response.UserInfluenceVO;
import com.seu.emotionhub.model.entity.UserInfluenceScore;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户情感影响力服务接口
 *
 * 提供以下核心功能：
 * 1. PageRank 变体算法计算用户影响力
 * 2. 正面/负面影响力评估
 * 3. 争议性评分计算
 * 4. 影响力排行榜查询
 * 5. 影响力历史趋势分析
 *
 * @author EmotionHub Team
 */
public interface UserInfluenceService {

    /**
     * 获取用户最新的影响力评分
     *
     * @param userId 用户ID
     * @return 影响力评分 VO
     */
    UserInfluenceVO getLatestInfluence(Long userId);

    /**
     * 获取用户在指定日期的影响力评分
     *
     * @param userId 用户ID
     * @param date   计算日期
     * @return 影响力评分 VO
     */
    UserInfluenceVO getInfluenceByDate(Long userId, LocalDate date);

    /**
     * 获取综合影响力排行榜
     *
     * @param limit 返回数量限制，默认 20
     * @return 影响力排行列表
     */
    List<UserInfluenceVO> getTopInfluential(Integer limit);

    /**
     * 获取正能量影响力排行榜
     *
     * @param limit 返回数量限制，默认 20
     * @return 正能量影响力排行列表
     */
    List<UserInfluenceVO> getTopPositiveInfluence(Integer limit);

    /**
     * 获取争议性影响力排行榜（话题制造者）
     *
     * @param limit 返回数量限制，默认 20
     * @return 争议性影响力排行列表
     */
    List<UserInfluenceVO> getTopControversial(Integer limit);

    /**
     * 获取用户的影响力历史趋势
     *
     * @param userId    用户ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 影响力历史记录列表
     */
    List<UserInfluenceVO> getInfluenceTrend(Long userId, LocalDate startDate, LocalDate endDate);

    /**
     * 手动触发全量用户影响力计算
     * 计算所有活跃用户的影响力评分，包括：
     * - PageRank 变体算法计算综合影响力
     * - 正面/负面影响力分析
     * - 争议性评分计算
     * - 互动深度统计
     * - 情感改变率分析
     *
     * （定时任务每日凌晨 3:00 自动触发，也可通过 API 手动触发）
     *
     * @return 成功计算的用户数量
     */
    int calculateAllUserInfluence();

    /**
     * 计算单个用户的影响力评分
     *
     * @param userId 用户ID
     * @return 影响力评分实体
     */
    UserInfluenceScore calculateUserInfluence(Long userId);
}
