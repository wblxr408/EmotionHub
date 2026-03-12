package com.seu.emotionhub.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seu.emotionhub.model.entity.SentimentPropagation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 情感传播关系Mapper
 * 提供情感传播分析相关的数据库操作
 *
 * @author EmotionHub Team
 */
@Mapper
public interface SentimentPropagationMapper extends BaseMapper<SentimentPropagation> {

    /**
     * 查询帖子的所有情感传播记录
     *
     * @param postId 帖子ID
     * @return 传播记录列表
     */
    @Select("SELECT * FROM sentiment_propagation WHERE post_id = #{postId} ORDER BY depth_level, created_at")
    List<SentimentPropagation> selectByPostId(@Param("postId") Long postId);

    /**
     * 查询帖子的情感传播统计信息
     *
     * @param postId 帖子ID
     * @return 统计信息Map
     */
    @Select("SELECT " +
            "COUNT(*) as total_comments, " +
            "AVG(sentiment_consistency) as avg_consistency, " +
            "AVG(sentiment_amplification) as avg_amplification, " +
            "SUM(CASE WHEN is_sentiment_shift = 1 THEN 1 ELSE 0 END) as shift_count, " +
            "MAX(depth_level) as max_depth " +
            "FROM sentiment_propagation WHERE post_id = #{postId}")
    Map<String, Object> selectPropagationStats(@Param("postId") Long postId);

    /**
     * 按层级查询情感传播记录
     *
     * @param postId 帖子ID
     * @param depthLevel 层级
     * @return 传播记录列表
     */
    @Select("SELECT * FROM sentiment_propagation WHERE post_id = #{postId} AND depth_level = #{depthLevel} ORDER BY created_at")
    List<SentimentPropagation> selectByDepthLevel(@Param("postId") Long postId, @Param("depthLevel") Integer depthLevel);

    /**
     * 查询情感转折点
     *
     * @param postId 帖子ID
     * @return 发生情感转折的传播记录
     */
    @Select("SELECT * FROM sentiment_propagation WHERE post_id = #{postId} AND is_sentiment_shift = 1 ORDER BY created_at")
    List<SentimentPropagation> selectSentimentShifts(@Param("postId") Long postId);

    /**
     * 查询评论链的情感演变
     *
     * @param commentId 评论ID
     * @return 从该评论开始的完整评论链
     */
    @Select("WITH RECURSIVE comment_chain AS ( " +
            "SELECT * FROM sentiment_propagation WHERE comment_id = #{commentId} " +
            "UNION ALL " +
            "SELECT sp.* FROM sentiment_propagation sp " +
            "INNER JOIN comment_chain cc ON sp.parent_comment_id = cc.comment_id " +
            ") SELECT * FROM comment_chain ORDER BY depth_level")
    List<SentimentPropagation> selectCommentChain(@Param("commentId") Long commentId);

    /**
     * 查询帖子的情感时间序列数据
     *
     * @param postId 帖子ID
     * @return 时间序列数据（包含时间和情感分数）
     */
    @Select("SELECT created_at, comment_sentiment_score, depth_level " +
            "FROM sentiment_propagation WHERE post_id = #{postId} " +
            "ORDER BY created_at")
    List<Map<String, Object>> selectTimelineData(@Param("postId") Long postId);

    /**
     * 统计用户在某帖子下引发的情感改变数
     *
     * @param postId 帖子ID
     * @param userId 用户ID
     * @return 情感改变数
     */
    @Select("SELECT COUNT(*) FROM sentiment_propagation " +
            "WHERE post_id = #{postId} AND user_id = #{userId} AND is_sentiment_shift = 1")
    Integer countSentimentChangesByUser(@Param("postId") Long postId, @Param("userId") Long userId);
}
