package com.seu.emotionhub.common.annotation;

import java.lang.annotation.*;

/**
 * 敏感词过滤注解
 * 用于自动过滤方法参数或返回值中的敏感词
 *
 * @author EmotionHub Team
 */
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SensitiveFilter {

    /**
     * 过滤策略
     */
    FilterStrategy strategy() default FilterStrategy.REPLACE;

    /**
     * 替换字符（仅在REPLACE策略下有效）
     */
    String replaceChar() default "*";

    /**
     * 是否抛出异常（在REJECT策略下）
     */
    boolean throwException() default true;

    /**
     * 异常消息
     */
    String message() default "内容包含敏感词，请修改后重试";

    /**
     * 过滤策略枚举
     */
    enum FilterStrategy {
        /**
         * 替换敏感词（敏感词 -> ***）
         */
        REPLACE,

        /**
         * 拒绝请求（抛出异常或返回null）
         */
        REJECT,

        /**
         * 仅记录日志
         */
        LOG_ONLY
    }
}
