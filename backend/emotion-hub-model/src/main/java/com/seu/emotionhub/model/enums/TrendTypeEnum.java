package com.emotionhub.model.enums;

import lombok.Getter;

@Getter
public enum TrendTypeEnum {
    RISING("上升", 1),
    FALLING("下降", -1),
    STABLE("稳定", 0);

    private final String name;
    private final Integer value;

    TrendTypeEnum(String name, Integer value) {
        this.name = name;
        this.value = value;
    }
}