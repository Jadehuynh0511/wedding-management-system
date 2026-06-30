package com.uit.weddingmanagement.modules.report.application.model.result;

import com.uit.weddingmanagement.modules.report.domain.model.MonthlyRevenueReportItem;
import java.math.BigDecimal;
import java.time.LocalDate;

public record MonthlyRevenueReportItemResult(
    LocalDate reportDate, int weddingBookingCount, BigDecimal revenue, BigDecimal revenueRatio) {

  public static MonthlyRevenueReportItemResult from(
      MonthlyRevenueReportItem monthlyRevenueReportItem) {
    return new MonthlyRevenueReportItemResult(
        monthlyRevenueReportItem.reportDate(),
        monthlyRevenueReportItem.weddingBookingCount(),
        monthlyRevenueReportItem.revenue(),
        monthlyRevenueReportItem.revenueRatio());
  }
}
