package com.seu.emotionhub.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 帖子状态枚举
 *
 * @author EmotionHub Team
 */
@Getter
@AllArgsConstructor
public enum PostStatus {

    /**
     * 分析中 - 帖子已发布，情感分析正在进行
     */
    ANALYZING("ANALYZING", "分析中"),

    /**
     * 已发布 - 情感分析完成，帖子正常展示
     */
    PUBLISHED("PUBLISHED", "已发布"),

    /**
     * 已删除 - 软删除状态
     */
    DELETED("DELETED", "已删除");

    private final String code;
    private final String description;

    public static PostStatus fromCode(String code) {
        for (PostStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return PUBLISHED;
    }
}
