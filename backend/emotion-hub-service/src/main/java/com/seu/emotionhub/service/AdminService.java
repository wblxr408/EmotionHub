package com.seu.emotionhub.service;

import com.seu.emotionhub.model.dto.request.ReportHandleRequest;
import com.seu.emotionhub.model.dto.response.AdminDashboardOverviewVO;
import com.seu.emotionhub.model.dto.response.AdminOperationLogVO;
import com.seu.emotionhub.model.dto.response.AdminReportVO;
import com.seu.emotionhub.model.dto.response.PageResult;
import com.seu.emotionhub.model.dto.response.PostVO;
import com.seu.emotionhub.model.dto.response.UserInfoVO;

/**
 * 管理后台服务
 */
public interface AdminService {

    PageResult<UserInfoVO> listUsers(Integer page, Integer size, String keyword, String status);

    UserInfoVO getUserDetail(Long userId);

    void updateUserStatus(Long userId, String status);

    PageResult<PostVO> listPosts(Integer page, Integer size, String keyword, String status, Long userId);

    void updatePostStatus(Long postId, String status);

    void deleteComment(Long commentId);

    PageResult<AdminReportVO> listReports(Integer page, Integer size, String status, String targetType);

    AdminReportVO getReportDetail(Long reportId);

    void handleReport(Long reportId, ReportHandleRequest request);

    AdminDashboardOverviewVO getDashboardOverview();

    PageResult<AdminOperationLogVO> listOperationLogs(Integer page, Integer size, Long operatorId, String action);
}
