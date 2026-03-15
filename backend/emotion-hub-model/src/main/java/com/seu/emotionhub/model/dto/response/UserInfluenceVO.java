package com.seu.emotionhub.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 用户影响力响应VO
 * 用于展示用户的情感影响力评分及相关指标
 *
 * @author EmotionHub Team
 */
@Data
public class UserInfluenceVO {

    /** 用户ID */
    private Long userId;

    /** 用户昵称 */
    private String nickname;

    /** 用户头像 */
    private String avatar;

    /**
     * 综合影响力分数（0-100）
     * 通过PageRank变体算法计算
     */
    private BigDecimal influenceScore;

    /**
     * 正面影响力
     * 引发正面评论的能力
     */
    private BigDecimal positiveImpact;

    /**
     * 负面影响力
     * 引发负面评论的能力
     */
    private BigDecimal negativeImpact;

    /**
     * 争议性分数
     * 引发情感分化的程度（评论区情感分歧大）
     */
    private BigDecimal controversialScore;

    /** 统计期内帖子数 */
    private Integer postCount;

    /** 统计期内获得的评论数 */
    private Integer commentCount;

    /**
     * 平均互动深度
     * 评论链平均层级（越深说明引发的讨论越激烈）
     */
    private BigDecimal avgEngagementDepth;

    /**
     * 情感改变率
     * 能够改变他人情感的比例
     */
    private BigDecimal sentimentChangeRate;

    /** 计算日期 */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate calculationDate;

    /** 排名（用于排行榜） */
    private Integer rank;

    /**
     * 影响力等级标签
     * LEGENDARY（传奇）、EXPERT（专家）、ADVANCED（进阶）、INTERMEDIATE（中级）、NOVICE（新手）
     */
    private String influenceLevel;

    /** 影响力等级描述 */
    private String influenceLevelDesc;
}
