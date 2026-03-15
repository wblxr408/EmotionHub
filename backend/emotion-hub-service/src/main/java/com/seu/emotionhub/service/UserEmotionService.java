package com.emotionhub.service.impl;

import com.emotionhub.model.dto.EmotionStatsDTO;
import com.emotionhub.model.entity.UserEmotionHistory;
import com.emotionhub.model.enums.EmotionStateEnum;

import java.util.List;

public interface UserEmotionService {
    void saveEmotionRecord(UserEmotionHistory history);
    EmotionStatsDTO calculateSlidingWindowStats(Long userId, String timeWindow);
    String judgeEmotionTrend(Long userId, String timeWindow);
    EmotionStateEnum matchEmotionState(Long userId);
    List<UserEmotionHistory> getEmotionHistory(Long userId, Long startTime, Long endTime);
}