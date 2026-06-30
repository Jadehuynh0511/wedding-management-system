package com.uit.weddingmanagement.modules.booking.application.port.out;

import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBooking;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingStatus;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingSummary;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WeddingBookingQueryPort {

  boolean existsActiveBookingByHallIdAndShiftIdAndCelebrationDate(
      Long hallId, Long shiftId, LocalDate celebrationDate);

  Set<Long> findBookedHallIdsByCelebrationDateAndShiftId(LocalDate celebrationDate, Long shiftId);

  Page<WeddingBookingSummary> searchWeddingBookings(
      String groomName,
      String brideName,
      Long hallId,
      LocalDate celebrationDate,
      WeddingBookingStatus status,
      Pageable pageable);

  Optional<WeddingBooking> findWeddingBookingById(Long bookingId);

  Optional<WeddingBooking> findWeddingBookingByIdForUpdate(Long bookingId);
}
