package com.seu.emotionhub.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seu.emotionhub.model.entity.ContentEmotionTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 内容情感标签Mapper接口
 *
 * @author EmotionHub Team
 */
@Mapper
public interface ContentEmotionTagMapper extends BaseMapper<ContentEmotionTag> {

    /**
     * 根据帖子ID查询标签
     */
    @Select("SELECT * FROM content_emotion_tags WHERE post_id = #{postId} LIMIT 1")
    ContentEmotionTag selectByPostId(@Param("postId") Long postId);
}
