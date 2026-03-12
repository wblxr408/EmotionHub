package com.seu.emotionhub.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seu.emotionhub.model.entity.SentimentResonance;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 情感共鸣关系Mapper
 * 提供情感共鸣网络相关的数据库操作
 *
 * @author EmotionHub Team
 */
@Mapper
public interface SentimentResonanceMapper extends BaseMapper<SentimentResonance> {

    /**
     * 查询用户的情感共鸣网络（指定深度）
     *
     * @param userId 用户ID
     * @param date 计算日期
     * @param minScore 最小共鸣分数阈值
     * @param limit 限制数量
     * @return 共鸣关系列表
     */
    @Select("SELECT * FROM sentiment_resonance " +
            "WHERE calculation_date = #{date} " +
            "AND (user_a_id = #{userId} OR user_b_id = #{userId}) " +
            "AND resonance_score >= #{minScore} " +
            "ORDER BY resonance_score DESC LIMIT #{limit}")
    List<SentimentResonance> selectResonanceNetwork(
            @Param("userId") Long userId,
            @Param("date") LocalDate date,
            @Param("minScore") Double minScore,
            @Param("limit") Integer limit
    );

    /**
     * 查询两个用户之间的共鸣关系
     *
     * @param userAId 用户A的ID（较小值）
     * @param userBId 用户B的ID（较大值）
     * @param date 计算日期
     * @return 共鸣关系记录
     */
    @Select("SELECT * FROM sentiment_resonance " +
            "WHERE user_a_id = LEAST(#{userAId}, #{userBId}) " +
            "AND user_b_id = GREATEST(#{userAId}, #{userBId}) " +
            "AND calculation_date = #{date}")
    SentimentResonance selectByUserPair(
            @Param("userAId") Long userAId,
            @Param("userBId") Long userBId,
            @Param("date") LocalDate date
    );

    /**
     * 查询情感社区信息
     *
     * @param date 计算日期
     * @return 社区统计信息列表
     */
    @Select("SELECT community_id, COUNT(*) as member_count, " +
            "AVG(resonance_score) as avg_resonance, " +
            "common_emotion_label " +
            "FROM sentiment_resonance " +
            "WHERE calculation_date = #{date} AND community_id IS NOT NULL " +
            "GROUP BY community_id, common_emotion_label " +
            "ORDER BY member_count DESC")
    List<Map<String, Object>> selectCommunityStats(@Param("date") LocalDate date);

    /**
     * 查询某社区的所有成员关系
     *
     * @param communityId 社区ID
     * @param date 计算日期
     * @return 社区成员关系列表
     */
    @Select("SELECT * FROM sentiment_resonance " +
            "WHERE community_id = #{communityId} AND calculation_date = #{date} " +
            "ORDER BY resonance_score DESC")
    List<SentimentResonance> selectByCommunityId(
            @Param("communityId") Integer communityId,
            @Param("date") LocalDate date
    );

    /**
     * 查询用户所在的情感社区ID
     *
     * @param userId 用户ID
     * @param date 计算日期
     * @return 社区ID，如果用户没有社区则返回null
     */
    @Select("SELECT community_id FROM sentiment_resonance " +
            "WHERE (user_a_id = #{userId} OR user_b_id = #{userId}) " +
            "AND calculation_date = #{date} AND community_id IS NOT NULL " +
            "LIMIT 1")
    Integer selectCommunityIdByUser(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 查询高共鸣用户对（用于网络图可视化）
     *
     * @param date 计算日期
     * @param minScore 最小共鸣分数
     * @param limit 限制数量
     * @return 高共鸣关系列表
     */
    @Select("SELECT * FROM sentiment_resonance " +
            "WHERE calculation_date = #{date} AND resonance_score >= #{minScore} " +
            "ORDER BY resonance_score DESC LIMIT #{limit}")
    List<SentimentResonance> selectHighResonancePairs(
            @Param("date") LocalDate date,
            @Param("minScore") Double minScore,
            @Param("limit") Integer limit
    );

    /**
     * 统计某日期的情感社区数量
     *
     * @param date 计算日期
     * @return 社区数量
     */
    @Select("SELECT COUNT(DISTINCT community_id) FROM sentiment_resonance " +
            "WHERE calculation_date = #{date} AND community_id IS NOT NULL")
    Integer countCommunities(@Param("date") LocalDate date);

    /**
     * 查询用户的情感伙伴（最相似的用户）
     *
     * @param userId 用户ID
     * @param date 计算日期
     * @param limit 限制数量
     * @return 最相似的用户列表
     */
    @Select("SELECT " +
            "CASE WHEN user_a_id = #{userId} THEN user_b_id ELSE user_a_id END as partner_id, " +
            "resonance_score, sentiment_similarity, common_emotion_label " +
            "FROM sentiment_resonance " +
            "WHERE (user_a_id = #{userId} OR user_b_id = #{userId}) " +
            "AND calculation_date = #{date} " +
            "ORDER BY resonance_score DESC LIMIT #{limit}")
    List<Map<String, Object>> selectEmotionalPartners(
            @Param("userId") Long userId,
            @Param("date") LocalDate date,
            @Param("limit") Integer limit
    );
}
