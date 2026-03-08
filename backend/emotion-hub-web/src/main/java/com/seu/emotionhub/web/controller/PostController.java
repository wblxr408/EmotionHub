package com.seu.emotionhub.web.controller;

import com.seu.emotionhub.common.result.Result;
import com.seu.emotionhub.model.dto.request.PostCreateRequest;
import com.seu.emotionhub.model.dto.request.PostQueryRequest;
import com.seu.emotionhub.model.dto.response.PageResult;
import com.seu.emotionhub.model.dto.response.PostVO;
import com.seu.emotionhub.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 帖子Controller
 * 处理帖子的发布、查询、删除等操作
 *
 * @author EmotionHub Team
 */
@Slf4j
@RestController
@RequestMapping("/api/post")
@RequiredArgsConstructor
@Tag(name = "帖子管理", description = "帖子发布、查询、删除等操作")
public class PostController {

    private final PostService postService;

    /**
     * 发布帖子
     */
    @PostMapping("/create")
    @Operation(summary = "发布帖子", description = "用户发布新帖子，系统将自动进行情感分析")
    public Result<PostVO> createPost(@Valid @RequestBody PostCreateRequest request) {
        log.info("发布帖子请求: content length={}", request.getContent().length());
        PostVO post = postService.createPost(request);
        return Result.success("发布成功", post);
    }

    /**
     * 查询帖子列表（分页）
     */
    @GetMapping("/list")
    @Operation(summary = "查询帖子列表", description = "分页查询帖子列表，支持按情感标签过滤和排序")
    public Result<PageResult<PostVO>> listPosts(@Valid PostQueryRequest request) {
        log.info("查询帖子列表: page={}, size={}, emotionLabel={}, orderBy={}",
                request.getPage(), request.getSize(), request.getEmotionLabel(), request.getOrderBy());
        PageResult<PostVO> result = postService.listPosts(request);
        return Result.success(result);
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/{postId}")
    @Operation(summary = "获取帖子详情", description = "根据帖子ID获取详细信息，浏览量+1")
    public Result<PostVO> getPostDetail(@PathVariable Long postId) {
        log.info("获取帖子详情: postId={}", postId);
        PostVO post = postService.getPostDetail(postId);
        return Result.success(post);
    }

    /**
     * 删除帖子
     */
    @DeleteMapping("/{postId}")
    @Operation(summary = "删除帖子", description = "软删除帖子，只能删除自己发布的帖子")
    public Result<Void> deletePost(@PathVariable Long postId) {
        log.info("删除帖子: postId={}", postId);
        postService.deletePost(postId);
        return Result.success("删除成功", null);
    }

    /**
     * 获取用户发布的帖子
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户帖子", description = "获取指定用户发布的所有帖子")
    public Result<PageResult<PostVO>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        log.info("获取用户帖子: userId={}, page={}, size={}", userId, page, size);
        PageResult<PostVO> result = postService.getUserPosts(userId, page, size);
        return Result.success(result);
    }
}
