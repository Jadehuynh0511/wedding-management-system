package com.uit.weddingmanagement.modules.booking.infrastructure.persistence.repository;

import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingStatus;
import com.uit.weddingmanagement.modules.booking.infrastructure.persistence.entity.WeddingBookingJpaEntity;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WeddingBookingJpaRepository
    extends JpaRepository<WeddingBookingJpaEntity, Long>,
        JpaSpecificationExecutor<WeddingBookingJpaEntity> {

  boolean existsByHall_Id(Long hallId);

  boolean existsByShift_Id(Long shiftId);

  boolean existsByHall_IdAndShift_IdAndCelebrationDateAndStatusNot(
      Long hallId, Long shiftId, LocalDate celebrationDate, WeddingBookingStatus status);

  // Lấy danh sách hallId đã được đặt cho shift + date, dùng để loại trừ khi search/filter
  @Query(
      """
      select distinct weddingBooking.hall.id
      from WeddingBookingJpaEntity weddingBooking
      where weddingBooking.celebrationDate = :celebrationDate
        and weddingBooking.shift.id = :shiftId
        and weddingBooking.status <> :cancelledStatus
      """)
  List<Long> findBookedHallIdsByCelebrationDateAndShiftId(
      @Param("celebrationDate") LocalDate celebrationDate,
      @Param("shiftId") Long shiftId,
      @Param("cancelledStatus") WeddingBookingStatus cancelledStatus);

  // Dùng EntityGraph để tránh N+1 khi load hall và shift cho mỗi booking trong trang tra cứu
  @Override
  @EntityGraph(attributePaths = {"hall", "shift"})
  Page<WeddingBookingJpaEntity> findAll(
      Specification<WeddingBookingJpaEntity> specification, Pageable pageable);

  // Dùng EntityGraph để load sẵn hall, shift, phiếu cọc và user của phiếu cọc khi load booking chi tiết
  @Override
  @EntityGraph(attributePaths = {"hall", "shift", "depositReceipt", "depositReceipt.user"})
  Optional<WeddingBookingJpaEntity> findById(Long id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @EntityGraph(attributePaths = {"hall", "shift", "depositReceipt", "depositReceipt.user"})
  @Query(
      """
      select weddingBooking
      from WeddingBookingJpaEntity weddingBooking
      where weddingBooking.id = :id
      """)
  Optional<WeddingBookingJpaEntity> findByIdForUpdate(@Param("id") Long id);
}
