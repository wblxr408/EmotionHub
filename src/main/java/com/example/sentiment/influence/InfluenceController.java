package com.example.sentiment.influence;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sentiment")
@RequiredArgsConstructor
public class InfluenceController {

    private final UserInfluenceService userInfluenceService;

    // 获取用户情感影响力
    @GetMapping("/user/{userId}/influence")
    public UserInfluenceScore getUserInfluence(@PathVariable String userId) {
        return userInfluenceService.getByUserId(userId);
    }

    // 影响力排行榜：positive / controversial
    @GetMapping("/ranking/influence")
    public List<UserInfluenceScore> getRanking(
            @RequestParam(defaultValue = "positive") String type) {
        return userInfluenceService.getRanking(type);
    }
}