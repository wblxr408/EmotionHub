package com.seu.emotionhub.common.annotation;

import java.lang.annotation.*;

/**
 * 限流注解 - 支持多种限流策略
 * 1. 滑动窗口算法（推荐）：平滑限流，避免临界问题
 * 2. 令牌桶算法：允许突发流量
 * 3. 固定窗口算法：简单高效
 *
 * 支持多维度限流：IP、用户、全局、自定义
 *
 * @author EmotionHub Team
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 限流key的前缀（用于区分不同接口）
     * 默认为空，使用方法签名作为key
     */
    String key() default "";

    /**
     * 限流类型
     */
    LimitType limitType() default LimitType.IP;

    /**
     * 限流策略
     */
    LimitStrategy strategy() default LimitStrategy.SLIDING_WINDOW;

    /**
     * 时间窗口大小（秒）
     */
    int period() default 60;

    /**
     * 时间窗口内最大请求数
     */
    int count() default 100;

    /**
     * 令牌桶容量（仅在TOKEN_BUCKET策略下有效）
     */
    int capacity() default 100;

    /**
     * 令牌生成速率（每秒生成的令牌数，仅在TOKEN_BUCKET策略下有效）
     */
    double rate() default 10.0;

    /**
     * 提示消息
     */
    String message() default "访问过于频繁，请稍后再试";

    /**
     * 限流类型枚举
     */
    enum LimitType {
        /**
         * 根据IP限流
         */
        IP,
        /**
         * 根据用户ID限流
         */
        USER,
        /**
         * 全局限流（针对接口）
         */
        GLOBAL,
        /**
         * 自定义限流（使用key）
         */
        CUSTOM
    }

    /**
     * 限流策略枚举
     */
    enum LimitStrategy {
        /**
         * 滑动窗口算法（推荐）
         * 优点：平滑限流，避免临界问题
         */
        SLIDING_WINDOW,

        /**
         * 令牌桶算法
         * 优点：允许突发流量
         */
        TOKEN_BUCKET,

        /**
         * 固定窗口算法
         * 优点：简单高效
         * 缺点：存在临界问题
         */
        FIXED_WINDOW
    }
}
