package com.uit.weddingmanagement.modules.booking.application.usecase;

import com.uit.weddingmanagement.modules.booking.application.model.result.WeddingBookingSummaryResult;
import com.uit.weddingmanagement.modules.booking.application.port.in.SearchWeddingBookingsUseCase;
import com.uit.weddingmanagement.modules.booking.application.port.out.WeddingBookingQueryPort;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SearchWeddingBookingsService implements SearchWeddingBookingsUseCase {

  private final WeddingBookingQueryPort weddingBookingQueryPort;

  public SearchWeddingBookingsService(WeddingBookingQueryPort weddingBookingQueryPort) {
    this.weddingBookingQueryPort = weddingBookingQueryPort;
  }

  // Search theo tên cô dâu/chú rể, sảnh, ngày tổ chức, trạng thái
  @Override
  public Page<WeddingBookingSummaryResult> searchWeddingBookings(SearchWeddingBookingsQuery query) {
    requireQuery(query);
    requirePositiveHallIdIfPresent(query.hallId());

    return weddingBookingQueryPort
        .searchWeddingBookings(
            normalizeSearchText(query.groomName()),
            normalizeSearchText(query.brideName()),
            query.hallId(),
            query.celebrationDate(),
            query.status(),
            query.pageable())
        .map(WeddingBookingSummaryResult::from);
  }

  private void requireQuery(SearchWeddingBookingsQuery query) {
    if (query == null) {
      throw new IllegalArgumentException("Search wedding bookings query is required.");
    }

    if (query.pageable() == null) {
      throw new IllegalArgumentException("Pageable is required.");
    }
  }

  private void requirePositiveHallIdIfPresent(Long hallId) {
    if (hallId != null && hallId <= 0) {
      throw new IllegalArgumentException("Hall id must be greater than 0.");
    }
  }

  private String normalizeSearchText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return value.trim().replaceAll("\\s+", " ");
  }
}
