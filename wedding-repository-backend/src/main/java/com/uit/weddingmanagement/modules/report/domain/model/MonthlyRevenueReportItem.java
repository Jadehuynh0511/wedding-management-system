package com.uit.weddingmanagement.modules.report.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MonthlyRevenueReportItem(
    LocalDate reportDate, int weddingBookingCount, BigDecimal revenue, BigDecimal revenueRatio) {

  public MonthlyRevenueReportItem {
    // Mỗi item là một dòng báo cáo BM5 đã hoàn chỉnh, vì vậy invariant của nó cần tự đứng vững
    // thay vì phụ thuộc vào service/controller bên ngoài.
    if (reportDate == null) {
      throw new IllegalArgumentException("Report item date is required.");
    }

    if (weddingBookingCount < 0) {
      throw new IllegalArgumentException(
          "Report item wedding booking count must be greater than or equal to 0.");
    }

    if (revenue == null) {
      throw new IllegalArgumentException("Report item revenue is required.");
    }

    if (revenue.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Report item revenue must be greater than or equal to 0.");
    }

    if (revenueRatio == null) {
      throw new IllegalArgumentException("Report item revenue ratio is required.");
    }

    if (revenueRatio.compareTo(BigDecimal.ZERO) < 0
        || revenueRatio.compareTo(new BigDecimal("100")) > 0) {
      throw new IllegalArgumentException("Report item revenue ratio must be between 0 and 100.");
    }
  }
}
