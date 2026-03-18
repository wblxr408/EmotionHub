package com.seu.emotionhub.service;

import com.seu.emotionhub.model.dto.request.UserLoginRequest;
import com.seu.emotionhub.model.dto.request.UserRegisterRequest;
import com.seu.emotionhub.model.dto.response.UserInfoVO;

/**
 * 用户服务接口
 *
 * @author EmotionHub Team
 */
public interface UserService {

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 用户信息
     */
    UserInfoVO register(UserRegisterRequest request);

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @return JWT Token
     */
    String login(UserLoginRequest request);

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    UserInfoVO getCurrentUser();

    /**
     * 根据ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    UserInfoVO getUserById(Long userId);

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    UserInfoVO getUserByUsername(String username);

    /**
     * 修改密码
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(String oldPassword, String newPassword);
}
