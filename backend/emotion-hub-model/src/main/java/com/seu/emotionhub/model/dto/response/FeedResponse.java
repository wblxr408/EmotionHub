package com.seu.emotionhub.model.dto.response;

import lombok.Data;

import java.util.List;

/**
 * Feed 响应 DTO
 *
 * @author EmotionHub Team
 */
@Data
public class FeedResponse {

    /** 用户ID */
    private Long userId;

    /** 实际执行的推荐策略（A/B 测试后的策略） */
    private String strategy;

    /** 用户当前情感状态 */
    private String emotionState;

    /** 当前页码（从0开始） */
    private Integer page;

    /** 每页数量 */
    private Integer size;

    /** 帖子列表 */
    private List<PostVO> items;
}
