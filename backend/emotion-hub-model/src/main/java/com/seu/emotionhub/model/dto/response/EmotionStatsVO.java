package com.seu.emotionhub.model.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 情感统计VO
 * 用于展示情感趋势图表数据
 *
 * @author EmotionHub Team
 */
@Data
public class EmotionStatsVO {

    /**
     * 统计时间范围
     */
    private LocalDate startDate;
    private LocalDate endDate;

    /**
     * 总帖子数
     */
    private Integer totalPosts;

    /**
     * 平均情感分数
     */
    private BigDecimal avgEmotionScore;

    /**
     * 情感分布
     */
    private EmotionDistribution distribution;

    /**
     * 每日统计数据列表（用于绘制趋势图）
     */
    private List<DailyStats> dailyStatsList;

    /**
     * 情感分布
     */
    @Data
    public static class EmotionDistribution {
        /**
         * 积极情绪数
         */
        private Integer positiveCount;

        /**
         * 中性情绪数
         */
        private Integer neutralCount;

        /**
         * 消极情绪数
         */
        private Integer negativeCount;

        /**
         * 积极情绪占比（百分比）
         */
        private BigDecimal positiveRate;

        /**
         * 中性情绪占比
         */
        private BigDecimal neutralRate;

        /**
         * 消极情绪占比
         */
        private BigDecimal negativeRate;
    }

    /**
     * 每日统计数据
     */
    @Data
    public static class DailyStats {
        /**
         * 日期
         */
        private LocalDate date;

        /**
         * 当日帖子数
         */
        private Integer postCount;

        /**
         * 当日平均情感分数
         */
        private BigDecimal avgScore;

        /**
         * 积极情绪数
         */
        private Integer positiveCount;

        /**
         * 中性情绪数
         */
        private Integer neutralCount;

        /**
         * 消极情绪数
         */
        private Integer negativeCount;
    }
}
