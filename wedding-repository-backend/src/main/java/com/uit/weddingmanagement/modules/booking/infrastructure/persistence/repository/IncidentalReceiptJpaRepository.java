package com.uit.weddingmanagement.modules.booking.infrastructure.persistence.repository;

import com.uit.weddingmanagement.modules.booking.infrastructure.persistence.entity.IncidentalReceiptJpaEntity;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IncidentalReceiptJpaRepository
    extends JpaRepository<IncidentalReceiptJpaEntity, Long> {

  @Query(
      """
      select coalesce(sum(incidentalReceipt.totalAmount), 0)
      from IncidentalReceiptJpaEntity incidentalReceipt
      where incidentalReceipt.weddingBooking.id = :bookingId
      """)
  BigDecimal sumTotalAmountByWeddingBookingId(@Param("bookingId") Long bookingId);

  @Query(
      """
      select distinct incidentalReceipt
      from IncidentalReceiptJpaEntity incidentalReceipt
      join fetch incidentalReceipt.weddingBooking weddingBooking
      left join fetch incidentalReceipt.user
      left join fetch incidentalReceipt.items item
      left join fetch item.serviceItem
      where weddingBooking.id = :bookingId
      order by incidentalReceipt.recordedAt desc, incidentalReceipt.id desc
      """)
  List<IncidentalReceiptJpaEntity> findDetailedByWeddingBookingId(@Param("bookingId") Long bookingId);
}
