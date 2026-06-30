package com.uit.weddingmanagement.modules.report.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyRevenueSummary(
    LocalDate reportDate, int weddingBookingCount, BigDecimal revenue) {

  private static final BigDecimal ZERO_AMOUNT = new BigDecimal("0.00");

  public DailyRevenueSummary {
    // Đây là aggregate thô đi lên từ persistence, nên domain chặn dữ liệu âm/null từ sớm
    // để các bước tính báo cáo phía sau không phải defensive lặp lại.
    if (reportDate == null) {
      throw new IllegalArgumentException("Report date is required.");
    }

    if (weddingBookingCount < 0) {
      throw new IllegalArgumentException(
          "Wedding booking count must be greater than or equal to 0.");
    }

    if (revenue == null) {
      throw new IllegalArgumentException("Revenue is required.");
    }

    if (revenue.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Revenue must be greater than or equal to 0.");
    }
  }

  public static DailyRevenueSummary zero(LocalDate reportDate) {
    // BM5 cần hiển thị cả ngày không có doanh thu để bảng/biểu đồ không bị khuyết dữ liệu.
    return new DailyRevenueSummary(reportDate, 0, ZERO_AMOUNT);
  }
}
