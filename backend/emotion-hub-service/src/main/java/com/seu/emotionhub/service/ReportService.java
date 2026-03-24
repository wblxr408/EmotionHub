package com.seu.emotionhub.service;

import com.seu.emotionhub.model.dto.request.ReportCreateRequest;

/**
 * 举报服务
 */
public interface ReportService {

    void createReport(ReportCreateRequest request);
}
