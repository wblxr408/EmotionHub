package com.seu.emotionhub.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seu.emotionhub.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 *
 * @author EmotionHub Team
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 基础的CRUD方法已经由BaseMapper提供
    // 如果需要自定义SQL，可以在这里添加方法
}
