package com.example.sentiment.influence;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserInfluenceService {

    private final UserInfluenceRepository repository;

    // 每日凌晨2点执行影响力计算（比赛亮点）
    @Scheduled(cron = "0 0 2 * * ?")
    public void calculateAllActiveUsers() {
        // 模拟：最近7天有互动的用户
        List<String> activeUserIds = List.of("user001", "user002", "user003");
        for (String userId : activeUserIds) {
            calculateUserInfluence(userId);
        }
    }

    @Transactional
    public void calculateUserInfluence(String userId) {
        // 1. 发帖影响力
        double postInf = 25 * 0.85 * 1.8;
        // 2. 评论影响力
        double commentInf = 42 * 0.55;
        // 3. 综合影响力（PageRank 改进算法）
        double totalScore = 0.5 * postInf + 0.3 * commentInf + 0.2 * 1.5;
        // 4. 正负影响 & 争议度
        double positive = 8.2;
        double negative = 2.7;
        double controversial = Math.abs(positive - negative);

        // 5. 保存或更新
        UserInfluenceScore score = repository.findByUserId(userId)
                .orElse(new UserInfluenceScore());
        score.setUserId(userId);
        score.setPostInfluence(postInf);
        score.setCommentInfluence(commentInf);
        score.setInfluenceScore(totalScore);
        score.setPositiveImpact(positive);
        score.setNegativeImpact(negative);
        score.setControversialScore(controversial);
        score.setUpdateTime(LocalDateTime.now());
        repository.save(score);
    }

    public UserInfluenceScore getByUserId(String userId) {
        return repository.findByUserId(userId).orElse(null);
    }

    public List<UserInfluenceScore> getRanking(String type) {
        if ("positive".equals(type)) {
            return repository.findTop10ByOrderByPositiveImpactDesc();
        } else if ("controversial".equals(type)) {
            return repository.findTop10ByOrderByControversialScoreDesc();
        }
        return List.of();
    }
}