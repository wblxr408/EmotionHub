package com.emotionhub.model.enums;

import lombok.Getter;

@Getter
public enum EmotionStateEnum {
    HAPPY(1, "高兴", 70, 100, 0.2),
    CALM(2, "平静", 20, 69, 0.1),
    LOW(3, "低落", -69, -20, 0.2),
    ANXIOUS(4, "焦虑", -100, -70, 0.5),
    FLUCTUANT(5, "波动", -19, 19, 0.3);

    private final Integer code;
    private final String name;
    private final Integer minScore;
    private final Integer maxScore;
    private final Double volatility;

    EmotionStateEnum(Integer code, String name, Integer minScore, Integer maxScore, Double volatility) {
        this.code = code;
        this.name = name;
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.volatility = volatility;
    }

    public static EmotionStateEnum matchState(Integer score, Double volatility) {
        for (EmotionStateEnum state : values()) {
            if (score >= state.getMinScore() && score <= state.getMaxScore()) {
                if (state == FLUCTUANT && volatility >= state.getVolatility()) {
                    return FLUCTUANT;
                }
                if (state == ANXIOUS && volatility >= state.getVolatility()) {
                    return ANXIOUS;
                }
                if (volatility <= state.getVolatility()) {
                    return state;
                }
            }
        }
        return CALM;
    }
}