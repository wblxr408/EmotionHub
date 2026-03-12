package com.seu.emotionhub.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 错误码枚举
 * 统一管理所有错误码和错误消息
 *
 * @author EmotionHub Team
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    // ========== 通用错误 1000-1999 ==========
    SUCCESS(200, "操作成功"),
    SYSTEM_ERROR(500, "系统内部错误"),
    PARAM_ERROR(400, "参数错误"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),

    // ========== 用户相关错误 2000-2999 ==========
    USER_NOT_FOUND(2001, "用户不存在"),
    USERNAME_EXIST(2002, "用户名已存在"),
    EMAIL_EXIST(2003, "邮箱已被注册"),
    PASSWORD_ERROR(2004, "密码错误"),
    TOKEN_INVALID(2005, "Token无效或已过期"),
    TOKEN_EXPIRED(2006, "Token已过期"),
    PERMISSION_DENIED(2007, "权限不足"),
    USER_BANNED(2008, "账号已被禁用"),
    LOGIN_REQUIRED(2009, "请先登录"),

    // ========== 帖子相关错误 3000-3999 ==========
    POST_NOT_FOUND(3001, "帖子不存在"),
    POST_DELETED(3002, "帖子已删除"),
    CONTENT_TOO_LONG(3003, "内容超出长度限制"),
    CONTENT_EMPTY(3004, "内容不能为空"),
    IMAGE_LIMIT_EXCEEDED(3005, "图片数量超过限制"),
    POST_NOT_OWNER(3006, "无权操作此帖子"),

    // ========== 评论相关错误 4000-4999 ==========
    COMMENT_NOT_FOUND(4001, "评论不存在"),
    COMMENT_DELETED(4002, "评论已删除"),
    COMMENT_NOT_OWNER(4003, "无权操作此评论"),
    PARENT_COMMENT_NOT_FOUND(4004, "父评论不存在"),

    // ========== LLM/情感分析相关错误 5000-5999 ==========
    LLM_SERVICE_ERROR(5001, "情感分析服务异常"),
    LLM_TIMEOUT(5002, "分析超时，请稍后重试"),
    LLM_PROVIDER_NOT_AVAILABLE(5003, "LLM服务商不可用"),
    LLM_API_KEY_INVALID(5004, "LLM API密钥无效"),
    LLM_QUOTA_EXCEEDED(5005, "LLM调用配额已用尽"),
    EMOTION_ANALYSIS_FAILED(5006, "情感分析失败"),

    // ========== 频率限制错误 6000-6999 ==========
    RATE_LIMIT_EXCEEDED(6001, "操作过于频繁，请稍后重试"),
    POST_FREQUENCY_LIMIT(6002, "发帖过于频繁，请稍后再试"),
    COMMENT_FREQUENCY_LIMIT(6003, "评论过于频繁，请稍后再试"),
    LIKE_FREQUENCY_LIMIT(6004, "点赞过于频繁，请稍后再试"),

    // ========== 业务规则错误 7000-7999 ==========
    ALREADY_LIKED(7001, "已经点赞过了"),
    NOT_LIKED_YET(7002, "还未点赞"),
    CANNOT_LIKE_OWN_POST(7003, "不能给自己的帖子点赞"),
    SENSITIVE_WORD_DETECTED(7004, "内容包含敏感词"),
    DUPLICATE_OPERATION(7005, "重复操作"),
    OPERATION_ERROR(7006, "操作失败");

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误消息
     */
    private final String message;
}
