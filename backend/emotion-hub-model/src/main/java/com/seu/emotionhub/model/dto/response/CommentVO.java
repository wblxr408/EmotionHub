package com.seu.emotionhub.model.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论详情VO
 * 支持树形结构展示
 *
 * @author EmotionHub Team
 */
@Data
public class CommentVO {

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 父评论ID
     */
    private Long parentId;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 情感分数
     */
    private BigDecimal emotionScore;

    /**
     * 情感标签
     */
    private String emotionLabel;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 当前用户是否已点赞
     */
    private Boolean liked;

    /**
     * 子评论列表（树形结构）
     */
    private List<CommentVO> children;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
