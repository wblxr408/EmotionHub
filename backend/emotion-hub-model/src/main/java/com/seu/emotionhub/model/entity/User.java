package com.seu.emotionhub.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库的user表
 *
 * @author EmotionHub Team
 */
@Data
@TableName("user")
public class User {

    /**
     * 用户ID（主键，自增）
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名（唯一）
     */
    private String username;

    /**
     * 密码（BCrypt加密存储）
     * 使用@JsonIgnore防止序列化时泄露密码
     */
    @JsonIgnore
    private String password;

    /**
     * 邮箱（唯一）
     */
    private String email;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 个人简介
     */
    private String bio;

    /**
     * 角色：USER-普通用户 / ADMIN-管理员
     */
    private String role;

    /**
     * 账号状态：ACTIVE-正常 / BANNED-禁用
     */
    private String status;

    /**
     * 创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
