package com.seu.emotionhub.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * LLM API密钥配置实体类
 * 对应数据库的api_key_config表
 * 存储用户自主配置的各平台API密钥
 *
 * @author EmotionHub Team
 */
@Data
@TableName("api_key_config")
public class ApiKeyConfig {

    /**
     * 配置ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * LLM提供商：qianwen / openai / wenxin / zhipu
     */
    private String provider;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 自定义API地址（可选）
     */
    @TableField("api_url")
    private String apiUrl;

    /**
     * 使用的模型名称（可选）
     */
    private String model;

    /**
     * 是否启用：0-禁用 1-启用
     */
    @TableField("is_enabled")
    private Integer isEnabled;

    /**
     * 是否默认：0-否 1-是
     */
    @TableField("is_default")
    private Integer isDefault;

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
}
