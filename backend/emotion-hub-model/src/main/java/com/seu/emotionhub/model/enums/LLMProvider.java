package com.seu.emotionhub.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * LLM提供商枚举
 *
 * @author EmotionHub Team
 */
@Getter
@AllArgsConstructor
public enum LLMProvider {

    /**
     * OpenAI（GPT系列）
     */
    OPENAI("openai", "OpenAI"),

    /**
     * 通义千问（阿里云）
     */
    QIANWEN("qianwen", "通义千问"),

    /**
     * 文心一言（百度）
     */
    WENXIN("wenxin", "文心一言"),

    /**
     * 智谱AI（ChatGLM）
     */
    ZHIPU("zhipu", "智谱AI");

    private final String code;
    private final String description;

    public static LLMProvider fromCode(String code) {
        for (LLMProvider provider : values()) {
            if (provider.getCode().equals(code)) {
                return provider;
            }
        }
        return null;
    }
}
