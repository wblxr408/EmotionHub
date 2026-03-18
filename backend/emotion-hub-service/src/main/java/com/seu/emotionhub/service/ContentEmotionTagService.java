package com.seu.emotionhub.service;

import com.seu.emotionhub.model.entity.ContentEmotionTag;

import java.util.Collection;
import java.util.Map;

/**
 * 内容情感标签服务
 *
 * @author EmotionHub Team
 */
public interface ContentEmotionTagService {

    /**
     * 获取帖子标签映射
     *
     * @param postIds 帖子ID集合
     * @return postId -> ContentEmotionTag
     */
    Map<Long, ContentEmotionTag> getTagsByPostIds(Collection<Long> postIds);

    /**
     * 刷新内容情感标签库
     */
    void refreshContentTags();
}
