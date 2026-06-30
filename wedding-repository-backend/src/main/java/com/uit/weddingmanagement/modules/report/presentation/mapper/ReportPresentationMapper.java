package com.uit.weddingmanagement.modules.report.presentation.mapper;

import com.uit.weddingmanagement.modules.report.application.model.result.MonthlyRevenueReportItemResult;
import com.uit.weddingmanagement.modules.report.application.model.result.MonthlyRevenueReportResult;
import com.uit.weddingmanagement.modules.report.presentation.dto.response.MonthlyRevenueReportItemResponse;
import com.uit.weddingmanagement.modules.report.presentation.dto.response.MonthlyRevenueReportResponse;

import org.springframework.stereotype.Component;

/**
 * Mapper chuyển đổi MonthlyRevenueReport Result objects sang presentation Response DTOs.
 * Được tách ra từ MonthlyRevenueReportController để tuân thủ SRP.
 */
@Component
public class ReportPresentationMapper {

    public MonthlyRevenueReportResponse toResponse(MonthlyRevenueReportResult result) {
        return new MonthlyRevenueReportResponse(
                result.reportMonth(),
                result.reportYear(),
                result.generatedAt(),
                result.totalRevenue(),
                result.totalWeddingBookings(),
                result.items().stream().map(this::toResponse).toList());
    }

    public MonthlyRevenueReportItemResponse toResponse(MonthlyRevenueReportItemResult result) {
        return new MonthlyRevenueReportItemResponse(
                result.reportDate(),
                result.weddingBookingCount(),
                result.revenue(),
                result.revenueRatio());
    }
}
