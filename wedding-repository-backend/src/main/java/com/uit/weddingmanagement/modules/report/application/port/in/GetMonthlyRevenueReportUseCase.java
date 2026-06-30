package com.uit.weddingmanagement.modules.report.application.port.in;

import com.uit.weddingmanagement.modules.report.application.model.result.MonthlyRevenueReportResult;

public interface GetMonthlyRevenueReportUseCase {

  MonthlyRevenueReportResult getMonthlyRevenueReport(int month, int year);
}
