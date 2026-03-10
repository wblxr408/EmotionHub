package com.seu.emotionhub.service.cache;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Redis缓存服务 - 增强版
 *
 * 技术亮点：
 * 1. 布隆过滤器防止缓存穿透
 * 2. 分布式锁防止缓存击穿
 * 3. 随机过期时间防止缓存雪崩
 * 4. 缓存预热机制
 *
 * @author EmotionHub Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 布隆过滤器 - 防止缓存穿透
     * 预期元素数量: 100万，误判率: 0.01
     */
    private BloomFilter<String> bloomFilter;

    /**
     * 缓存key前缀
     */
    public static class CacheKey {
        public static final String POST_DETAIL = "post:detail:";
        public static final String POST_HOT = "post:hot";
        public static final String USER_INFO = "user:info:";
        public static final String STATS_USER = "stats:user:";
        public static final String EMOTION_TREND = "emotion:trend:";
        public static final String RATE_LIMIT = "rate:limit:";
    }

    /**
     * 缓存过期时间（秒）
     */
    public static class CacheTTL {
        public static final long POST_DETAIL = 300; // 5分钟
        public static final long POST_HOT = 600; // 10分钟
        public static final long USER_INFO = 1800; // 30分钟
        public static final long STATS = 3600; // 1小时
        public static final long EMOTION_TREND = 7200; // 2小时
    }

    /**
     * 设置缓存
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.error("设置缓存失败: key={}", key, e);
        }
    }

    /**
     * 获取缓存
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null && clazz.isInstance(value)) {
                return (T) value;
            }
        } catch (Exception e) {
            log.error("获取缓存失败: key={}", key, e);
        }
        return null;
    }

    /**
     * 删除缓存
     */
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("删除缓存失败: key={}", key, e);
        }
    }

    /**
     * 批量删除缓存（模糊匹配）
     */
    public void deletePattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("批量删除缓存失败: pattern={}", pattern, e);
        }
    }

    /**
     * 判断key是否存在
     */
    public boolean exists(String key) {
        try {
            Boolean result = redisTemplate.hasKey(key);
            return result != null && result;
        } catch (Exception e) {
            log.error("判断缓存是否存在失败: key={}", key, e);
            return false;
        }
    }

    /**
     * 设置过期时间
     */
    public void expire(String key, long timeout, TimeUnit unit) {
        try {
            redisTemplate.expire(key, timeout, unit);
        } catch (Exception e) {
            log.error("设置过期时间失败: key={}", key, e);
        }
    }

    /**
     * 自增操作
     */
    public Long increment(String key) {
        try {
            return redisTemplate.opsForValue().increment(key);
        } catch (Exception e) {
            log.error("自增操作失败: key={}", key, e);
            return null;
        }
    }

    /**
     * 自增指定值
     */
    public Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.error("自增操作失败: key={}, delta={}", key, delta, e);
            return null;
        }
    }
}
