package com.seu.emotionhub.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知类型枚举
 *
 * @author EmotionHub Team
 */
@Getter
@AllArgsConstructor
public enum NotificationType {

    /**
     * 点赞通知
     */
    LIKE("LIKE", "点赞通知"),

    /**
     * 评论通知
     */
    COMMENT("COMMENT", "评论通知"),

    /**
     * 系统通知
     */
    SYSTEM("SYSTEM", "系统通知"),

    /**
     * 情感分析完成通知
     */
    ANALYSIS_COMPLETE("ANALYSIS_COMPLETE", "分析完成");

    private final String code;
    private final String description;

    public static NotificationType fromCode(String code) {
        for (NotificationType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return SYSTEM;
    }
}
