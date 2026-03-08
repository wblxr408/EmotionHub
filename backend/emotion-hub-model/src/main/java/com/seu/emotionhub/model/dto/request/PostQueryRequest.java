package com.seu.emotionhub.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 帖子查询请求DTO
 *
 * @author EmotionHub Team
 */
@Data
public class PostQueryRequest {

    /**
     * 页码（从1开始）
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer size = 10;

    /**
     * 情感标签过滤（可选）
     * POSITIVE / NEUTRAL / NEGATIVE
     */
    private String emotionLabel;

    /**
     * 排序方式（可选）
     * LATEST-最新 / HOT-最热
     */
    private String orderBy = "LATEST";

    /**
     * 关键词搜索（可选）
     */
    private String keyword;

    /**
     * 用户ID过滤（可选）
     * 查询指定用户的帖子
     */
    private Long userId;
}
