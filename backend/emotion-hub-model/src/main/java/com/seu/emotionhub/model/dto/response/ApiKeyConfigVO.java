package com.seu.emotionhub.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * API密钥配置响应VO
 * 注意：apiKey字段返回掩码形式，只显示前4位和后4位
 *
 * @author EmotionHub Team
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiKeyConfigVO {

    private Long id;

    private Long userId;

    private Boolean isPlatformDefault;

    private String provider;

    private String maskedApiKey;

    private String apiUrl;

    private String model;

    private Boolean isEnabled;

    private Boolean isDefault;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * 掩码处理：只显示前4位 + *** + 后4位
     */
    public static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }
}
