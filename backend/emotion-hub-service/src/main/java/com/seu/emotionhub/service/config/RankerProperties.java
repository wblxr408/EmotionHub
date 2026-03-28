package com.seu.emotionhub.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 排序模型服务配置
 *
 * @author EmotionHub Team
 */
@Data
@Component
@ConfigurationProperties(prefix = "ranker")
public class RankerProperties {

    /** 预测服务地址 */
    private String url = "http://localhost:5000";

    /** HTTP 请求超时（毫秒） */
    private int timeoutMs = 500;

    /**
     * 是否启用 ML 排序。
     * false 时 FeedServiceImpl 回退到规则打分，方便灰度上线。
     */
    private boolean enabled = false;
}
