package com.seu.emotionhub.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库的user表
 */
@Data
@TableName("user")
public class User {

    /**
     * 用户ID（主键，自增）
     * @TableId 标记主键字段
     * type = IdType.AUTO 表示主键自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（唯一）
     */
    private String username;

    /**
     * 密码（加密存储）
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 角色：USER-普通用户，ADMIN-管理员
     */
    private String role;

    /**
     * 账号状态：0-正常，1-禁用
     */
    private Integer status;

    /**
     * 创建时间
     * @TableField(fill = FieldFill.INSERT) 表示插入时自动填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     * @TableField(fill = FieldFill.INSERT_UPDATE) 表示插入和更新时都自动填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
