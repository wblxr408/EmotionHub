package com.seu.emotionhub.service;

/**
 * 敏感词过滤服务接口
 *
 * @author EmotionHub Team
 */
public interface SensitiveWordService {

    /**
     * 初始化敏感词库
     */
    void init();

    /**
     * 检查文本是否包含敏感词
     *
     * @param text 待检查文本
     * @return true-包含敏感词，false-不包含
     */
    boolean contains(String text);

    /**
     * 过滤敏感词，替换为***
     *
     * @param text 待过滤文本
     * @return 过滤后的文本
     */
    String filter(String text);

    /**
     * 获取文本中的所有敏感词
     *
     * @param text 待检查文本
     * @return 敏感词列表
     */
    java.util.Set<String> getSensitiveWords(String text);
}
