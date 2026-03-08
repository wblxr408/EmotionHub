package com.seu.emotionhub.service;

/**
 * 情感分析服务接口
 *
 * @author EmotionHub Team
 */
public interface EmotionAnalysisService {

    /**
     * 异步分析帖子情感
     *
     * @param postId 帖子ID
     */
    void analyzePostAsync(Long postId);

    /**
     * 分析文本情感（同步）
     *
     * @param content 文本内容
     * @return 分析结果
     */
    EmotionResult analyzeText(String content);

    /**
     * 情感分析结果
     */
    class EmotionResult {
        private Double score;      // -1.0 到 1.0
        private String label;      // POSITIVE/NEUTRAL/NEGATIVE
        private String analysis;   // 分析说明
        private String[] keywords; // 关键词

        public EmotionResult(Double score, String label, String analysis, String[] keywords) {
            this.score = score;
            this.label = label;
            this.analysis = analysis;
            this.keywords = keywords;
        }

        public Double getScore() {
            return score;
        }

        public String getLabel() {
            return label;
        }

        public String getAnalysis() {
            return analysis;
        }

        public String[] getKeywords() {
            return keywords;
        }
    }
}
