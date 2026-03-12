package com.seu.emotionhub.model.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 情感传播分析响应VO
 * 用于返回帖子的完整情感传播分析结果
 *
 * @author EmotionHub Team
 */
@Data
public class SentimentPropagationVO {

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 帖子情感分数
     */
    private BigDecimal postSentimentScore;

    /**
     * 帖子情感标签
     */
    private String postEmotionLabel;

    /**
     * 总评论数
     */
    private Integer totalComments;

    /**
     * 平均情感一致性
     */
    private BigDecimal avgConsistency;

    /**
     * 平均情感放大系数
     */
    private BigDecimal avgAmplification;

    /**
     * 情感转折点数量
     */
    private Integer shiftCount;

    /**
     * 最大评论深度
     */
    private Integer maxDepth;

    /**
     * 情感传播类型
     * CONSISTENT - 一致传播（评论情感与帖子一致）
     * AMPLIFIED - 放大传播（评论情感更强烈）
     * DAMPENED - 衰减传播（评论情感减弱）
     * CONTROVERSIAL - 争议传播（评论情感分化）
     */
    private String propagationType;

    /**
     * 详细传播记录列表
     */
    private List<PropagationNodeVO> propagationNodes;

    /**
     * 情感转折点列表
     */
    private List<PropagationNodeVO> shiftNodes;

    /**
     * 情感传播节点VO
     */
    @Data
    public static class PropagationNodeVO {
        /**
         * 评论ID
         */
        private Long commentId;

        /**
         * 用户ID
         */
        private Long userId;

        /**
         * 用户昵称
         */
        private String userNickname;

        /**
         * 用户头像
         */
        private String userAvatar;

        /**
         * 评论内容
         */
        private String content;

        /**
         * 父评论ID
         */
        private Long parentCommentId;

        /**
         * 评论层级
         */
        private Integer depthLevel;

        /**
         * 评论情感分数
         */
        private BigDecimal commentSentimentScore;

        /**
         * 评论情感标签
         */
        private String commentEmotionLabel;

        /**
         * 情感一致性
         */
        private BigDecimal sentimentConsistency;

        /**
         * 情感放大系数
         */
        private BigDecimal sentimentAmplification;

        /**
         * 是否发生情感转折
         */
        private Boolean isSentimentShift;

        /**
         * 转折方向
         */
        private String shiftDirection;

        /**
         * 创建时间
         */
        private LocalDateTime createdAt;
    }
}
