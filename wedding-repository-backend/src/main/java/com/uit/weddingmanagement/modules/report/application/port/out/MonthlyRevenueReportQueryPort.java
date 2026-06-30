package com.uit.weddingmanagement.modules.report.application.port.out;

import com.uit.weddingmanagement.modules.report.domain.model.DailyRevenueSummary;
import java.time.YearMonth;
import java.util.List;

public interface MonthlyRevenueReportQueryPort {

  List<DailyRevenueSummary> findDailyRevenueSummaries(YearMonth reportPeriod);
}
