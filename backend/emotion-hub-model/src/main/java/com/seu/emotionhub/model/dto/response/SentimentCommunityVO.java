package com.seu.emotionhub.model.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 情感社区VO
 * 用于展示通过 Louvain 算法发现的情感用户社区
 *
 * @author EmotionHub Team
 */
@Data
public class SentimentCommunityVO {

    /** 社区ID（由 Louvain 算法分配） */
    private Integer communityId;

    /** 社区类型: OPTIMISTIC / RATIONAL / PESSIMISTIC / MIXED */
    private String communityType;

    /** 社区中文标签：乐观派 / 理性派 / 悲观派 / 混合派 */
    private String communityLabel;

    /** 社区成员数量 */
    private Integer memberCount;

    /** 社区内平均共鸣分数 */
    private Double avgResonanceScore;

    /** 社区主导情感: POSITIVE / NEUTRAL / NEGATIVE */
    private String dominantEmotion;

    /** 社区平均情感分数（-1.0 到 1.0） */
    private Double avgEmotionScore;

    /** 社区成员列表 */
    private List<CommunityMemberVO> members;

    /**
     * 社区成员信息
     */
    @Data
    public static class CommunityMemberVO {

        /** 用户ID */
        private Long userId;

        /** 用户昵称 */
        private String nickname;

        /** 用户头像URL */
        private String avatar;

        /** 平均情感分数（-1.0 到 1.0） */
        private Double avgEmotionScore;

        /** 主导情感: POSITIVE / NEUTRAL / NEGATIVE */
        private String dominantEmotion;

        /** 总发帖数量 */
        private Integer postCount;
    }
}
