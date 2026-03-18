package com.seu.emotionhub.web.controller;

import com.seu.emotionhub.common.result.Result;
import com.seu.emotionhub.model.dto.response.UserInfoVO;
import com.seu.emotionhub.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户信息Controller
 *
 * @author EmotionHub Team
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户信息", description = "用户信息查询接口")
public class UserController {

    private final UserService userService;

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息", description = "返回当前登录用户信息")
    public Result<UserInfoVO> getCurrentUserInfo() {
        UserInfoVO userInfo = userService.getCurrentUser();
        return Result.success(userInfo);
    }

    /**
     * 根据用户ID获取用户信息
     */
    @GetMapping("/{userId}")
    @Operation(summary = "根据ID获取用户信息", description = "返回指定用户信息")
    public Result<UserInfoVO> getUserById(@PathVariable Long userId) {
        log.info("获取用户信息: userId={}", userId);
        UserInfoVO userInfo = userService.getUserById(userId);
        return Result.success(userInfo);
    }
}
