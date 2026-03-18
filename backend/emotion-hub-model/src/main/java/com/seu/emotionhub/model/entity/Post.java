package com.seu.emotionhub.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 帖子实体类
 * 对应数据库的post表
 *
 * @author EmotionHub Team
 */
@Data
@TableName("post")
public class Post {

    /**
     * 帖子ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID（发布者）
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 帖子内容
     */
    private String content;

    /**
     * 图片URLs（JSON数组格式）
     * 示例：["https://example.com/img1.jpg", "https://example.com/img2.jpg"]
     */
    private String images;

    /**
     * 情感分数：-1.00 到 1.00
     * 负数表示消极，正数表示积极，0表示中性
     */
    @TableField("emotion_score")
    private BigDecimal emotionScore;

    /**
     * 情感标签：POSITIVE-积极 / NEUTRAL-中性 / NEGATIVE-消极
     */
    @TableField("emotion_label")
    private String emotionLabel;

    /**
     * LLM分析结果（JSON格式）
     * 包含关键词、详细分析等信息
     */
    @TableField("llm_analysis")
    private String llmAnalysis;

    /**
     * 浏览数
     */
    @TableField("view_count")
    private Integer viewCount;

    /**
     * 点赞数
     */
    @TableField("like_count")
    private Integer likeCount;

    /**
     * 评论数
     */
    @TableField("comment_count")
    private Integer commentCount;

    /**
     * 帖子状态：ANALYZING-分析中 / PUBLISHED-已发布 / DELETED-已删除
     */
    private String status;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
    
    public Boolean getDeleted(){
        if (this.status == "deleted")
            return true;
        return false;
    }
}
