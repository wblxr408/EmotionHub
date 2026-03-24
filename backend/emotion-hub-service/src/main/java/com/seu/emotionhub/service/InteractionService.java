package com.seu.emotionhub.service;

import com.seu.emotionhub.model.dto.request.CommentCreateRequest;
import com.seu.emotionhub.model.dto.response.CommentVO;

import java.util.List;

/**
 * 互动服务接口（点赞、评论）
 *
 * @author EmotionHub Team
 */
public interface InteractionService {

    /**
     * 点赞/取消点赞（幂等操作）
     *
     * @param targetId   目标ID（帖子或评论）
     * @param targetType 目标类型（POST/COMMENT）
     * @return true-点赞成功，false-取消点赞成功
     */
    boolean toggleLike(Long targetId, String targetType);

    /**
     * 检查是否已点赞
     *
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @return true-已点赞，false-未点赞
     */
    boolean isLiked(Long targetId, String targetType);

    /**
     * 发表评论
     *
     * @param request 评论请求
     * @return 评论详情
     */
    CommentVO createComment(CommentCreateRequest request);

    /**
     * 查询帖子的评论列表
     *
     * @param postId 帖子ID
     * @return 评论列表（树形结构）
     */
    List<CommentVO> listComments(Long postId);

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     */
    void deleteComment(Long commentId);

    /**
     * 管理员删除评论
     *
     * @param commentId 评论ID
     */
    void adminDeleteComment(Long commentId);

    /**
     * 获取评论详情
     *
     * @param commentId 评论ID
     * @return 评论详情
     */
    CommentVO getComment(Long commentId);
}
