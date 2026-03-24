package com.seu.emotionhub.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seu.emotionhub.model.entity.Report;
import org.apache.ibatis.annotations.Mapper;

/**
 * 举报 Mapper
 */
@Mapper
public interface ReportMapper extends BaseMapper<Report> {
}
