package com.seu.emotionhub.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seu.emotionhub.model.entity.UserInfluenceScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户情感影响力Mapper
 * 提供用户影响力评分相关的数据库操作
 *
 * @author EmotionHub Team
 */
@Mapper
public interface UserInfluenceScoreMapper extends BaseMapper<UserInfluenceScore> {

    /**
     * 查询用户最新的影响力评分
     *
     * @param userId 用户ID
     * @return 影响力评分记录
     */
    @Select("SELECT * FROM user_influence_score WHERE user_id = #{userId} " +
            "ORDER BY calculation_date DESC LIMIT 1")
    UserInfluenceScore selectLatestByUserId(@Param("userId") Long userId);

    /**
     * 查询用户在指定日期的影响力评分
     *
     * @param userId 用户ID
     * @param date 计算日期
     * @return 影响力评分记录
     */
    @Select("SELECT * FROM user_influence_score WHERE user_id = #{userId} AND calculation_date = #{date}")
    UserInfluenceScore selectByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 查询影响力排行榜（综合影响力）
     *
     * @param date 计算日期
     * @param limit 限制数量
     * @return 影响力排行列表
     */
    @Select("SELECT * FROM user_influence_score WHERE calculation_date = #{date} " +
            "ORDER BY influence_score DESC LIMIT #{limit}")
    List<UserInfluenceScore> selectTopInfluential(@Param("date") LocalDate date, @Param("limit") Integer limit);

    /**
     * 查询正能量影响力排行榜
     *
     * @param date 计算日期
     * @param limit 限制数量
     * @return 正能量影响力排行列表
     */
    @Select("SELECT * FROM user_influence_score WHERE calculation_date = #{date} " +
            "ORDER BY positive_impact DESC LIMIT #{limit}")
    List<UserInfluenceScore> selectTopPositiveInfluence(@Param("date") LocalDate date, @Param("limit") Integer limit);

    /**
     * 查询争议性影响力排行榜
     *
     * @param date 计算日期
     * @param limit 限制数量
     * @return 争议性影响力排行列表
     */
    @Select("SELECT * FROM user_influence_score WHERE calculation_date = #{date} " +
            "ORDER BY controversial_score DESC LIMIT #{limit}")
    List<UserInfluenceScore> selectTopControversial(@Param("date") LocalDate date, @Param("limit") Integer limit);

    /**
     * 查询用户的影响力历史趋势
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 影响力历史记录列表
     */
    @Select("SELECT * FROM user_influence_score " +
            "WHERE user_id = #{userId} AND calculation_date BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY calculation_date")
    List<UserInfluenceScore> selectHistoryByUserId(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 统计指定日期有影响力评分的用户数
     *
     * @param date 计算日期
     * @return 用户数
     */
    @Select("SELECT COUNT(DISTINCT user_id) FROM user_influence_score WHERE calculation_date = #{date}")
    Integer countActiveUsers(@Param("date") LocalDate date);
}
