package com.seu.emotionhub.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seu.emotionhub.model.entity.UserEmotionStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 用户情感统计Mapper接口
 *
 * @author EmotionHub Team
 */
@Mapper
public interface UserEmotionStatsMapper extends BaseMapper<UserEmotionStats> {

    /**
     * 查询用户的情感特征汇总（用于构建情感特征向量）
     * 聚合该用户所有时间段的情感统计数据
     *
     * @param userId 用户ID
     * @return 包含 avg_score, total_positive, total_neutral, total_negative, total_posts 的 Map
     */
    @Select("SELECT user_id, " +
            "AVG(avg_emotion_score) AS avg_score, " +
            "SUM(positive_count)    AS total_positive, " +
            "SUM(neutral_count)     AS total_neutral, " +
            "SUM(negative_count)    AS total_negative, " +
            "SUM(total_posts)       AS total_posts " +
            "FROM user_emotion_stats " +
            "WHERE user_id = #{userId} " +
            "GROUP BY user_id")
    Map<String, Object> selectUserEmotionFeature(@Param("userId") Long userId);

    /**
     * 查询所有有情感统计记录的活跃用户ID列表
     * 只返回至少发过一篇帖子的用户
     *
     * @return 活跃用户ID列表
     */
    @Select("SELECT DISTINCT user_id FROM user_emotion_stats WHERE total_posts > 0")
    List<Long> selectActiveUserIds();
}
