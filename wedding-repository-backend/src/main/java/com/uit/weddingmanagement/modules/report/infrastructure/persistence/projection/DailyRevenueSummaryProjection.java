package com.uit.weddingmanagement.modules.report.infrastructure.persistence.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface DailyRevenueSummaryProjection {

  LocalDate getReportDate();

  Long getWeddingBookingCount();

  BigDecimal getRevenue();
}
