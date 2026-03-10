package com.seu.emotionhub.service.impl;

import com.seu.emotionhub.service.SensitiveWordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.*;

/**
 * 敏感词过滤服务实现
 * 基于DFA（确定有穷自动机）算法
 *
 * @author EmotionHub Team
 */
@Slf4j
@Service
public class SensitiveWordServiceImpl implements SensitiveWordService {

    /**
     * 敏感词库 - DFA算法的Trie树结构
     */
    private Map<String, Object> sensitiveWordMap;

    /**
     * 最小匹配规则
     */
    private static final int MIN_MATCH_TYPE = 1;

    /**
     * 最大匹配规则
     */
    private static final int MAX_MATCH_TYPE = 2;

    /**
     * 是否是结束标识
     */
    private static final String IS_END = "isEnd";

    @PostConstruct
    @Override
    public void init() {
        // 初始化敏感词库
        Set<String> sensitiveWords = loadSensitiveWords();
        buildSensitiveWordMap(sensitiveWords);
        log.info("敏感词库初始化完成，共加载 {} 个敏感词", sensitiveWords.size());
    }

    /**
     * 加载敏感词列表
     * 实际项目中应从数据库或配置文件读取
     */
    private Set<String> loadSensitiveWords() {
        Set<String> words = new HashSet<>();

        // 政治敏感词
        words.add("习近平");
        words.add("毛泽东");
        words.add("共产党");
        words.add("台独");
        words.add("法轮功");

        // 违法犯罪
        words.add("毒品");
        words.add("枪支");
        words.add("走私");
        words.add("诈骗");
        words.add("传销");

        // 暴力色情
        words.add("杀人");
        words.add("自杀");
        words.add("色情");
        words.add("赌博");

        // 侮辱性词汇
        words.add("傻逼");
        words.add("草泥马");
        words.add("fuck");
        words.add("shit");

        // 其他敏感词
        words.add("病毒");
        words.add("疫情");
        words.add("泄密");

        return words;
    }

    /**
     * 构建敏感词DFA Map
     */
    @SuppressWarnings("unchecked")
    private void buildSensitiveWordMap(Set<String> sensitiveWords) {
        sensitiveWordMap = new HashMap<>(sensitiveWords.size());

        for (String word : sensitiveWords) {
            Map<String, Object> currentMap = sensitiveWordMap;
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                String key = String.valueOf(c);

                Map<String, Object> subMap = (Map<String, Object>) currentMap.get(key);
                if (subMap == null) {
                    subMap = new HashMap<>();
                    currentMap.put(key, subMap);
                }

                currentMap = subMap;

                if (i == word.length() - 1) {
                    currentMap.put(IS_END, true);
                }
            }
        }
    }

    @Override
    public boolean contains(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        for (int i = 0; i < text.length(); i++) {
            int matchLength = checkSensitiveWord(text, i, MIN_MATCH_TYPE);
            if (matchLength > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String filter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        int i = 0;

        while (i < text.length()) {
            int matchLength = checkSensitiveWord(text, i, MAX_MATCH_TYPE);
            if (matchLength > 0) {
                // 找到敏感词，用***替换
                result.append("***");
                i += matchLength;
            } else {
                result.append(text.charAt(i));
                i++;
            }
        }

        return result.toString();
    }

    @Override
    public Set<String> getSensitiveWords(String text) {
        Set<String> words = new HashSet<>();
        if (text == null || text.isEmpty()) {
            return words;
        }

        for (int i = 0; i < text.length(); i++) {
            int matchLength = checkSensitiveWord(text, i, MAX_MATCH_TYPE);
            if (matchLength > 0) {
                words.add(text.substring(i, i + matchLength));
                i += matchLength - 1;
            }
        }

        return words;
    }

    /**
     * 检查文本中是否包含敏感词
     *
     * @param text      待检查文本
     * @param beginIndex 起始索引
     * @param matchType  匹配类型（1:最小匹配，2:最大匹配）
     * @return 匹配到的敏感词长度，0表示未匹配
     */
    @SuppressWarnings("unchecked")
    private int checkSensitiveWord(String text, int beginIndex, int matchType) {
        int matchLength = 0;
        Map<String, Object> currentMap = sensitiveWordMap;

        for (int i = beginIndex; i < text.length(); i++) {
            char c = text.charAt(i);
            String key = String.valueOf(c);

            currentMap = (Map<String, Object>) currentMap.get(key);

            if (currentMap == null) {
                // 未匹配到
                break;
            }

            matchLength++;

            if (Boolean.TRUE.equals(currentMap.get(IS_END))) {
                // 匹配到敏感词结尾
                if (matchType == MIN_MATCH_TYPE) {
                    // 最小匹配，立即返回
                    break;
                }
                // 最大匹配，继续查找更长的匹配
            }
        }

        return matchLength;
    }
}
