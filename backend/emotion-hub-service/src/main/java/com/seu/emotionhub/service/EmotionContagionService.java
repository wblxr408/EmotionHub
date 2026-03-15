package com.seu.emotionhub.service;

import com.seu.emotionhub.model.dto.response.EmotionContagionAnalysisVO;
import com.seu.emotionhub.model.dto.response.UserInfluenceVO;

import java.util.List;

/**
 * 情感传染分析服务接口
 *
 * 创新功能：
 * 1. 分析评论情感与帖子情感的关联度
 * 2. 计算用户的情感影响力指数
 * 3. 识别情感共鸣网络
 * 4. 预测情感传播趋势
 *
 * @author EmotionHub Team
 */
public interface EmotionContagionService {

    /**
     * 分析帖子的情感传染效果
     *
     * @param postId 帖子ID
     * @return 情感传染分析结果
     */
    EmotionContagionAnalysisVO analyzePostContagion(Long postId);

    /**
     * 计算用户的情感影响力
     *
     * @param userId 用户ID
     * @return 用户影响力指数
     */
    UserInfluenceVO calculateUserInfluence(Long userId);

    /**
     * 获取情感影响力排行榜
     *
     * @param limit 数量限制
     * @return 影响力排行榜
     */
    List<UserInfluenceVO> getInfluenceRanking(int limit);

    /**
     * 分析用户之间的情感共鸣度
     *
     * @param userId1 用户1ID
     * @param userId2 用户2ID
     * @return 共鸣度分数 (0-1)
     */
    double calculateEmotionResonance(Long userId1, Long userId2);

    /**
     * 预测帖子的情感传播趋势
     *
     * @param postId 帖子ID
     * @return 传播趋势：VIRAL(病毒式传播), NORMAL(正常), DECLINING(下降)
     */
    String predictContagionTrend(Long postId);
}
