package com.seu.emotionhub.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seu.emotionhub.model.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 *
 * @Mapper 标记这是一个MyBatis的Mapper接口
 * 继承BaseMapper<User>后，自动拥有增删改查方法：
 * - insert(User user): 插入
 * - deleteById(Long id): 根据ID删除
 * - updateById(User user): 根据ID更新
 * - selectById(Long id): 根据ID查询
 * - selectList(Wrapper): 条件查询
 * 等等...
 */

public interface UserMapper extends BaseMapper<User> {
    // 基础的CRUD方法已经由BaseMapper提供
    // 如果需要自定义SQL，可以在这里添加方法
}
