package com.seu.emotionhub.model.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员操作日志视图
 */
@Data
public class AdminOperationLogVO {

    private Long id;

    private Long operatorId;

    private String operatorUsername;

    private String operatorNickname;

    private String action;

    private String targetType;

    private Long targetId;

    private String beforeState;

    private String afterState;

    private String remark;

    private LocalDateTime createdAt;
}
