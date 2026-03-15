package com.seu.emotionhub.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 *
 * @author EmotionHub Team
 */
@Getter
@AllArgsConstructor
public enum UserRole {

    /**
     * 普通用户
     */
    USER("USER", "普通用户"),

    /**
     * 管理员
     */
    ADMIN("ADMIN", "管理员");

    private final String code;
    private final String description;

    public static UserRole fromCode(String code) {
        for (UserRole role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return USER;
    }
}
