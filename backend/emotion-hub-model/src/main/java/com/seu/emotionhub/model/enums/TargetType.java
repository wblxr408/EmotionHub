package com.seu.emotionhub.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 目标类型枚举
 * 用于点赞记录等需要标识目标的场景
 *
 * @author EmotionHub Team
 */
@Getter
@AllArgsConstructor
public enum TargetType {

    /**
     * 帖子
     */
    POST("POST", "帖子"),

    /**
     * 评论
     */
    COMMENT("COMMENT", "评论");

    private final String code;
    private final String description;

    public static TargetType fromCode(String code) {
        for (TargetType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return POST;
    }
}
