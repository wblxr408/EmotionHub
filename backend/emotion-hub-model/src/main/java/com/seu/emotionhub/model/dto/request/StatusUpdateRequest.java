package com.seu.emotionhub.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 状态更新请求
 */
@Data
public class StatusUpdateRequest {

    @NotBlank(message = "status不能为空")
    private String status;
}
