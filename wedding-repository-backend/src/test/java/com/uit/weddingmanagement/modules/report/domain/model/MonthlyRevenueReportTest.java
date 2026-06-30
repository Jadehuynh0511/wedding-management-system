package com.uit.weddingmanagement.modules.report.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;

class MonthlyRevenueReportTest {

  @Test
  void shouldCreateMonthlyReportWithConsistentAggregates() {
    MonthlyRevenueReport monthlyRevenueReport =
        MonthlyRevenueReport.from(
            YearMonth.of(2026, 6),
            Instant.parse("2026-06-02T04:00:00Z"),
            List.of(
                new DailyRevenueSummary(
                    LocalDate.of(2026, 6, 1), 1, new BigDecimal("10000000.00")),
                new DailyRevenueSummary(
                    LocalDate.of(2026, 6, 15), 2, new BigDecimal("25000000.00"))));

    assertThat(monthlyRevenueReport.totalRevenue()).isEqualByComparingTo("35000000.00");
    assertThat(monthlyRevenueReport.totalWeddingBookings()).isEqualTo(3);
    assertThat(monthlyRevenueReport.items()).hasSize(30);
  }

  @Test
  void shouldRejectWhenItemsContainDateOutsideReportMonth() {
    assertThatThrownBy(
            () ->
                new MonthlyRevenueReport(
                    6,
                    2026,
                    Instant.parse("2026-06-02T04:00:00Z"),
                    new BigDecimal("10000000.00"),
                    1,
                    List.of(
                        new MonthlyRevenueReportItem(
                            LocalDate.of(2026, 7, 1),
                            1,
                            new BigDecimal("10000000.00"),
                            new BigDecimal("100.0000")))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Monthly revenue report items must belong to the requested month.");
  }

  @Test
  void shouldRejectWhenTotalsDoNotMatchDetailRows() {
    assertThatThrownBy(
            () ->
                new MonthlyRevenueReport(
                    6,
                    2026,
                    Instant.parse("2026-06-02T04:00:00Z"),
                    new BigDecimal("20000000.00"),
                    1,
                    List.of(
                        new MonthlyRevenueReportItem(
                            LocalDate.of(2026, 6, 1),
                            1,
                            new BigDecimal("10000000.00"),
                            new BigDecimal("100.0000")))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Total revenue must equal the sum of report item revenues.");
  }
}
