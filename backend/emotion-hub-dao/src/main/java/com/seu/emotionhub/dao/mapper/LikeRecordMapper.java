package com.seu.emotionhub.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seu.emotionhub.model.entity.LikeRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 点赞记录Mapper接口
 *
 * @author EmotionHub Team
 */
@Mapper
public interface LikeRecordMapper extends BaseMapper<LikeRecord> {
    // 基础CRUD方法已由BaseMapper提供
    // 特殊查询可通过QueryWrapper实现
}
