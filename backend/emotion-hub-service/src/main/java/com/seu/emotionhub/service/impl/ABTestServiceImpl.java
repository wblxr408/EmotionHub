package com.seu.emotionhub.service.impl;

import com.seu.emotionhub.service.ABTestService;
import com.seu.emotionhub.service.cache.FeedRedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * A/B 测试服务实现
 * <p>
 * 分桶策略：hash(userId) % 100，低于实验比例进入实验组（emotional_adaptive），否则为对照组（traditional）。
 * 分组结果写入 Redis 保证同一用户在 TTL 周期内分组稳定。
 *
 * @author EmotionHub Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ABTestServiceImpl implements ABTestService {

    private final RedisTemplate<String, Object> redisTemplate;

    /** 实验组比例（0-100），50 表示 50% 用户进入实验组 */
    private static final int EXPERIMENT_RATIO = 50;

    private static final String STRATEGY_EMOTIONAL = "emotional_adaptive";
    private static final String STRATEGY_TRADITIONAL = "traditional";

    @Override
    public String assignFeedStrategy(Long userId) {
        String key = String.format(FeedRedisKeyConstants.AB_TEST_BUCKET, userId);

        // 优先读取 Redis 中已有的分组，保证稳定性
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return cached.toString();
        }

        // 基于 userId hash 确定性分桶
        int bucket = (int) (Math.abs(userId.hashCode()) % 100);
        String strategy = bucket < EXPERIMENT_RATIO ? STRATEGY_EMOTIONAL : STRATEGY_TRADITIONAL;

        redisTemplate.opsForValue().set(key, strategy, FeedRedisKeyConstants.AB_TTL_SECONDS, TimeUnit.SECONDS);
        log.debug("A/B 分配: userId={}, bucket={}, strategy={}", userId, bucket, strategy);
        return strategy;
    }

    @Override
    public String resolveStrategy(Long userId, String requestStrategy) {
        if (StringUtils.hasText(requestStrategy)) {
            String normalized = requestStrategy.trim().toLowerCase();
            if (STRATEGY_EMOTIONAL.equals(normalized) || STRATEGY_TRADITIONAL.equals(normalized)) {
                return normalized;
            }
        }
        return assignFeedStrategy(userId);
    }
}
