package com.seu.emotionhub.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步任务配置
 *
 * @author EmotionHub Team
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * 异步任务执行器
     * 用于情感分析、通知发送等异步操作
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // 核心线程数
        executor.setCorePoolSize(5);

        // 最大线程数
        executor.setMaxPoolSize(20);

        // 队列容量
        executor.setQueueCapacity(100);

        // 线程名称前缀
        executor.setThreadNamePrefix("emotion-async-");

        // 线程空闲时间（秒）
        executor.setKeepAliveSeconds(60);

        // 拒绝策略：调用者线程执行
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);

        // 等待时间（秒）
        executor.setAwaitTerminationSeconds(60);

        executor.initialize();

        log.info("异步任务执行器初始化完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }
}
