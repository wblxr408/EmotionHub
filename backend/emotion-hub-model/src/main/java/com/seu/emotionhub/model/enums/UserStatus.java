package com.seu.emotionhub.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态枚举
 *
 * @author EmotionHub Team
 */
@Getter
@AllArgsConstructor
public enum UserStatus {

    /**
     * 正常状态
     */
    ACTIVE("ACTIVE", "正常"),

    /**
     * 禁用状态
     */
    BANNED("BANNED", "禁用");

    private final String code;
    private final String description;

    public static UserStatus fromCode(String code) {
        for (UserStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return ACTIVE;
    }
}
