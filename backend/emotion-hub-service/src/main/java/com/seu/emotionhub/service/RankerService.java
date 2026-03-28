package com.seu.emotionhub.service;

import com.seu.emotionhub.model.entity.ContentEmotionTag;
import com.seu.emotionhub.model.entity.Post;
import com.seu.emotionhub.model.enums.EmotionStateEnum;

import java.util.List;
import java.util.Map;

/**
 * ML 排序服务接口
 *
 * @author EmotionHub Team
 */
public interface RankerService {

    /**
     * 批量预测候选帖子的排序分数
     *
     * @param posts                  候选帖子列表
     * @param tagMap                 内容情感标签（来自 2.2）
     * @param state                  用户当前情感状态（来自 2.1）
     * @param baseScores             CF 基础分（postId → score，来自 2.3）
     * @param userAvgScore           用户 24h 平均情感分（来自 2.1 滑动窗口统计）
     * @param userVolatility         用户情感波动性（来自 2.1）
     * @param trendType              情感趋势：RISING / FALLING / STABLE（来自 2.1）
     * @param authorInfluenceScores  作者影响力分（authorUserId → normalizedScore，来自 2.3）
     * @return 分数列表，顺序与 posts 一致
     */
    List<Double> predict(List<Post> posts,
                         Map<Long, ContentEmotionTag> tagMap,
                         EmotionStateEnum state,
                         Map<Long, Double> baseScores,
                         Double userAvgScore,
                         Double userVolatility,
                         String trendType,
                         Map<Long, Double> authorInfluenceScores);
}
