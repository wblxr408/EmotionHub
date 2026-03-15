package com.seu.emotionhub.web.controller;

import com.seu.emotionhub.common.result.Result;
import com.seu.emotionhub.model.dto.request.CommentCreateRequest;
import com.seu.emotionhub.model.dto.request.LikeRequest;
import com.seu.emotionhub.model.dto.response.CommentVO;
import com.seu.emotionhub.service.InteractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 互动Controller
 * 处理点赞、评论等互动操作
 *
 * @author EmotionHub Team
 */
@Slf4j
@RestController
@RequestMapping("/api/interaction")
@RequiredArgsConstructor
@Tag(name = "互动管理", description = "点赞、评论等互动功能")
public class InteractionController {

    private final InteractionService interactionService;

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/like")
    @Operation(summary = "点赞/取消点赞", description = "点赞或取消点赞帖子/评论（幂等操作）")
    public Result<Map<String, Object>> toggleLike(@Valid @RequestBody LikeRequest request) {
        log.info("点赞请求: targetId={}, targetType={}", request.getTargetId(), request.getTargetType());
        boolean liked = interactionService.toggleLike(request.getTargetId(), request.getTargetType());

        Map<String, Object> data = new HashMap<>();
        data.put("liked", liked);
        data.put("message", liked ? "点赞成功" : "取消点赞成功");

        return Result.success(data);
    }

    /**
     * 检查是否已点赞
     */
    @GetMapping("/like/check")
    @Operation(summary = "检查是否已点赞", description = "检查当前用户是否已对目标点赞")
    public Result<Map<String, Boolean>> checkLike(@RequestParam Long targetId,
                                                    @RequestParam String targetType) {
        boolean liked = interactionService.isLiked(targetId, targetType);
        Map<String, Boolean> data = new HashMap<>();
        data.put("liked", liked);
        return Result.success(data);
    }

    /**
     * 发表评论
     */
    @PostMapping("/comment")
    @Operation(summary = "发表评论", description = "对帖子发表评论或回复其他评论")
    public Result<CommentVO> createComment(@Valid @RequestBody CommentCreateRequest request) {
        log.info("发表评论: postId={}, parentId={}", request.getPostId(), request.getParentId());
        CommentVO comment = interactionService.createComment(request);
        return Result.success("评论成功", comment);
    }

    /**
     * 查询评论列表
     */
    @GetMapping("/comment/list")
    @Operation(summary = "查询评论列表", description = "获取帖子的所有评论（树形结构）")
    public Result<List<CommentVO>> listComments(@RequestParam Long postId) {
        log.info("查询评论列表: postId={}", postId);
        List<CommentVO> comments = interactionService.listComments(postId);
        return Result.success(comments);
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/comment/{commentId}")
    @Operation(summary = "删除评论", description = "删除自己发表的评论（级联删除子评论）")
    public Result<Void> deleteComment(@PathVariable Long commentId) {
        log.info("删除评论: commentId={}", commentId);
        interactionService.deleteComment(commentId);
        return Result.success("删除成功", null);
    }

    /**
     * 获取评论详情
     */
    @GetMapping("/comment/{commentId}")
    @Operation(summary = "获取评论详情", description = "根据ID获取评论详情")
    public Result<CommentVO> getComment(@PathVariable Long commentId) {
        log.info("获取评论详情: commentId={}", commentId);
        CommentVO comment = interactionService.getComment(commentId);
        return Result.success(comment);
    }
}
