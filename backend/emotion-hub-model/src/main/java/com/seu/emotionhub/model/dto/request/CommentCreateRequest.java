package com.seu.emotionhub.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 发表评论请求DTO
 *
 * @author EmotionHub Team
 */
@Data
public class CommentCreateRequest {

    /**
     * 帖子ID
     */
    @NotNull(message = "帖子ID不能为空")
    private Long postId;

    /**
     * 父评论ID（可选）
     * 为null表示一级评论，不为null表示回复某条评论
     */
    private Long parentId;

    /**
     * 评论内容
     * 要求：1-500字
     */
    @NotBlank(message = "评论内容不能为空")
    @Size(min = 1, max = 500, message = "评论内容长度必须在1-500字之间")
    private String content;
}
