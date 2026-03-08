package com.seu.emotionhub.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seu.emotionhub.model.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 评论Mapper接口
 *
 * @author EmotionHub Team
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 增加点赞数（原子操作）
     *
     * @param commentId 评论ID
     * @param count     增加的数量（正数表示+1，负数表示-1）
     * @return 影响行数
     */
    @Update("UPDATE comment SET like_count = like_count + #{count} WHERE id = #{commentId}")
    int incrementLikeCount(@Param("commentId") Long commentId, @Param("count") Integer count);
}
