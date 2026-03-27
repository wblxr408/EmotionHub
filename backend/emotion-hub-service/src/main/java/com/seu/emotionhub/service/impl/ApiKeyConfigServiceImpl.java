package com.seu.emotionhub.service.impl;

import com.seu.emotionhub.common.enums.ErrorCode;
import com.seu.emotionhub.common.exception.BusinessException;
import com.seu.emotionhub.dao.mapper.ApiKeyConfigMapper;
import com.seu.emotionhub.model.entity.ApiKeyConfig;
import com.seu.emotionhub.model.enums.LLMProvider;
import com.seu.emotionhub.service.ApiKeyConfigService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * API密钥配置服务实现类
 *
 * @author EmotionHub Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApiKeyConfigServiceImpl implements ApiKeyConfigService {

    private final ApiKeyConfigMapper apiKeyConfigMapper;

    @Override
    public ApiKeyConfig getEffectiveApiKey(Long userId, String provider) {
        // 优先级：用户个人配置 > 平台默认配置
        if (userId != null && userId > 0) {
            // 先查用户个人默认配置
            ApiKeyConfig userConfig = apiKeyConfigMapper.selectDefaultByUserAndProvider(userId, provider);
            if (userConfig != null) {
                log.debug("命中用户个人配置: userId={}, provider={}", userId, provider);
                return userConfig;
            }
            // 再查用户任意启用配置
            ApiKeyConfig userAnyEnabled = apiKeyConfigMapper.selectEnabledByUserAndProvider(userId, provider);
            if (userAnyEnabled != null) {
                log.debug("命中用户启用配置: userId={}, provider={}", userId, provider);
                return userAnyEnabled;
            }
        }

        // 查平台默认配置
        ApiKeyConfig platformDefault = apiKeyConfigMapper.selectPlatformDefault(provider);
        if (platformDefault != null) {
            log.debug("命中平台默认配置: provider={}", provider);
            return platformDefault;
        }

        log.warn("未找到有效API配置: userId={}, provider={}", userId, provider);
        return null;
    }

    @Override
    public List<ApiKeyConfig> getUserConfigs(Long userId) {
        LambdaQueryWrapper<ApiKeyConfig> query = new LambdaQueryWrapper<>();
        query.eq(ApiKeyConfig::getUserId, userId);
        query.orderByDesc(ApiKeyConfig::getIsDefault)
                .orderByDesc(ApiKeyConfig::getUpdatedAt);
        return apiKeyConfigMapper.selectList(query);
    }

    @Override
    public List<ApiKeyConfig> getPlatformDefaults() {
        LambdaQueryWrapper<ApiKeyConfig> query = new LambdaQueryWrapper<>();
        query.isNull(ApiKeyConfig::getUserId);
        query.eq(ApiKeyConfig::getIsEnabled, 1);
        query.orderByDesc(ApiKeyConfig::getIsDefault);
        return apiKeyConfigMapper.selectList(query);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveConfig(Long userId, String provider, String apiKey, String apiUrl, String model, Boolean isDefault) {
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "API Key不能为空");
        }
        if (!StringUtils.hasText(provider) || LLMProvider.fromCode(provider) == null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的LLM提供商: " + provider);
        }

        // 查询是否已存在配置
        LambdaQueryWrapper<ApiKeyConfig> query = new LambdaQueryWrapper<>();
        query.eq(ApiKeyConfig::getUserId, userId)
                .eq(ApiKeyConfig::getProvider, provider);
        ApiKeyConfig existing = apiKeyConfigMapper.selectOne(query);

        if (existing != null) {
            // 更新现有配置
            existing.setApiKey(apiKey);
            if (StringUtils.hasText(apiUrl)) {
                existing.setApiUrl(apiUrl);
            }
            if (StringUtils.hasText(model)) {
                existing.setModel(model);
            }
            existing.setIsDefault(Boolean.TRUE.equals(isDefault) ? 1 : 0);
            existing.setIsEnabled(1);
            apiKeyConfigMapper.updateById(existing);
            log.info("更新API配置: userId={}, provider={}", userId, provider);
        } else {
            // 新增配置
            ApiKeyConfig newConfig = new ApiKeyConfig();
            newConfig.setUserId(userId);
            newConfig.setProvider(provider);
            newConfig.setApiKey(apiKey);
            newConfig.setApiUrl(apiUrl);
            newConfig.setModel(model);
            newConfig.setIsEnabled(1);
            newConfig.setIsDefault(Boolean.TRUE.equals(isDefault) ? 1 : 0);
            apiKeyConfigMapper.insert(newConfig);
            log.info("新增API配置: userId={}, provider={}", userId, provider);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteConfig(Long userId, String provider) {
        LambdaQueryWrapper<ApiKeyConfig> query = new LambdaQueryWrapper<>();
        query.eq(ApiKeyConfig::getUserId, userId)
                .eq(ApiKeyConfig::getProvider, provider);
        int rows = apiKeyConfigMapper.delete(query);
        log.info("删除API配置: userId={}, provider={}, rows={}", userId, provider, rows);
    }

    @Override
    public boolean validateApiKey(String provider, String apiKey) {
        if (!StringUtils.hasText(provider) || !StringUtils.hasText(apiKey) || apiKey.length() < 8) {
            return false;
        }

        switch (provider) {
            case "qianwen":
                return apiKey.startsWith("sk-") && apiKey.length() >= 20;
            case "openai":
                return apiKey.startsWith("sk-") && apiKey.length() >= 20;
            case "wenxin":
                return apiKey.length() >= 10;
            case "zhipu":
                return apiKey.length() >= 10;
            default:
                return false;
        }
    }
}
