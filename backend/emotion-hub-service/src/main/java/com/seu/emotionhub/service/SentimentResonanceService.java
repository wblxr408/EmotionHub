package com.seu.emotionhub.service;

import com.seu.emotionhub.model.dto.response.SentimentCommunityVO;
import com.seu.emotionhub.model.dto.response.SentimentNetworkVO;

import java.util.List;

/**
 * 情感共鸣网络服务接口
 *
 * 提供以下核心功能：
 * 1. 用户情感特征向量提取（基于历史帖子情感数据）
 * 2. 余弦相似度计算（衡量两用户情感相似性）
 * 3. 情感共鸣关系持久化（写入 sentiment_resonance 表）
 * 4. Louvain 社区发现算法（将相似用户归为社区）
 * 5. 网络图查询（BFS 扩展，供前端可视化）
 * 6. 社区列表查询
 *
 * @author EmotionHub Team
 */
public interface SentimentResonanceService {

    /**
     * 获取以指定用户为中心的情感共鸣网络图
     * 通过 BFS 按深度扩展，返回节点和边供前端渲染
     *
     * @param userId 中心用户ID
     * @param depth  展开深度，范围 [1, 3]，默认 2
     * @return 网络图数据（节点 + 边 + 统计信息）
     */
    SentimentNetworkVO getResonanceNetwork(Long userId, Integer depth);

    /**
     * 获取所有情感社区列表
     * 每个社区包含成员信息和社区类型（乐观派/理性派/悲观派）
     *
     * @return 情感社区列表
     */
    List<SentimentCommunityVO> getSentimentCommunities();

    /**
     * 手动触发全量情感共鸣计算
     * 计算所有活跃用户之间的情感相似度，运行 Louvain 社区发现，并持久化结果
     * （定时任务每周日 2:00 自动触发，也可通过 API 手动触发）
     *
     * @return 创建的共鸣关系对数量
     */
    int recalculateResonance();

    /**
     * 查询与指定用户情感最相似的用户列表（情感伙伴）
     *
     * @param userId 目标用户ID
     * @param limit  返回数量上限（默认 10）
     * @return 情感伙伴节点列表，按共鸣分数降序排列
     */
    List<SentimentNetworkVO.NetworkNodeVO> getEmotionalPartners(Long userId, Integer limit);
}
