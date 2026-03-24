package com.seu.emotionhub.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 举报处理请求
 */
@Data
public class ReportHandleRequest {

    @NotBlank(message = "status不能为空")
    private String status;

    @Size(max = 50, message = "action长度不能超过50")
    private String action;

    @Size(max = 500, message = "remark长度不能超过500")
    private String remark;
}
