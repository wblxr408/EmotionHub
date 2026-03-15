package com.seu.emotionhub.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 情感分析记录实体类
 * 对应数据库的emotion_analysis表
 * 存储LLM分析的完整结果和日志
 *
 * @author EmotionHub Team
 */
@Data
@TableName("emotion_analysis")
public class EmotionAnalysis {

    /**
     * 分析记录ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 内容类型：POST-帖子 / COMMENT-评论
     */
    @TableField("content_type")
    private String contentType;

    /**
     * 内容ID（帖子或评论的ID）
     */
    @TableField("content_id")
    private Long contentId;

    /**
     * LLM提供商：openai / qianwen / wenxin / zhipu
     */
    @TableField("llm_provider")
    private String llmProvider;

    /**
     * 请求数据（JSON格式）
     * 包含发送给LLM的prompt和参数
     */
    @TableField("request_data")
    private String requestData;

    /**
     * 响应数据（JSON格式）
     * 包含LLM返回的完整响应
     */
    @TableField("response_data")
    private String responseData;

    /**
     * 情感分数：-1.00 到 1.00
     */
    @TableField("emotion_score")
    private BigDecimal emotionScore;

    /**
     * 情感标签：POSITIVE / NEUTRAL / NEGATIVE
     */
    @TableField("emotion_label")
    private String emotionLabel;

    /**
     * 关键词（JSON数组）
     * 示例：["开心", "快乐", "幸福"]
     */
    private String keywords;

    /**
     * 分析说明
     * LLM对情感的详细解释
     */
    private String analysis;

    /**
     * 分析耗时（毫秒）
     * 用于性能监控
     */
    @TableField("analysis_time")
    private Integer analysisTime;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
