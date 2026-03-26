package com.seu.emotionhub.web.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import com.seu.emotionhub.common.result.Result;
import com.seu.emotionhub.model.dto.request.ApiKeyConfigRequest;
import com.seu.emotionhub.model.dto.response.ApiKeyConfigVO;
import com.seu.emotionhub.model.entity.ApiKeyConfig;
import com.seu.emotionhub.service.ApiKeyConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * API密钥配置Controller
 * 提供LLM API密钥的管理接口，包括配置查询、保存、删除等
 *
 * @author EmotionHub Team
 */
@Slf4j
@RestController
@RequestMapping("/api/apikey")
@RequiredArgsConstructor
@Tag(name = "API密钥管理", description = "LLM API密钥配置管理，支持多平台配置")
public class ApiKeyConfigController {

    private final ApiKeyConfigService apiKeyConfigService;

    @GetMapping("/list")
    @Operation(summary = "获取当前用户的API密钥配置列表",
               description = "返回当前用户配置的所有LLM API密钥，API Key以掩码形式展示")
    public Result<List<ApiKeyConfigVO>> listMyConfigs() {
        Long userId = getCurrentUserId();
        List<ApiKeyConfig> configs = apiKeyConfigService.getUserConfigs(userId);
        List<ApiKeyConfigVO> voList = configs.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    @GetMapping("/platform")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取平台默认API密钥配置",
               description = "返回平台默认的API密钥配置，API Key以掩码形式展示")
    public Result<List<ApiKeyConfigVO>> listPlatformDefaults() {
        List<ApiKeyConfig> configs = apiKeyConfigService.getPlatformDefaults();
        List<ApiKeyConfigVO> voList = configs.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    @GetMapping("/effective")
    @Operation(summary = "获取指定提供商的生效API配置",
               description = "按优先级返回生效的API配置：用户个人配置 > 平台默认配置")
    public Result<ApiKeyConfigVO> getEffectiveConfig(
            @Parameter(description = "LLM提供商: qianwen/openai/wenxin/zhipu", required = true)
            @RequestParam String provider) {
        Long userId = getCurrentUserId();
        ApiKeyConfig config = apiKeyConfigService.getEffectiveApiKey(userId, provider);
        if (config == null) {
            return Result.error("未找到有效的API配置，请先配置");
        }
        return Result.success(convertToVO(config));
    }

    @PostMapping("/save")
    @Operation(summary = "保存API密钥配置",
               description = "为当前用户保存或更新指定提供商的API配置，支持同时设置是否默认")
    public Result<Void> saveConfig(@Valid @RequestBody ApiKeyConfigRequest request) {
        Long userId = getCurrentUserId();
        log.info("保存API配置: userId={}, provider={}", userId, request.getProvider());

        if (!apiKeyConfigService.validateApiKey(request.getProvider(), request.getApiKey())) {
            return Result.error("API Key格式不正确，请检查后重试");
        }

        apiKeyConfigService.saveConfig(
                userId,
                request.getProvider(),
                request.getApiKey(),
                request.getApiUrl(),
                request.getModel(),
                request.getIsDefault()
        );
        return Result.success("保存成功");
    }

    @DeleteMapping("/delete")
    @Operation(summary = "删除API密钥配置",
               description = "删除当前用户指定提供商的API配置")
    public Result<Void> deleteConfig(
            @Parameter(description = "LLM提供商", required = true)
            @RequestParam String provider) {
        Long userId = getCurrentUserId();
        log.info("删除API配置: userId={}, provider={}", userId, provider);
        apiKeyConfigService.deleteConfig(userId, provider);
        return Result.success("删除成功");
    }

    @PostMapping("/validate")
    @Operation(summary = "验证API Key有效性",
               description = "在保存前验证API Key格式是否正确（不验证真实可用性）")
    public Result<Boolean> validateApiKey(
            @Parameter(description = "LLM提供商: qianwen/openai/wenxin/zhipu", required = true)
            @RequestParam String provider,
            @Parameter(description = "API密钥", required = true)
            @RequestParam String apiKey) {
        boolean valid = apiKeyConfigService.validateApiKey(provider, apiKey);
        return Result.success(valid);
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Long) {
                return (Long) principal;
            }
        }
        return null;
    }

    /**
     * 转换为VO（掩码处理API Key）
     */
    private ApiKeyConfigVO convertToVO(ApiKeyConfig config) {
        ApiKeyConfigVO vo = new ApiKeyConfigVO();
        vo.setId(config.getId());
        vo.setUserId(config.getUserId());
        vo.setIsPlatformDefault(config.getUserId() == null);
        vo.setProvider(config.getProvider());
        vo.setMaskedApiKey(ApiKeyConfigVO.maskApiKey(config.getApiKey()));
        vo.setApiUrl(config.getApiUrl());
        vo.setModel(config.getModel());
        vo.setIsEnabled(config.getIsEnabled() != null && config.getIsEnabled() == 1);
        vo.setIsDefault(config.getIsDefault() != null && config.getIsDefault() == 1);
        vo.setCreatedAt(config.getCreatedAt());
        vo.setUpdatedAt(config.getUpdatedAt());
        return vo;
    }
}
