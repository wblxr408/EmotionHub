package com.seu.emotionhub.model.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 情感时间线响应VO
 * 用于返回帖子的情感演变时间序列数据
 *
 * @author EmotionHub Team
 */
@Data
public class SentimentTimelineVO {

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 帖子创建时间
     */
    private LocalDateTime postCreatedAt;

    /**
     * 帖子初始情感分数
     */
    private BigDecimal initialSentiment;

    /**
     * 当前平均情感分数（包含所有评论）
     */
    private BigDecimal currentAvgSentiment;

    /**
     * 情感趋势
     * RISING - 上升（越来越积极）
     * FALLING - 下降（越来越消极）
     * STABLE - 稳定
     * VOLATILE - 波动
     */
    private String sentimentTrend;

    /**
     * 情感波动率（标准差）
     */
    private BigDecimal volatility;

    /**
     * 时间线数据点列表
     */
    private List<TimelinePointVO> timelinePoints;

    /**
     * 按层级统计的情感数据
     */
    private List<DepthSentimentVO> depthSentiments;

    /**
     * 时间线数据点VO
     */
    @Data
    public static class TimelinePointVO {
        /**
         * 时间戳
         */
        private LocalDateTime timestamp;

        /**
         * 评论ID
         */
        private Long commentId;

        /**
         * 情感分数
         */
        private BigDecimal sentimentScore;

        /**
         * 情感标签
         */
        private String emotionLabel;

        /**
         * 评论层级
         */
        private Integer depthLevel;

        /**
         * 累计平均情感（到该时间点为止）
         */
        private BigDecimal cumulativeAvgSentiment;

        /**
         * 是否为转折点
         */
        private Boolean isShiftPoint;
    }

    /**
     * 按层级统计的情感数据VO
     */
    @Data
    public static class DepthSentimentVO {
        /**
         * 评论层级
         */
        private Integer depthLevel;

        /**
         * 该层级的评论数
         */
        private Integer commentCount;

        /**
         * 该层级的平均情感分数
         */
        private BigDecimal avgSentiment;

        /**
         * 该层级的情感标准差
         */
        private BigDecimal stdDeviation;

        /**
         * 主导情感标签
         */
        private String dominantLabel;
    }
}
