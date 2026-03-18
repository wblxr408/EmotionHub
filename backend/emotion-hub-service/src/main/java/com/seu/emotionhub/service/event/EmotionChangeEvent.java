package com.seu.emotionhub.service.event;

import com.seu.emotionhub.model.enums.EmotionStateEnum;
import org.springframework.context.ApplicationEvent;

/**
 * 情感变化事件
 */
public class EmotionChangeEvent extends ApplicationEvent {
    private Long userId;
    private EmotionStateEnum newState;

    public EmotionChangeEvent(Long userId, EmotionStateEnum newState) {
        super(userId);
        this.userId = userId;
        this.newState = newState;
    }

    // Getter
    public Long getUserId() {
        return userId;
    }

    public EmotionStateEnum getNewState() {
        return newState;
    }
}