package com.seu.emotionhub.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserEmotionHistory {
    private Long userId;
    private Long timestamp;
    private Integer sentimentScore;
    private String source;
}