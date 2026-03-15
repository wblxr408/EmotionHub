package com.seu.emotionhub.service;

import com.seu.emotionhub.model.dto.request.PostCreateRequest;
import com.seu.emotionhub.model.dto.request.PostQueryRequest;
import com.seu.emotionhub.model.dto.response.PageResult;
import com.seu.emotionhub.model.dto.response.PostVO;

/**
 * 帖子服务接口
 *
 * @author EmotionHub Team
 */
public interface PostService {

    /**
     * 发布帖子
     *
     * @param request 发帖请求
     * @return 帖子详情
     */
    PostVO createPost(PostCreateRequest request);

    /**
     * 查询帖子列表（分页）
     *
     * @param request 查询条件
     * @return 帖子列表
     */
    PageResult<PostVO> listPosts(PostQueryRequest request);

    /**
     * 获取帖子详情
     *
     * @param postId 帖子ID
     * @return 帖子详情
     */
    PostVO getPostDetail(Long postId);

    /**
     * 删除帖子（软删除）
     *
     * @param postId 帖子ID
     */
    void deletePost(Long postId);

    /**
     * 获取用户发布的帖子
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页数量
     * @return 帖子列表
     */
    PageResult<PostVO> getUserPosts(Long userId, Integer page, Integer size);
}
