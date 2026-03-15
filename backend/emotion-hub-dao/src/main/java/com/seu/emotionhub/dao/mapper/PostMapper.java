package com.seu.emotionhub.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seu.emotionhub.model.entity.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 帖子Mapper接口
 *
 * @author EmotionHub Team
 */
@Mapper
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 增加浏览数（原子操作）
     *
     * @param postId 帖子ID
     * @param count  增加的数量
     * @return 影响行数
     */
    @Update("UPDATE post SET view_count = view_count + #{count} WHERE id = #{postId}")
    int incrementViewCount(@Param("postId") Long postId, @Param("count") Integer count);

    /**
     * 增加点赞数（原子操作）
     *
     * @param postId 帖子ID
     * @param count  增加的数量（正数表示+1，负数表示-1）
     * @return 影响行数
     */
    @Update("UPDATE post SET like_count = like_count + #{count} WHERE id = #{postId}")
    int incrementLikeCount(@Param("postId") Long postId, @Param("count") Integer count);

    /**
     * 增加评论数（原子操作）
     *
     * @param postId 帖子ID
     * @param count  增加的数量
     * @return 影响行数
     */
    @Update("UPDATE post SET comment_count = comment_count + #{count} WHERE id = #{postId}")
    int incrementCommentCount(@Param("postId") Long postId, @Param("count") Integer count);
}
