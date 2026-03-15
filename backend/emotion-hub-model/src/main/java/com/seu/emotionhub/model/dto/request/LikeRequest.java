package com.seu.emotionhub.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 点赞请求DTO
 *
 * @author EmotionHub Team
 */
@Data
public class LikeRequest {

    /**
     * 目标ID（帖子或评论的ID）
     */
    @NotNull(message = "目标ID不能为空")
    private Long targetId;

    /**
     * 目标类型：POST-帖子 / COMMENT-评论
     */
    @NotBlank(message = "目标类型不能为空")
    private String targetType;
}
