package com.uit.weddingmanagement.modules.report.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record MonthlyRevenueReport(
    int reportMonth,
    int reportYear,
    Instant generatedAt,
    BigDecimal totalRevenue,
    int totalWeddingBookings,
    List<MonthlyRevenueReportItem> items) {

  private static final BigDecimal ZERO_AMOUNT = new BigDecimal("0.00");
  private static final BigDecimal ZERO_RATIO = new BigDecimal("0.0000");
  private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
  private static final int REVENUE_RATIO_SCALE = 4;

  public MonthlyRevenueReport {
    // Domain object này giữ phần “luật” của BM5:
    // dữ liệu phải cùng tháng/năm, tổng phải khớp các dòng chi tiết, và không được có ngày trùng.
    if (reportMonth < 1 || reportMonth > 12) {
      throw new IllegalArgumentException("Report month must be between 1 and 12.");
    }

    if (reportYear < 2000 || reportYear > 2100) {
      throw new IllegalArgumentException("Report year must be between 2000 and 2100.");
    }

    if (generatedAt == null) {
      throw new IllegalArgumentException("Report generation time is required.");
    }

    if (totalRevenue == null) {
      throw new IllegalArgumentException("Total revenue is required.");
    }

    if (totalRevenue.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Total revenue must be greater than or equal to 0.");
    }

    if (totalWeddingBookings < 0) {
      throw new IllegalArgumentException(
          "Total wedding bookings must be greater than or equal to 0.");
    }

    if (items == null || items.isEmpty()) {
      throw new IllegalArgumentException("Monthly revenue report items are required.");
    }

    if (items.stream().anyMatch(item -> item == null)) {
      throw new IllegalArgumentException(
          "Monthly revenue report items cannot contain null values.");
    }

    validateItemsBelongToReportPeriod(YearMonth.of(reportYear, reportMonth), items);
    validateNoDuplicateReportDate(items);
    validateAggregateConsistency(totalRevenue, totalWeddingBookings, items);

    items = List.copyOf(items);
  }

  public static MonthlyRevenueReport from(
      YearMonth reportPeriod,
      Instant generatedAt,
      List<DailyRevenueSummary> dailyRevenueSummaries) {
    YearMonth normalizedReportPeriod = requireReportPeriod(reportPeriod);
    Instant normalizedGeneratedAt = requireGeneratedAt(generatedAt);
    List<DailyRevenueSummary> normalizedDailyRevenueSummaries =
        requireDailyRevenueSummaries(dailyRevenueSummaries);

    Map<LocalDate, DailyRevenueSummary> summariesByDate = new LinkedHashMap<>();

    // Gộp các dòng cùng ngày để domain không phụ thuộc tuyệt đối vào việc repository đã aggregate
    // “đẹp” hay chưa. Nếu sau này query thay đổi, báo cáo vẫn giữ đúng contract.
    for (DailyRevenueSummary dailyRevenueSummary : normalizedDailyRevenueSummaries) {
      if (!YearMonth.from(dailyRevenueSummary.reportDate()).equals(normalizedReportPeriod)) {
        throw new IllegalArgumentException(
            "Daily revenue summary contains a date outside the requested month.");
      }

      summariesByDate.merge(
          dailyRevenueSummary.reportDate(), dailyRevenueSummary, MonthlyRevenueReport::merge);
    }

    BigDecimal totalRevenue =
        summariesByDate.values().stream()
            .map(DailyRevenueSummary::revenue)
            .reduce(ZERO_AMOUNT, BigDecimal::add);
    int totalWeddingBookings =
        summariesByDate.values().stream().mapToInt(DailyRevenueSummary::weddingBookingCount).sum();

    List<MonthlyRevenueReportItem> items = new ArrayList<>(normalizedReportPeriod.lengthOfMonth());

    // Điền đủ toàn bộ ngày trong tháng để contract API ổn định: frontend không phải tự bù ngày thiếu
    // và có thể render bảng/biểu đồ theo lịch một cách nhất quán.
    for (int dayOfMonth = 1; dayOfMonth <= normalizedReportPeriod.lengthOfMonth(); dayOfMonth++) {
      LocalDate reportDate = normalizedReportPeriod.atDay(dayOfMonth);
      DailyRevenueSummary dailyRevenueSummary =
          summariesByDate.getOrDefault(reportDate, DailyRevenueSummary.zero(reportDate));
      items.add(
          new MonthlyRevenueReportItem(
              reportDate,
              dailyRevenueSummary.weddingBookingCount(),
              dailyRevenueSummary.revenue(),
              calculateRevenueRatio(dailyRevenueSummary.revenue(), totalRevenue)));
    }

    return new MonthlyRevenueReport(
        normalizedReportPeriod.getMonthValue(),
        normalizedReportPeriod.getYear(),
        normalizedGeneratedAt,
        totalRevenue,
        totalWeddingBookings,
        items);
  }

  private static YearMonth requireReportPeriod(YearMonth reportPeriod) {
    if (reportPeriod == null) {
      throw new IllegalArgumentException("Report period is required.");
    }

    return reportPeriod;
  }

  private static Instant requireGeneratedAt(Instant generatedAt) {
    if (generatedAt == null) {
      throw new IllegalArgumentException("Report generation time is required.");
    }

    return generatedAt;
  }

  private static List<DailyRevenueSummary> requireDailyRevenueSummaries(
      List<DailyRevenueSummary> dailyRevenueSummaries) {
    if (dailyRevenueSummaries == null) {
      throw new IllegalArgumentException("Daily revenue summaries are required.");
    }

    if (dailyRevenueSummaries.stream().anyMatch(summary -> summary == null)) {
      throw new IllegalArgumentException("Daily revenue summaries cannot contain null values.");
    }

    return dailyRevenueSummaries;
  }

  private static void validateItemsBelongToReportPeriod(
      YearMonth reportPeriod, List<MonthlyRevenueReportItem> items) {
    boolean hasOffMonthItem =
        items.stream()
            .map(MonthlyRevenueReportItem::reportDate)
            .anyMatch(reportDate -> !YearMonth.from(reportDate).equals(reportPeriod));

    if (hasOffMonthItem) {
      throw new IllegalArgumentException(
          "Monthly revenue report items must belong to the requested month.");
    }
  }

  private static void validateNoDuplicateReportDate(List<MonthlyRevenueReportItem> items) {
    Set<LocalDate> uniqueDates = new HashSet<>();
    boolean hasDuplicateDate =
        items.stream().map(MonthlyRevenueReportItem::reportDate).anyMatch(reportDate -> !uniqueDates.add(reportDate));

    if (hasDuplicateDate) {
      throw new IllegalArgumentException(
          "Monthly revenue report items must not contain duplicate report dates.");
    }
  }

  private static void validateAggregateConsistency(
      BigDecimal totalRevenue,
      int totalWeddingBookings,
      List<MonthlyRevenueReportItem> items) {
    BigDecimal aggregatedRevenue =
        items.stream().map(MonthlyRevenueReportItem::revenue).reduce(ZERO_AMOUNT, BigDecimal::add);
    int aggregatedWeddingBookings =
        items.stream().mapToInt(MonthlyRevenueReportItem::weddingBookingCount).sum();

    if (aggregatedRevenue.compareTo(totalRevenue) != 0) {
      throw new IllegalArgumentException(
          "Total revenue must equal the sum of report item revenues.");
    }

    if (aggregatedWeddingBookings != totalWeddingBookings) {
      throw new IllegalArgumentException(
          "Total wedding bookings must equal the sum of report item counts.");
    }
  }

  private static DailyRevenueSummary merge(DailyRevenueSummary left, DailyRevenueSummary right) {
    return new DailyRevenueSummary(
        left.reportDate(),
        left.weddingBookingCount() + right.weddingBookingCount(),
        left.revenue().add(right.revenue()));
  }

  private static BigDecimal calculateRevenueRatio(BigDecimal revenue, BigDecimal totalRevenue) {
    if (revenue.signum() == 0 || totalRevenue.signum() == 0) {
      return ZERO_RATIO;
    }

    return revenue
        .multiply(ONE_HUNDRED)
        .divide(totalRevenue, REVENUE_RATIO_SCALE, RoundingMode.HALF_UP);
  }
}
