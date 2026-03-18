package com.seu.emotionhub.web.aspect;

import com.seu.emotionhub.common.annotation.RateLimit;
import com.seu.emotionhub.common.enums.ErrorCode;
import com.seu.emotionhub.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Collections;

/**
 * 限流切面 - 支持多种限流策略
 * 1. 滑动窗口算法（推荐）：平滑限流，避免临界问题
 * 2. 令牌桶算法：允许突发流量
 * 3. 固定窗口算法：简单高效
 *
 * 技术亮点：
 * - 基于Redis实现分布式限流
 * - 使用Lua脚本保证原子性
 * - 支持IP、用户、全局多维度限流
 *
 * @author EmotionHub Team
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    @Before("@annotation(com.seu.emotionhub.common.annotation.RateLimit)")
    public void doBefore(JoinPoint point) {
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);

        // 构建限流key
        String key = buildKey(rateLimit, method);

        // 根据不同策略执行限流
        boolean allowed = switch (rateLimit.strategy()) {
            case SLIDING_WINDOW -> slidingWindowLimit(key, rateLimit.period(), rateLimit.count());
            case TOKEN_BUCKET -> tokenBucketLimit(key, rateLimit.capacity(), rateLimit.rate());
            case FIXED_WINDOW -> fixedWindowLimit(key, rateLimit.period(), rateLimit.count());
        };

        if (!allowed) {
            log.warn("限流触发: key={}, strategy={}", key, rateLimit.strategy());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, rateLimit.message());
        }
    }

    /**
     * 滑动窗口限流（推荐）
     * 使用Redis的ZSet实现，Lua脚本保证原子性
     */
    private boolean slidingWindowLimit(String key, int period, int count) {
        long currentTime = Instant.now().toEpochMilli();
        long windowStart = currentTime - period * 1000L;

        // Lua脚本保证原子性
        String luaScript = """
            local key = KEYS[1]
            local windowStart = tonumber(ARGV[1])
            local currentTime = tonumber(ARGV[2])
            local limit = tonumber(ARGV[3])
            local period = tonumber(ARGV[4])

            -- 移除过期的记录
            redis.call('ZREMRANGEBYSCORE', key, 0, windowStart)

            -- 获取当前窗口内的请求数
            local current = redis.call('ZCARD', key)

            if current < limit then
                -- 添加当前请求
                redis.call('ZADD', key, currentTime, currentTime)
                -- 设置过期时间
                redis.call('EXPIRE', key, period)
                return 1
            else
                return 0
            end
            """;

        RedisScript<Long> script = RedisScript.of(luaScript, Long.class);
        Long result = redisTemplate.execute(
                script,
                Collections.singletonList(RATE_LIMIT_PREFIX + key),
                windowStart, currentTime, count, period
        );

        return result != null && result == 1;
    }

    /**
     * 令牌桶限流
     * 允许突发流量，平滑限流
     */
    private boolean tokenBucketLimit(String key, int capacity, double rate) {
        long currentTime = Instant.now().toEpochMilli();

        String luaScript = """
            local key = KEYS[1]
            local capacity = tonumber(ARGV[1])
            local rate = tonumber(ARGV[2])
            local currentTime = tonumber(ARGV[3])

            local bucket = redis.call('HMGET', key, 'tokens', 'lastTime')
            local tokens = tonumber(bucket[1])
            local lastTime = tonumber(bucket[2])

            if tokens == nil then
                tokens = capacity
                lastTime = currentTime
            else
                -- 计算新增的令牌数
                local deltaTime = (currentTime - lastTime) / 1000
                local newTokens = tokens + deltaTime * rate
                if newTokens > capacity then
                    newTokens = capacity
                end
                tokens = newTokens
                lastTime = currentTime
            end

            if tokens >= 1 then
                tokens = tokens - 1
                redis.call('HMSET', key, 'tokens', tokens, 'lastTime', lastTime)
                redis.call('EXPIRE', key, 3600)
                return 1
            else
                redis.call('HMSET', key, 'tokens', tokens, 'lastTime', lastTime)
                redis.call('EXPIRE', key, 3600)
                return 0
            end
            """;

        RedisScript<Long> script = RedisScript.of(luaScript, Long.class);
        Long result = redisTemplate.execute(
                script,
                Collections.singletonList(RATE_LIMIT_PREFIX + key),
                capacity, rate, currentTime
        );

        return result != null && result == 1;
    }

    /**
     * 固定窗口限流
     * 简单高效，但存在临界问题
     */
    private boolean fixedWindowLimit(String key, int period, int count) {
        long currentTime = Instant.now().getEpochSecond();
        long windowKey = currentTime / period;
        String finalKey = RATE_LIMIT_PREFIX + key + ":" + windowKey;

        Long current = redisTemplate.opsForValue().increment(finalKey);
        if (current == null) {
            return false;
        }

        if (current == 1) {
            redisTemplate.expire(finalKey, java.time.Duration.ofSeconds(period));
        }

        return current <= count;
    }

    /**
     * 构建限流key
     */
    private String buildKey(RateLimit rateLimit, Method method) {
        String baseKey = rateLimit.key();
        if (baseKey.isEmpty()) {
            baseKey = method.getDeclaringClass().getSimpleName() + ":" + method.getName();
        }

        return switch (rateLimit.limitType()) {
            case IP -> baseKey + ":" + getClientIP();
            case USER -> baseKey + ":" + getCurrentUserId();
            case GLOBAL -> baseKey;
            case CUSTOM -> baseKey;
        };
    }

    /**
     * 获取客户端IP
     */
    private String getClientIP() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "unknown";
        }

        HttpServletRequest request = attributes.getRequest();
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 处理多个IP的情况（取第一个）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    /**
     * 获取当前用户ID
     */
    private String getCurrentUserId() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof Long) {
                return principal.toString();
            }
            if (principal instanceof String) {
                return (String) principal;
            }
            return "anonymous";
        } catch (Exception e) {
            return "anonymous";
        }
    }
}
