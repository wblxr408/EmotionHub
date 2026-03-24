package com.seu.emotionhub.model.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理后台举报视图
 */
@Data
public class AdminReportVO {

    private Long id;

    private Long reporterId;

    private String reporterUsername;

    private String reporterNickname;

    private String targetType;

    private Long targetId;

    private String targetPreview;

    private String reason;

    private String status;

    private Long handlerId;

    private String handlerNickname;

    private LocalDateTime handledAt;

    private String action;

    private String remark;

    private LocalDateTime createdAt;
}
