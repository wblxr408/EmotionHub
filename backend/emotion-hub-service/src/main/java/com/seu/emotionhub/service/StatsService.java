package com.seu.emotionhub.service;

import java.util.Map;

/**
 * 统计服务接口
 *
 * @author EmotionHub Team
 */
public interface StatsService {

    /**
     * 获取用户统计信息
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    Map<String, Object> getUserStats(Long userId);

    /**
     * 获取平台统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getPlatformStats();

    /**
     * 获取当前用户的个人统计
     *
     * @return 统计信息
     */
    Map<String, Object> getMyStats();
}
