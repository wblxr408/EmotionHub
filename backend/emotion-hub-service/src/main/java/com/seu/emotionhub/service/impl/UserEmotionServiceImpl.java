package com.emotionhub.service;

import cn.hutool.core.collection.CollectionUtil;
import com.emotionhub.model.dto.EmotionStatsDTO;
import com.emotionhub.model.entity.UserEmotionHistory;
import com.emotionhub.model.enums.EmotionStateEnum;
import com.emotionhub.model.enums.TrendTypeEnum;
import com.emotionhub.service.UserEmotionService;
import com.emotionhub.service.event.EmotionChangeEvent;
import com.emotionhub.service.redis.EmotionRedisKeyConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserEmotionServiceImpl implements UserEmotionService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ApplicationContext applicationContext;

    @Override
    public void saveEmotionRecord(UserEmotionHistory history) {
        String historyKey = String.format(EmotionRedisKeyConstants.USER_EMOTION_HISTORY, history.getUserId());
        String stateKey = String.format(EmotionRedisKeyConstants.USER_EMOTION_STATE, history.getUserId());

        redisTemplate.opsForZSet().add(historyKey, history.getTimestamp().toString(), history.getSentimentScore());
        redisTemplate.expire(historyKey, EmotionRedisKeyConstants.TTL_SECONDS);

        EmotionStateEnum currentState = matchEmotionState(history.getUserId());
        redisTemplate.opsForValue().set(stateKey, currentState.getName());
        redisTemplate.expire(stateKey, EmotionRedisKeyConstants.TTL_SECONDS);

        applicationContext.publishEvent(new EmotionChangeEvent(history.getUserId(), currentState));
    }

    @Override
    public EmotionStatsDTO calculateSlidingWindowStats(Long userId, String timeWindow) {
        long now = System.currentTimeMillis();
        long timeRange;
        switch (timeWindow) {
            case "1h": timeRange = 3600 * 1000L; break;
            case "24h": timeRange = 24 * 3600 * 1000L; break;
            case "7d": timeRange = 7 * 24 * 3600 * 1000L; break;
            default: throw new IllegalArgumentException("不支持的时间窗口：" + timeWindow);
        }
        long startTime = now - timeRange;

        String historyKey = String.format(EmotionRedisKeyConstants.USER_EMOTION_HISTORY, userId);
        Set<String> timestampSet = redisTemplate.opsForZSet().rangeByScore(historyKey, -100, 100);
        if (CollectionUtil.isEmpty(timestampSet)) {
            return new EmotionStatsDTO();
        }

        List<UserEmotionHistory> historyList = timestampSet.stream()
                .map(timestampStr -> {
                    Long timestamp = Long.parseLong(timestampStr);
                    if (timestamp < startTime) return null;
                    Double score = redisTemplate.opsForZSet().score(historyKey, timestampStr);
                    UserEmotionHistory history = new UserEmotionHistory();
                    history.setUserId(userId);
                    history.setTimestamp(timestamp);
                    history.setSentimentScore(score.intValue());
                    return history;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        EmotionStatsDTO statsDTO = new EmotionStatsDTO();
        statsDTO.setUserId(userId);
        statsDTO.setTimeWindow(timeWindow);

        List<Integer> scoreList = historyList.stream().map(UserEmotionHistory::getSentimentScore).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(scoreList)) return statsDTO;

        double avgScore = scoreList.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        statsDTO.setAvgScore(avgScore);
        statsDTO.setMaxScore(Collections.max(scoreList));
        statsDTO.setMinScore(Collections.min(scoreList));

        double variance = scoreList.stream().mapToDouble(s -> Math.pow(s - avgScore, 2)).average().orElse(0.0);
        double stdDev = Math.sqrt(variance);
        double volatility = avgScore == 0 ? 0 : stdDev / Math.abs(avgScore);
        statsDTO.setVolatility(volatility);

        EmotionStateEnum state = EmotionStateEnum.matchState(scoreList.get(scoreList.size()-1), volatility);
        statsDTO.setEmotionState(state.getName());
        statsDTO.setTrendType(judgeEmotionTrend(userId, timeWindow));

        return statsDTO;
    }

    @Override
    public String judgeEmotionTrend(Long userId, String timeWindow) {
        long now = System.currentTimeMillis();
        long timeRange;
        switch (timeWindow) {
            case "1h": timeRange = 3600 * 1000L; break;
            case "24h": timeRange = 24 * 3600 * 1000L; break;
            case "7d": timeRange = 7 * 24 * 3600 * 1000L; break;
            default: throw new IllegalArgumentException("不支持的时间窗口：" + timeWindow);
        }
        long midTime = now - timeRange / 2;
        long startTime = now - timeRange;

        List<Integer> firstHalfScores = getScoresInTimeRange(userId, startTime, midTime);
        List<Integer> secondHalfScores = getScoresInTimeRange(userId, midTime, now);

        if (CollectionUtil.isEmpty(firstHalfScores) || CollectionUtil.isEmpty(secondHalfScores)) {
            return TrendTypeEnum.STABLE.getName();
        }

        double firstAvg = firstHalfScores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double secondAvg = secondHalfScores.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double diff = secondAvg - firstAvg;

        if (diff >= 5) return TrendTypeEnum.RISING.getName();
        else if (diff <= -5) return TrendTypeEnum.FALLING.getName();
        else return TrendTypeEnum.STABLE.getName();
    }

    @Override
    public EmotionStateEnum matchEmotionState(Long userId) {
        EmotionStatsDTO stats = calculateSlidingWindowStats(userId, "1h");
        if (stats.getAvgScore() == null) return EmotionStateEnum.CALM;
        return EmotionStateEnum.matchState(stats.getAvgScore().intValue(), stats.getVolatility());
    }

    private List<Integer> getScoresInTimeRange(Long userId, Long startTime, Long endTime) {
        String historyKey = String.format(EmotionRedisKeyConstants.USER_EMOTION_HISTORY, userId);
        Set<String> timestampSet = redisTemplate.opsForZSet().rangeByScore(historyKey, -100, 100);
        if (CollectionUtil.isEmpty(timestampSet)) return Collections.emptyList();

        return timestampSet.stream()
                .map(timestampStr -> {
                    Long timestamp = Long.parseLong(timestampStr);
                    if (timestamp >= startTime && timestamp <= endTime) {
                        Double score = redisTemplate.opsForZSet().score(historyKey, timestampStr);
                        return score.intValue();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserEmotionHistory> getEmotionHistory(Long userId, Long startTime, Long endTime) {
        String historyKey = String.format(EmotionRedisKeyConstants.USER_EMOTION_HISTORY, userId);
        Set<String> timestampSet = redisTemplate.opsForZSet().rangeByScore(historyKey, -100, 100);
        if (CollectionUtil.isEmpty(timestampSet)) return Collections.emptyList();

        return timestampSet.stream()
                .map(timestampStr -> {
                    Long timestamp = Long.parseLong(timestampStr);
                    if (timestamp >= startTime && timestamp <= endTime) {
                        Double score = redisTemplate.opsForZSet().score(historyKey, timestampStr);
                        UserEmotionHistory history = new UserEmotionHistory();
                        history.setUserId(userId);
                        history.setTimestamp(timestamp);
                        history.setSentimentScore(score.intValue());
                        return history;
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}