package com.seu.emotionhub.web.controller;

import com.seu.emotionhub.common.result.Result;
import com.seu.emotionhub.common.annotation.RateLimit;
import com.seu.emotionhub.model.dto.request.UserLoginRequest;
import com.seu.emotionhub.model.dto.request.UserRegisterRequest;
import com.seu.emotionhub.model.dto.response.UserInfoVO;
import com.seu.emotionhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证Controller
 * 处理用户注册、登录等认证相关接口
 *
 * @author EmotionHub Team
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户注册、登录、登出等认证相关接口")
public class AuthController {

    private final UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户账号")
    @RateLimit(limitType = RateLimit.LimitType.IP, period = 60, count = 10, message = "注册过于频繁，请稍后再试")
    public Result<Map<String, Object>> register(@Valid @RequestBody UserRegisterRequest request) {
        log.info("用户注册请求: username={}", request.getUsername());
        UserInfoVO userInfo = userService.register(request);

        // 注册成功后自动登录，生成Token
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setUsername(request.getUsername());
        loginRequest.setPassword(request.getPassword());
        String token = userService.login(loginRequest);

        // 组装返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userInfo", userInfo);

        return Result.success("注册成功", data);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录并获取Token")
    @RateLimit(limitType = RateLimit.LimitType.IP, period = 60, count = 20, message = "登录过于频繁，请稍后再试")
    public Result<Map<String, Object>> login(@Valid @RequestBody UserLoginRequest request) {
        log.info("用户登录请求: username={}", request.getUsername());
        String token = userService.login(request);

        // 首次登录时请求上下文里尚未注入本次token，因此按用户名查询用户信息
        UserInfoVO userInfo = userService.getUserByUsername(request.getUsername());

        // 组装返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userInfo", userInfo);

        return Result.success("登录成功", data);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public Result<UserInfoVO> getCurrentUser() {
        UserInfoVO userInfo = userService.getCurrentUser();
        return Result.success(userInfo);
    }

    /**
     * 用户登出（前端删除Token即可，此接口可选）
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出（前端删除Token）")
    public Result<Void> logout() {
        log.info("用户登出");
        // JWT是无状态的，登出只需前端删除Token
        // 如果需要实现Token黑名单，可以在此处理
        return Result.success("登出成功", null);
    }

    /**
     * 修改密码
     */
    @PostMapping("/change-password")
    @Operation(summary = "修改密码", description = "修改当前用户密码")
    @RateLimit(limitType = RateLimit.LimitType.USER, period = 300, count = 5, message = "操作过于频繁，请稍后再试")
    public Result<Void> changePassword(@RequestParam String oldPassword,
                                        @RequestParam String newPassword) {
        log.info("修改密码请求");
        userService.changePassword(oldPassword, newPassword);
        return Result.success("密码修改成功", null);
    }
}
