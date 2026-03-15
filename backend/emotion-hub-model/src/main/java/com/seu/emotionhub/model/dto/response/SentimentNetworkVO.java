package com.seu.emotionhub.model.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 情感共鸣网络图VO
 * 用于前端 D3.js / ECharts 渲染情感共鸣关系图
 *
 * @author EmotionHub Team
 */
@Data
public class SentimentNetworkVO {

    /** 中心用户ID（查询发起者） */
    private Long centerUserId;

    /** 展开深度（默认2，最大3） */
    private Integer depth;

    /** 网络节点列表（每个节点代表一个用户） */
    private List<NetworkNodeVO> nodes;

    /** 网络边列表（每条边代表两用户之间的共鸣关系） */
    private List<NetworkEdgeVO> edges;

    /** 网络整体统计信息 */
    private NetworkStatsVO stats;

    /**
     * 网络节点（代表一个用户）
     */
    @Data
    public static class NetworkNodeVO {

        /** 用户ID */
        private Long userId;

        /** 用户昵称 */
        private String nickname;

        /** 用户头像URL */
        private String avatar;

        /** 主导情感: POSITIVE / NEUTRAL / NEGATIVE */
        private String dominantEmotion;

        /** 平均情感分数（-1.0 到 1.0） */
        private Double avgEmotionScore;

        /** 所属情感社区ID（由 Louvain 算法计算） */
        private Integer communityId;

        /** 社区类型: OPTIMISTIC（乐观派）/ RATIONAL（理性派）/ PESSIMISTIC（悲观派）/ MIXED（混合派） */
        private String communityType;

        /** 总发帖数量 */
        private Integer postCount;

        /** 节点大小（用于可视化，基于帖子数量归一化） */
        private Double nodeSize;

        /** 是否为中心用户 */
        private Boolean isCenterUser;
    }

    /**
     * 网络边（代表两用户之间的情感共鸣关系）
     */
    @Data
    public static class NetworkEdgeVO {

        /** 源用户ID */
        private Long sourceUserId;

        /** 目标用户ID */
        private Long targetUserId;

        /** 共鸣分数（0.0 到 1.0，基于余弦相似度归一化） */
        private Double resonanceScore;

        /** 情感相似度（0.0 到 1.0） */
        private Double sentimentSimilarity;

        /** 互动次数（评论、点赞等） */
        private Integer interactionCount;

        /** 共同主导情感标签: POSITIVE / NEUTRAL / NEGATIVE */
        private String commonEmotionLabel;

        /** 边权重（用于可视化，与共鸣分数成正比） */
        private Double edgeWeight;
    }

    /**
     * 网络整体统计信息
     */
    @Data
    public static class NetworkStatsVO {

        /** 网络节点总数 */
        private Integer totalNodes;

        /** 网络边总数 */
        private Integer totalEdges;

        /** 网络平均共鸣分数 */
        private Double avgResonanceScore;

        /** 发现的情感社区数量 */
        private Integer communityCount;

        /** 中心用户的主导情感 */
        private String centerUserDominantEmotion;
    }
}
