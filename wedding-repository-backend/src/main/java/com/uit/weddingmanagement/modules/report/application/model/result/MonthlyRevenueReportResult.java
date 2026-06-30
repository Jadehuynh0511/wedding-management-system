package com.uit.weddingmanagement.modules.report.application.model.result;

import com.uit.weddingmanagement.modules.report.domain.model.MonthlyRevenueReport;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record MonthlyRevenueReportResult(
    int reportMonth,
    int reportYear,
    Instant generatedAt,
    BigDecimal totalRevenue,
    int totalWeddingBookings,
    List<MonthlyRevenueReportItemResult> items) {

  public static MonthlyRevenueReportResult from(MonthlyRevenueReport monthlyRevenueReport) {
    return new MonthlyRevenueReportResult(
        monthlyRevenueReport.reportMonth(),
        monthlyRevenueReport.reportYear(),
        monthlyRevenueReport.generatedAt(),
        monthlyRevenueReport.totalRevenue(),
        monthlyRevenueReport.totalWeddingBookings(),
        monthlyRevenueReport.items().stream().map(MonthlyRevenueReportItemResult::from).toList());
  }
}
