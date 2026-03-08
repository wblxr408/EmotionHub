package com.seu.emotionhub.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seu.emotionhub.model.entity.UserEmotionStats;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户情感统计Mapper接口
 *
 * @author EmotionHub Team
 */
@Mapper
public interface UserEmotionStatsMapper extends BaseMapper<UserEmotionStats> {
    // 基础CRUD方法已由BaseMapper提供
    // 可添加时间范围统计、趋势分析等方法
}
