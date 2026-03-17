package com.seu.emotionhub.service.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 情感变化事件监听器
 */
@Component
@Slf4j
public class EmotionChangeListener {

    @EventListener(EmotionChangeEvent.class)
    public void onEmotionChange(EmotionChangeEvent event) {
        Long userId = event.getUserId();
        String newState = event.getNewState().getName();
        log.info("[情感状态变更] 用户ID：{}，新状态：{}", userId, newState);

    }
}