package com.seu.emotionhub.model.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 帖子详情VO
 * 包含作者信息、点赞状态等
 *
 * @author EmotionHub Team
 */
@Data
public class PostVO {

    /**
     * 帖子ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 作者用户名
     */
    private String username;

    /**
     * 作者昵称
     */
    private String nickname;

    /**
     * 作者头像
     */
    private String avatar;

    /**
     * 帖子内容
     */
    private String content;

    /**
     * 图片URLs列表
     */
    private List<String> images;

    /**
     * 情感分数
     */
    private BigDecimal emotionScore;

    /**
     * 情感标签
     */
    private String emotionLabel;

    /**
     * LLM分析结果（简化版）
     */
    private EmotionAnalysisVO emotionAnalysis;

    /**
     * 浏览数
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 评论数
     */
    private Integer commentCount;

    /**
     * 当前用户是否已点赞
     */
    private Boolean liked;

    /**
     * 帖子状态
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 情感分析结果VO（嵌套）
     */
    @Data
    public static class EmotionAnalysisVO {
        /**
         * 关键词列表
         */
        private List<String> keywords;

        /**
         * 分析说明
         */
        private String analysis;
    }
}
