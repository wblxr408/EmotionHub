package com.example.sentiment.influence;

import jakarta.persistence.*;
        import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_influence_score")
@Data
public class UserInfluenceScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    private Double influenceScore;    // 综合影响力
    private Double postInfluence;      // 发帖影响力
    private Double commentInfluence;   // 评论影响力
    private Double positiveImpact;     // 正面影响
    private Double negativeImpact;     // 负面影响
    private Double controversialScore;// 争议得分
    private LocalDateTime updateTime;
}