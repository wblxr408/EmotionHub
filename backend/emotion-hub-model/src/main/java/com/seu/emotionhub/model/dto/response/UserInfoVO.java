package com.seu.emotionhub.model.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息VO（脱敏，不包含密码）
 *
 * @author EmotionHub Team
 */
@Data
public class UserInfoVO {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 邮箱
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
     * 角色
     */
    private String role;

    /**
     * 账号状态
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
