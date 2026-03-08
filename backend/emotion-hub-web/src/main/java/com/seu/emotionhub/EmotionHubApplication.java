package com.seu.emotionhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * EmotionHub 应用启动类
 *
 * @author EmotionHub Team
 */
@SpringBootApplication
@EnableTransactionManagement
public class EmotionHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmotionHubApplication.class, args);
        System.out.println("========================================");
        System.out.println("EmotionHub 启动成功！");
        System.out.println("API文档地址: http://localhost:8080/api/doc.html");
        System.out.println("健康检查: http://localhost:8080/api/test/hello");
        System.out.println("========================================");
    }
}
