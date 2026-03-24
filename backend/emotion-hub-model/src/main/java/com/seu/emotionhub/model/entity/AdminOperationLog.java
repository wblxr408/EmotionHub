package com.seu.emotionhub.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员操作日志实体
 */
@Data
@TableName("admin_operation_log")
public class AdminOperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("operator_id")
    private Long operatorId;

    private String action;

    @TableField("target_type")
    private String targetType;

    @TableField("target_id")
    private Long targetId;

    @TableField("before_state")
    private String beforeState;

    @TableField("after_state")
    private String afterState;

    private String remark;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
