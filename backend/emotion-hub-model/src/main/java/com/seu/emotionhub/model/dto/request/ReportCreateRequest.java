package com.seu.emotionhub.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 举报创建请求
 */
@Data
public class ReportCreateRequest {

    @NotBlank(message = "targetType不能为空")
    private String targetType;

    @NotNull(message = "targetId不能为空")
    private Long targetId;

    @NotBlank(message = "reason不能为空")
    @Size(max = 200, message = "reason长度不能超过200")
    private String reason;
}
