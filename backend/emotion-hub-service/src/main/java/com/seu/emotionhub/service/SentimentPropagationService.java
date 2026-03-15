package com.seu.emotionhub.service;

import com.seu.emotionhub.model.dto.response.SentimentPropagationVO;
import com.seu.emotionhub.model.dto.response.SentimentTimelineVO;

/**
 * 情感传播分析服务接口
 * 提供情感传播路径分析和时间序列分析功能
 *
 * @author EmotionHub Team
 */
public interface SentimentPropagationService {

    /**
     * 获取帖子的情感传播分析
     * 包括传播路径、情感一致性、放大系数等指标
     *
     * @param postId 帖子ID
     * @return 情感传播分析结果
     */
    SentimentPropagationVO getPropagationAnalysis(Long postId);

    /**
     * 获取帖子的情感时间线
     * 展示情感随时间的演变趋势
     *
     * @param postId 帖子ID
     * @return 情感时间线数据
     */
    SentimentTimelineVO getSentimentTimeline(Long postId);

    /**
     * 记录评论的情感传播
     * 在评论创建时自动调用，记录情感传播关系
     *
     * @param postId 帖子ID
     * @param commentId 评论ID
     */
    void recordCommentPropagation(Long postId, Long commentId);

    /**
     * 批量更新帖子的情感传播数据
     * 用于修复或重新计算历史数据
     *
     * @param postId 帖子ID
     */
    void recalculatePropagation(Long postId);
}
