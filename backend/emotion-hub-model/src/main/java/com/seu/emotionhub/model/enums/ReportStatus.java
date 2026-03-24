package com.seu.emotionhub.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 举报状态枚举
 */
@Getter
@AllArgsConstructor
public enum ReportStatus {

    /**
     * 待处理
     */
    PENDING("PENDING", "待处理"),

    /**
     * 已处理
     */
    PROCESSED("PROCESSED", "已处理"),

    /**
     * 已驳回
     */
    REJECTED("REJECTED", "已驳回");

    private final String code;
    private final String description;

    public static boolean isValid(String code) {
        for (ReportStatus status : values()) {
            if (status.getCode().equals(code)) {
                return true;
            }
        }
        return false;
    }
}
