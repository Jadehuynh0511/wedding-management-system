package com.uit.weddingmanagement.modules.booking.application.port.in;

import com.uit.weddingmanagement.modules.booking.application.model.result.WeddingBookingSummaryResult;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingStatus;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchWeddingBookingsUseCase {

  Page<WeddingBookingSummaryResult> searchWeddingBookings(SearchWeddingBookingsQuery query);

  record SearchWeddingBookingsQuery(
      String groomName,
      String brideName,
      Long hallId,
      LocalDate celebrationDate,
      WeddingBookingStatus status,
      Pageable pageable) {}
}
