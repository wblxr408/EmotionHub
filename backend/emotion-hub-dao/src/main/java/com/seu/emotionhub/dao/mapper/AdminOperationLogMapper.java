package com.seu.emotionhub.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seu.emotionhub.model.entity.AdminOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 管理员操作日志 Mapper
 */
@Mapper
public interface AdminOperationLogMapper extends BaseMapper<AdminOperationLog> {
}
