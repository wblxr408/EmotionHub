package com.seu.emotionhub.service;

import com.seu.emotionhub.model.entity.ApiKeyConfig;

import java.util.List;

/**
 * API密钥配置服务接口
 *
 * @author EmotionHub Team
 */
public interface ApiKeyConfigService {

    /**
     * 获取用户指定提供商的生效API配置
     * 优先级：用户个人配置 > 平台默认配置
     *
     * @param userId   用户ID（可以为null，表示仅查平台默认）
     * @param provider LLM提供商
     * @return 生效的配置，未找到返回null
     */
    ApiKeyConfig getEffectiveApiKey(Long userId, String provider);

    /**
     * 获取用户所有API配置
     */
    List<ApiKeyConfig> getUserConfigs(Long userId);

    /**
     * 获取平台默认配置
     */
    List<ApiKeyConfig> getPlatformDefaults();

    /**
     * 创建或更新API配置
     * 若该用户对该provider已存在配置，则更新；否则新增
     */
    void saveConfig(Long userId, String provider, String apiKey, String apiUrl, String model, Boolean isDefault);

    /**
     * 删除用户指定提供商的配置
     */
    void deleteConfig(Long userId, String provider);

    /**
     * 验证API Key是否有效（尝试验证连接）
     *
     * @param provider LLM提供商
     * @param apiKey   API密钥
     * @return true-有效，false-无效
     */
    boolean validateApiKey(String provider, String apiKey);
}
