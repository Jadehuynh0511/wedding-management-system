package com.uit.weddingmanagement.modules.report.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.modules.report.application.port.out.MonthlyRevenueReportQueryPort;
import com.uit.weddingmanagement.modules.report.domain.model.DailyRevenueSummary;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetMonthlyRevenueReportServiceTest {

  @Mock private MonthlyRevenueReportQueryPort monthlyRevenueReportQueryPort;

  @Test
  void shouldBuildMonthlyRevenueReportAndFillMissingDates() {
    YearMonth reportPeriod = YearMonth.of(2026, 5);
    when(monthlyRevenueReportQueryPort.findDailyRevenueSummaries(reportPeriod))
        .thenReturn(
            List.of(
                new DailyRevenueSummary(LocalDate.of(2026, 5, 3), 1, new BigDecimal("10000000.00")),
                new DailyRevenueSummary(LocalDate.of(2026, 5, 3), 1, new BigDecimal("2500000.00")),
                new DailyRevenueSummary(
                    LocalDate.of(2026, 5, 5), 2, new BigDecimal("15000000.00"))));

    GetMonthlyRevenueReportService getMonthlyRevenueReportService =
        new GetMonthlyRevenueReportService(monthlyRevenueReportQueryPort);

    var result = getMonthlyRevenueReportService.getMonthlyRevenueReport(5, 2026);

    assertThat(result.reportMonth()).isEqualTo(5);
    assertThat(result.reportYear()).isEqualTo(2026);
    assertThat(result.generatedAt()).isNotNull();
    assertThat(result.totalRevenue()).isEqualByComparingTo("27500000.00");
    assertThat(result.totalWeddingBookings()).isEqualTo(4);
    assertThat(result.items()).hasSize(31);

    assertThat(result.items().getFirst().reportDate()).isEqualTo(LocalDate.of(2026, 5, 1));
    assertThat(result.items().getFirst().weddingBookingCount()).isZero();
    assertThat(result.items().getFirst().revenue()).isEqualByComparingTo("0.00");
    assertThat(result.items().getFirst().revenueRatio()).isEqualByComparingTo("0.0000");

    assertThat(result.items().get(2).reportDate()).isEqualTo(LocalDate.of(2026, 5, 3));
    assertThat(result.items().get(2).weddingBookingCount()).isEqualTo(2);
    assertThat(result.items().get(2).revenue()).isEqualByComparingTo("12500000.00");
    assertThat(result.items().get(2).revenueRatio()).isEqualByComparingTo("45.4545");

    assertThat(result.items().get(4).reportDate()).isEqualTo(LocalDate.of(2026, 5, 5));
    assertThat(result.items().get(4).weddingBookingCount()).isEqualTo(2);
    assertThat(result.items().get(4).revenue()).isEqualByComparingTo("15000000.00");
    assertThat(result.items().get(4).revenueRatio()).isEqualByComparingTo("54.5455");
  }

  @Test
  void shouldReturnZeroReportWhenMonthHasNoPaidBookings() {
    YearMonth reportPeriod = YearMonth.of(2026, 2);
    when(monthlyRevenueReportQueryPort.findDailyRevenueSummaries(reportPeriod))
        .thenReturn(List.of());

    GetMonthlyRevenueReportService getMonthlyRevenueReportService =
        new GetMonthlyRevenueReportService(monthlyRevenueReportQueryPort);

    var result = getMonthlyRevenueReportService.getMonthlyRevenueReport(2, 2026);

    assertThat(result.totalRevenue()).isEqualByComparingTo("0.00");
    assertThat(result.totalWeddingBookings()).isZero();
    assertThat(result.items()).hasSize(28);
    assertThat(result.items())
        .allSatisfy(item -> assertThat(item.revenueRatio()).isEqualByComparingTo("0.0000"));
  }

  @Test
  void shouldRejectInvalidMonth() {
    GetMonthlyRevenueReportService getMonthlyRevenueReportService =
        new GetMonthlyRevenueReportService(monthlyRevenueReportQueryPort);

    assertThatThrownBy(() -> getMonthlyRevenueReportService.getMonthlyRevenueReport(13, 2026))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Report month must be between 1 and 12.");

    verifyNoInteractions(monthlyRevenueReportQueryPort);
  }
}
