package com.seu.emotionhub.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 发布帖子请求DTO
 *
 * @author EmotionHub Team
 */
@Data
public class PostCreateRequest {

    /**
     * 帖子内容
     * 要求：1-2000字
     */
    @NotBlank(message = "帖子内容不能为空")
    @Size(min = 1, max = 2000, message = "帖子内容长度必须在1-2000字之间")
    private String content;

    /**
     * 图片URLs列表（可选）
     * 最多9张图片
     */
    @Size(max = 9, message = "最多上传9张图片")
    private List<String> images;
}
