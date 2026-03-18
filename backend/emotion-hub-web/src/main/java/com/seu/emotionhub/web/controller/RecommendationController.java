package com.seu.emotionhub.web.controller;

import com.seu.emotionhub.common.result.Result;
import com.seu.emotionhub.model.dto.request.EmotionalRecommendationRequest;
import com.seu.emotionhub.model.dto.response.EmotionalRecommendationResponse;
import com.seu.emotionhub.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 推荐Controller
 *
 * @author EmotionHub Team
 */
@Slf4j
@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@Tag(name = "推荐系统", description = "情感互补推荐与协同过滤")
public class RecommendationController {

    private final RecommendationService recommendationService;

    /**
     * 情感互补推荐
     */
    @PostMapping("/emotional")
    @Operation(summary = "情感互补推荐", description = "根据用户情感状态与协同过滤结果返回推荐列表")
    public Result<EmotionalRecommendationResponse> recommend(@Valid @RequestBody EmotionalRecommendationRequest request) {
        log.info("情感推荐请求: userId={}, strategy={}, limit={}", request.getUserId(), request.getStrategy(), request.getLimit());
        EmotionalRecommendationResponse response = recommendationService.recommendEmotional(request);
        return Result.success(response);
    }
}
