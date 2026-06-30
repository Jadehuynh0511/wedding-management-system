package com.uit.weddingmanagement.modules.booking.application.usecase;

import com.uit.weddingmanagement.modules.booking.application.model.result.WeddingBookingDetailResult;
import com.uit.weddingmanagement.modules.booking.application.port.in.GetWeddingBookingDetailUseCase;
import com.uit.weddingmanagement.modules.booking.application.port.out.WeddingBookingQueryPort;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetWeddingBookingDetailService implements GetWeddingBookingDetailUseCase {

  private final WeddingBookingQueryPort weddingBookingQueryPort;

  public GetWeddingBookingDetailService(WeddingBookingQueryPort weddingBookingQueryPort) {
    this.weddingBookingQueryPort = weddingBookingQueryPort;
  }

  @Override
  public WeddingBookingDetailResult getWeddingBookingDetail(Long bookingId) {
    requirePositiveBookingId(bookingId);

    return weddingBookingQueryPort
        .findWeddingBookingById(bookingId)
        .map(WeddingBookingDetailResult::from)
        .orElseThrow(
            () -> new EntityNotFoundException("Wedding booking not found with id: " + bookingId));
  }

  private void requirePositiveBookingId(Long bookingId) {
    if (bookingId == null) {
      throw new IllegalArgumentException("Wedding booking id is required.");
    }

    if (bookingId <= 0) {
      throw new IllegalArgumentException("Wedding booking id must be greater than 0.");
    }
  }
}
