package com.seu.emotionhub.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seu.emotionhub.model.entity.EmotionAnalysis;
import org.apache.ibatis.annotations.Mapper;

/**
 * 情感分析记录Mapper接口
 *
 * @author EmotionHub Team
 */
@Mapper
public interface EmotionAnalysisMapper extends BaseMapper<EmotionAnalysis> {
    // 基础CRUD方法已由BaseMapper提供
    // 可添加统计查询、分析日志查询等方法
}
