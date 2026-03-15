package com.seu.emotionhub.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 情感标签枚举
 *
 * @author EmotionHub Team
 */
@Getter
@AllArgsConstructor
public enum EmotionLabel {

    /**
     * 积极情感
     */
    POSITIVE("POSITIVE", "积极"),

    /**
     * 中性情感
     */
    NEUTRAL("NEUTRAL", "中性"),

    /**
     * 消极情感
     */
    NEGATIVE("NEGATIVE", "消极");

    private final String code;
    private final String description;

    /**
     * 根据code获取枚举
     */
    public static EmotionLabel fromCode(String code) {
        for (EmotionLabel label : values()) {
            if (label.getCode().equals(code)) {
                return label;
            }
        }
        return NEUTRAL;
    }
}
