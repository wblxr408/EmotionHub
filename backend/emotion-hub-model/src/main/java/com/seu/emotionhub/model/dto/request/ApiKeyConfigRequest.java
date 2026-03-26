package com.seu.emotionhub.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * API密钥配置请求
 */
@Data
public class ApiKeyConfigRequest {

    @NotBlank(message = "provider不能为空")
    @Pattern(regexp = "^(qianwen|openai|wenxin|zhipu)$", message = "provider仅支持: qianwen/openai/wenxin/zhipu")
    private String provider;

    @NotBlank(message = "API Key不能为空")
    @Size(max = 500, message = "API Key长度不能超过500")
    private String apiKey;

    @Size(max = 500, message = "API URL长度不能超过500")
    private String apiUrl;

    @Size(max = 100, message = "model长度不能超过100")
    private String model;

    private Boolean isDefault = false;
}
