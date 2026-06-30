package com.uit.weddingmanagement.modules.report.application.usecase;

import com.uit.weddingmanagement.modules.report.application.model.result.MonthlyRevenueReportResult;
import com.uit.weddingmanagement.modules.report.application.port.in.GetMonthlyRevenueReportUseCase;
import com.uit.weddingmanagement.modules.report.application.port.out.MonthlyRevenueReportQueryPort;
import com.uit.weddingmanagement.modules.report.domain.model.MonthlyRevenueReport;
import java.time.Instant;
import java.time.YearMonth;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetMonthlyRevenueReportService implements GetMonthlyRevenueReportUseCase {

  private final MonthlyRevenueReportQueryPort monthlyRevenueReportQueryPort;

  public GetMonthlyRevenueReportService(
      MonthlyRevenueReportQueryPort monthlyRevenueReportQueryPort) {
    this.monthlyRevenueReportQueryPort = monthlyRevenueReportQueryPort;
  }

  @Override
  public MonthlyRevenueReportResult getMonthlyRevenueReport(int month, int year) {
    // Application layer chỉ điều phối: validate input thô, lấy dữ liệu aggregate và giao
    // cho domain dựng báo cáo BM5 hoàn chỉnh.
    YearMonth reportPeriod = toReportPeriod(month, year);
    MonthlyRevenueReport monthlyRevenueReport =
        MonthlyRevenueReport.from(
            reportPeriod,
            Instant.now(),
            monthlyRevenueReportQueryPort.findDailyRevenueSummaries(reportPeriod));
    return MonthlyRevenueReportResult.from(monthlyRevenueReport);
  }

  private YearMonth toReportPeriod(int month, int year) {
    if (month < 1 || month > 12) {
      throw new IllegalArgumentException("Report month must be between 1 and 12.");
    }

    if (year < 2000 || year > 2100) {
      throw new IllegalArgumentException("Report year must be between 2000 and 2100.");
    }

    return YearMonth.of(year, month);
  }
}
