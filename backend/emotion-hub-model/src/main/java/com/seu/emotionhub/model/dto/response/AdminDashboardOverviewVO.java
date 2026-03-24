package com.seu.emotionhub.model.dto.response;

import lombok.Data;

/**
 * 后台概览统计
 */
@Data
public class AdminDashboardOverviewVO {

    private Long totalUsers;

    private Long todayUsers;

    private Long totalPosts;

    private Long todayPosts;

    private Long pendingReports;

    private Long bannedUsers;
}
