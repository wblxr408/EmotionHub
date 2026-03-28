package com.seu.emotionhub.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seu.emotionhub.model.entity.RecommendationLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 推荐日志Mapper
 *
 * @author EmotionHub Team
 */
@Mapper
public interface RecommendationLogMapper extends BaseMapper<RecommendationLog> {

    /**
     * 标记点击（用于 A/B 测试 CTR 统计）
     *
     * @param id 日志ID
     * @return 影响行数
     */
    @Update("UPDATE recommendation_log SET clicked = 1, clicked_at = NOW() WHERE id = #{id} AND clicked = 0")
    int markClicked(@Param("id") Long id);
}
