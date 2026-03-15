package com.seu.emotionhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seu.emotionhub.common.enums.ErrorCode;
import com.seu.emotionhub.common.exception.BusinessException;
import com.seu.emotionhub.common.util.JwtUtil;
import com.seu.emotionhub.dao.mapper.UserMapper;
import com.seu.emotionhub.model.dto.request.UserLoginRequest;
import com.seu.emotionhub.model.dto.request.UserRegisterRequest;
import com.seu.emotionhub.model.dto.response.UserInfoVO;
import com.seu.emotionhub.model.entity.User;
import com.seu.emotionhub.model.enums.UserRole;
import com.seu.emotionhub.model.enums.UserStatus;
import com.seu.emotionhub.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务实现类
 *
 * @author EmotionHub Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfoVO register(UserRegisterRequest request) {
        // 1. 校验用户名是否已存在
        LambdaQueryWrapper<User> usernameQuery = new LambdaQueryWrapper<>();
        usernameQuery.eq(User::getUsername, request.getUsername());
        if (userMapper.selectCount(usernameQuery) > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXIST);
        }

        // 2. 校验邮箱是否已被注册
        LambdaQueryWrapper<User> emailQuery = new LambdaQueryWrapper<>();
        emailQuery.eq(User::getEmail, request.getEmail());
        if (userMapper.selectCount(emailQuery) > 0) {
            throw new BusinessException(ErrorCode.EMAIL_EXIST);
        }

        // 3. 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // BCrypt加密
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setRole(UserRole.USER.getCode()); // 默认普通用户
        user.setStatus(UserStatus.ACTIVE.getCode()); // 默认激活状态

        // 4. 保存到数据库
        int rows = userMapper.insert(user);
        if (rows == 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败");
        }

        log.info("用户注册成功: userId={}, username={}", user.getId(), user.getUsername());

        // 5. 返回用户信息
        return convertToUserInfoVO(user);
    }

    @Override
    public String login(UserLoginRequest request) {
        // 1. 根据用户名查询用户
        LambdaQueryWrapper<User> query = new LambdaQueryWrapper<>();
        query.eq(User::getUsername, request.getUsername());
        User user = userMapper.selectOne(query);

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 2. 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        // 3. 检查账号状态
        if (UserStatus.BANNED.getCode().equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.USER_BANNED);
        }

        // 4. 生成JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        log.info("用户登录成功: userId={}, username={}", user.getId(), user.getUsername());

        return token;
    }

    @Override
    public UserInfoVO getCurrentUser() {
        Long userId = getCurrentUserId();
        return getUserById(userId);
    }

    @Override
    public UserInfoVO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return convertToUserInfoVO(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(String oldPassword, String newPassword) {
        Long userId = getCurrentUserId();
        User user = userMapper.selectById(userId);

        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR, "旧密码错误");
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        int rows = userMapper.updateById(user);

        if (rows == 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改密码失败");
        }

        log.info("用户修改密码成功: userId={}", userId);
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof Long) {
            return (Long) principal;
        }

        throw new BusinessException(ErrorCode.TOKEN_INVALID);
    }

    /**
     * 转换为UserInfoVO
     */
    private UserInfoVO convertToUserInfoVO(User user) {
        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
