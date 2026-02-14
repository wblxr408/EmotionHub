package com.seu.emotionhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * EmotionHub 应用启动类
 *
 * @SpringBootApplication 是一个组合注解，包含：
 * - @Configuration: 标记这是一个配置类
 * - @EnableAutoConfiguration: 启用Spring Boot自动配置
 * - @ComponentScan: 自动扫描当前包及子包下的组件
 */
@SpringBootApplication
public class EmotionHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmotionHubApplication.class, args);
        System.out.println("========================================");
        System.out.println("EmotionHub 启动成功！");
        System.out.println("API文档地址: http://localhost:8080/doc.html");
        System.out.println("========================================");
    }
}
