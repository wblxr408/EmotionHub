package com.seu.emotionhub.service.impl;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.seu.emotionhub.dao.mapper.PostMapper;
import com.seu.emotionhub.model.entity.Post;
import com.seu.emotionhub.model.enums.EmotionLabel;
import com.seu.emotionhub.model.enums.PostStatus;
import com.seu.emotionhub.service.EmotionAnalysisService;
import com.seu.emotionhub.service.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通义千问情感分析服务实现
 * 使用真实的LLM API进行情感分析
 *
 * @author EmotionHub Team
 */
@Slf4j
@Service("qwenEmotionAnalysisService")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "emotion.analysis.provider", havingValue = "qwen")
public class QwenEmotionAnalysisServiceImpl implements EmotionAnalysisService {

    private final PostMapper postMapper;
    private final CacheService cacheService;

    @Value("${dashscope.api-key:}")
    private String apiKey;

    private static final String MODEL = "qwen-plus"; // 使用qwen-plus模型
    private static final String SYSTEM_PROMPT = """
            You are an expert in emotional analysis. Analyze the emotional tone of the given text and provide:
            1. Emotion Label: POSITIVE, NEGATIVE, or NEUTRAL
            2. Emotion Score: A number between -1.0 (extremely negative) and 1.0 (extremely positive)

            Respond ONLY in this exact format:
            LABEL: <POSITIVE|NEGATIVE|NEUTRAL>
            SCORE: <number between -1.0 and 1.0>

            Example:
            LABEL: POSITIVE
            SCORE: 0.75
            """;

    @Override
    @Async("taskExecutor")
    public void analyzePostAsync(Long postId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            log.error("帖子不存在: postId={}", postId);
            return;
        }

        log.info("开始分析帖子情感（通义千问）: postId={}", postId);

        try {
            // 检查API Key
            if (apiKey == null || apiKey.isEmpty()) {
                log.error("通义千问API Key未配置，降级使用关键词分析");
                fallbackToKeywordAnalysis(post);
                return;
            }

            // 调用通义千问API
            String content = post.getContent();
            InternalEmotionResult result = analyzeWithQwen(content);

            // 更新帖子
            post.setEmotionLabel(result.label);
            post.setEmotionScore(java.math.BigDecimal.valueOf(result.score));
            post.setStatus(PostStatus.PUBLISHED.name());
            postMapper.updateById(post);
            invalidateStatsCache(post.getUserId());

            log.info("情感分析完成（通义千问）: postId={}, label={}, score={}",
                    postId, result.label, result.score);

        } catch (Exception e) {
            log.error("通义千问分析失败，降级使用关键词分析: postId=" + postId, e);
            fallbackToKeywordAnalysis(post);
        }
    }

    @Override
    public EmotionAnalysisService.EmotionResult analyzeText(String content) {
        try {
            if (apiKey == null || apiKey.isEmpty()) {
                return fallbackAnalyzeText(content);
            }

            InternalEmotionResult result = analyzeWithQwen(content);
            return new EmotionAnalysisService.EmotionResult(
                    result.score,
                    result.label,
                    "Analyzed by Qwen AI",
                    new String[0]);
        } catch (Exception e) {
            log.error("通义千问文本分析失败", e);
            return fallbackAnalyzeText(content);
        }
    }

    /**
     * 使用通义千问进行情感分析
     */
    private InternalEmotionResult analyzeWithQwen(String content) throws Exception {
        Generation gen = new Generation();

        Message systemMsg = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content(SYSTEM_PROMPT)
                .build();

        Message userMsg = Message.builder()
                .role(Role.USER.getValue())
                .content("Analyze the emotion of this text: " + content)
                .build();

        GenerationParam param = GenerationParam.builder()
                .apiKey(apiKey)
                .model(MODEL)
                .messages(Arrays.asList(systemMsg, userMsg))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .topP(0.8)
                .temperature(0.3f) // 降低随机性，提高稳定性
                .build();

        GenerationResult result = gen.call(param);
        String response = result.getOutput().getChoices().get(0).getMessage().getContent();

        return parseEmotionResult(response);
    }

    /**
     * 解析通义千问的响应
     */
    private InternalEmotionResult parseEmotionResult(String response) {
        InternalEmotionResult result = new InternalEmotionResult();

        // 解析LABEL
        Pattern labelPattern = Pattern.compile("LABEL:\\s*(POSITIVE|NEGATIVE|NEUTRAL)", Pattern.CASE_INSENSITIVE);
        Matcher labelMatcher = labelPattern.matcher(response);
        if (labelMatcher.find()) {
            result.label = labelMatcher.group(1).toUpperCase();
        } else {
            result.label = EmotionLabel.NEUTRAL.name();
        }

        // 解析SCORE
        Pattern scorePattern = Pattern.compile("SCORE:\\s*(-?\\d+\\.?\\d*)");
        Matcher scoreMatcher = scorePattern.matcher(response);
        if (scoreMatcher.find()) {
            result.score = Double.parseDouble(scoreMatcher.group(1));
            // 确保分数在[-1.0, 1.0]范围内
            result.score = Math.max(-1.0, Math.min(1.0, result.score));
        } else {
            result.score = 0.0;
        }

        log.debug("解析结果: label={}, score={}, rawResponse={}", result.label, result.score, response);
        return result;
    }

    /**
     * 降级方案：使用关键词分析
     */
    private void fallbackToKeywordAnalysis(Post post) {
        String content = post.getContent().toLowerCase();

        String[] positiveWords = { "happy", "joy", "love", "excellent", "wonderful", "great", "amazing",
                "开心", "快乐", "喜欢", "棒", "好", "美好", "幸福", "成功", "满足" };
        String[] negativeWords = { "sad", "angry", "hate", "terrible", "awful", "bad", "horrible",
                "难过", "生气", "讨厌", "糟糕", "差", "痛苦", "失望", "焦虑", "压力" };

        int positiveCount = 0;
        int negativeCount = 0;

        for (String word : positiveWords) {
            if (content.contains(word))
                positiveCount++;
        }

        for (String word : negativeWords) {
            if (content.contains(word))
                negativeCount++;
        }

        String label;
        double score;

        if (positiveCount > negativeCount) {
            label = EmotionLabel.POSITIVE.name();
            score = Math.min(0.9, 0.5 + positiveCount * 0.1);
        } else if (negativeCount > positiveCount) {
            label = EmotionLabel.NEGATIVE.name();
            score = Math.max(-0.9, -0.5 - negativeCount * 0.1);
        } else {
            label = EmotionLabel.NEUTRAL.name();
            score = 0.0;
        }

        post.setEmotionLabel(label);
        post.setEmotionScore(java.math.BigDecimal.valueOf(score));
        post.setStatus(PostStatus.PUBLISHED.name());
        postMapper.updateById(post);
        invalidateStatsCache(post.getUserId());

        log.info("情感分析完成（关键词降级）: postId={}, label={}, score={}",
                post.getId(), label, score);
    }

    /**
     * 文本分析的降级方案
     */
    private EmotionAnalysisService.EmotionResult fallbackAnalyzeText(String content) {
        String lowerContent = content.toLowerCase();

        String[] positiveWords = { "happy", "joy", "love", "excellent", "wonderful", "great", "amazing",
                "开心", "快乐", "喜欢", "棒", "好", "美好", "幸福", "成功", "满足" };
        String[] negativeWords = { "sad", "angry", "hate", "terrible", "awful", "bad", "horrible",
                "难过", "生气", "讨厌", "糟糕", "差", "痛苦", "失望", "焦虑", "压力" };

        int positiveCount = 0;
        int negativeCount = 0;

        for (String word : positiveWords) {
            if (lowerContent.contains(word))
                positiveCount++;
        }

        for (String word : negativeWords) {
            if (lowerContent.contains(word))
                negativeCount++;
        }

        String label;
        double score;

        if (positiveCount > negativeCount) {
            label = EmotionLabel.POSITIVE.name();
            score = Math.min(0.9, 0.5 + positiveCount * 0.1);
        } else if (negativeCount > positiveCount) {
            label = EmotionLabel.NEGATIVE.name();
            score = Math.max(-0.9, -0.5 - negativeCount * 0.1);
        } else {
            label = EmotionLabel.NEUTRAL.name();
            score = 0.0;
        }

        return new EmotionAnalysisService.EmotionResult(
                score,
                label,
                "Analyzed by keyword matching (fallback)",
                new String[0]);
    }

    /**
     * 情感分析结果内部类
     */
    private static class InternalEmotionResult {
        String label = EmotionLabel.NEUTRAL.name();
        Double score = 0.0;
    }

    private void invalidateStatsCache(Long userId) {
        cacheService.delete(CacheService.CacheKey.STATS_USER + userId);
        cacheService.delete(CacheService.CacheKey.STATS_PLATFORM);
    }
}
