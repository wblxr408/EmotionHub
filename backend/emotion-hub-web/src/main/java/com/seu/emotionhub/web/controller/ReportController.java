package com.seu.emotionhub.web.controller;

import com.seu.emotionhub.common.result.Result;
import com.seu.emotionhub.model.dto.request.ReportCreateRequest;
import com.seu.emotionhub.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 举报接口
 */
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Tag(name = "举报管理", description = "用户举报相关接口")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @Operation(summary = "提交举报", description = "用户提交帖子或评论举报")
    public Result<Void> createReport(@Valid @RequestBody ReportCreateRequest request) {
        reportService.createReport(request);
        return Result.success("举报已提交", null);
    }
}
