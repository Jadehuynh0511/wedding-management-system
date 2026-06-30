package com.uit.weddingmanagement.modules.report.infrastructure.persistence.repository;

import com.uit.weddingmanagement.modules.booking.infrastructure.persistence.entity.InvoiceJpaEntity;
import com.uit.weddingmanagement.modules.report.infrastructure.persistence.projection.DailyRevenueSummaryProjection;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

public interface MonthlyRevenueQueryRepository extends Repository<InvoiceJpaEntity, Long> {

  // Dùng native query để đẩy phần GROUP BY ngày xuống PostgreSQL, tránh kéo invoice lên Java rồi mới
  // gom nhóm. Cách này phù hợp hơn cho production khi dữ liệu tăng dần theo tháng.
  String DAILY_REVENUE_QUERY =
      """
      select cast(i.paid_at at time zone 'Asia/Ho_Chi_Minh' as date) as reportDate,
             cast(count(i.id) as bigint) as weddingBookingCount,
             coalesce(sum(i.final_amount), 0) as revenue
      from invoices i
      inner join wedding_bookings wb on wb.id = i.wedding_booking_id
      where i.paid_at >= :startInclusive
        and i.paid_at < :endExclusive
        and wb.status = :paidStatus
      group by cast(i.paid_at at time zone 'Asia/Ho_Chi_Minh' as date)
      order by cast(i.paid_at at time zone 'Asia/Ho_Chi_Minh' as date)
      """;

  @Query(value = DAILY_REVENUE_QUERY, nativeQuery = true)
  List<DailyRevenueSummaryProjection> summarizePaidRevenueByDate(
      @Param("startInclusive") Instant startInclusive,
      @Param("endExclusive") Instant endExclusive,
      @Param("paidStatus") String paidStatus);
}
