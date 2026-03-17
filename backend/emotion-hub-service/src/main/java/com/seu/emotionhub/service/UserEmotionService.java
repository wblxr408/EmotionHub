package com.seu.emotionhub.service;

import com.seu.emotionhub.model.dto.response.EmotionStatsDTO;
import com.seu.emotionhub.model.entity.UserEmotionHistory;
import com.seu.emotionhub.model.enums.EmotionStateEnum;

import java.util.List;

public interface UserEmotionService {
    void saveEmotionRecord(UserEmotionHistory history);
    EmotionStatsDTO calculateSlidingWindowStats(Long userId, String timeWindow);
    String judgeEmotionTrend(Long userId, String timeWindow);
    EmotionStateEnum matchEmotionState(Long userId);
    List<UserEmotionHistory> getEmotionHistory(Long userId, Long startTime, Long endTime);
}