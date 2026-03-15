package com.seu.emotionhub.web.controller;

import com.seu.emotionhub.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试Controller
 * 用于验证项目是否正常启动
 *
 * @RestController = @Controller + @ResponseBody
 * 表示这个类的所有方法返回值都会自动转成JSON格式
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    /**
     * 测试接口
     * 访问地址: http://localhost:8080/api/test/hello
     */
    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("Hello, EmotionHub!");
    }
}
