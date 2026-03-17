package com.seu.emotionhub.service.cache;
/**
 * Redis Key常量（
 */
public class EmotionRedisKeyConstants {
    /** 用户情感历史ZSet（key:emotion:history:{userId}） */
    public static final String USER_EMOTION_HISTORY = "emotion:history:%s";
    /** 用户实时情感状态（key:emotion:state:{userId}） */
    public static final String USER_EMOTION_STATE = "emotion:state:%s";
    /** Redis TTL（7天，单位：秒） */
    public static final Long TTL_SECONDS = 604800L;
}