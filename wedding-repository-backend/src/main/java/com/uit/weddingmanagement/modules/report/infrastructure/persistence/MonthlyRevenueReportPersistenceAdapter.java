package com.uit.weddingmanagement.modules.report.infrastructure.persistence;

import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingStatus;
import com.uit.weddingmanagement.modules.report.application.port.out.MonthlyRevenueReportQueryPort;
import com.uit.weddingmanagement.modules.report.domain.model.DailyRevenueSummary;
import com.uit.weddingmanagement.modules.report.infrastructure.persistence.projection.DailyRevenueSummaryProjection;
import com.uit.weddingmanagement.modules.report.infrastructure.persistence.repository.MonthlyRevenueQueryRepository;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MonthlyRevenueReportPersistenceAdapter implements MonthlyRevenueReportQueryPort {

  private static final ZoneId BUSINESS_TIME_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

  private final MonthlyRevenueQueryRepository monthlyRevenueQueryRepository;

  public MonthlyRevenueReportPersistenceAdapter(
      MonthlyRevenueQueryRepository monthlyRevenueQueryRepository) {
    this.monthlyRevenueQueryRepository = monthlyRevenueQueryRepository;
  }

  @Override
  public List<DailyRevenueSummary> findDailyRevenueSummaries(YearMonth reportPeriod) {
    if (reportPeriod == null) {
      throw new IllegalArgumentException("Report period is required.");
    }

    // Persistence adapter chỉ làm một việc: đổi yêu cầu nghiệp vụ “tháng báo cáo” thành khoảng thời
    // gian thực tế để database aggregate được bằng index/time-range.
    return monthlyRevenueQueryRepository
        .summarizePaidRevenueByDate(
            reportPeriod.atDay(1).atStartOfDay(BUSINESS_TIME_ZONE).toInstant(),
            reportPeriod.plusMonths(1).atDay(1).atStartOfDay(BUSINESS_TIME_ZONE).toInstant(),
            WeddingBookingStatus.DA_THANH_TOAN.name())
        .stream()
        .map(this::toDomain)
        .toList();
  }

  private DailyRevenueSummary toDomain(
      DailyRevenueSummaryProjection dailyRevenueSummaryProjection) {
    // Projection tách biệt với domain để query native có thể tối ưu theo DB mà không kéo theo
    // chi tiết JPA entity không cần thiết.
    return new DailyRevenueSummary(
        dailyRevenueSummaryProjection.getReportDate(),
        Math.toIntExact(dailyRevenueSummaryProjection.getWeddingBookingCount()),
        dailyRevenueSummaryProjection.getRevenue());
  }
}
