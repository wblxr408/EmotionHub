package com.seu.emotionhub.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 举报实体
 */
@Data
@TableName("report")
public class Report {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("reporter_id")
    private Long reporterId;

    @TableField("target_type")
    private String targetType;

    @TableField("target_id")
    private Long targetId;

    private String reason;

    private String status;

    @TableField("handler_id")
    private Long handlerId;

    private String action;

    private String remark;

    @TableField("handled_at")
    private LocalDateTime handledAt;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
