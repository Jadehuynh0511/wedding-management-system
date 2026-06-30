package com.uit.weddingmanagement.modules.booking.application.usecase;

import com.uit.weddingmanagement.modules.booking.application.model.result.IncidentalReceiptResult;
import com.uit.weddingmanagement.modules.booking.application.port.in.ListIncidentalReceiptsUseCase;
import com.uit.weddingmanagement.modules.booking.application.port.out.IncidentalReceiptQueryPort;
import com.uit.weddingmanagement.modules.booking.application.port.out.WeddingBookingQueryPort;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListIncidentalReceiptsService implements ListIncidentalReceiptsUseCase {

  private final WeddingBookingQueryPort weddingBookingQueryPort;
  private final IncidentalReceiptQueryPort incidentalReceiptQueryPort;

  public ListIncidentalReceiptsService(
      WeddingBookingQueryPort weddingBookingQueryPort,
      IncidentalReceiptQueryPort incidentalReceiptQueryPort) {
    this.weddingBookingQueryPort = weddingBookingQueryPort;
    this.incidentalReceiptQueryPort = incidentalReceiptQueryPort;
  }

  @Override
  public List<IncidentalReceiptResult> listIncidentalReceipts(Long bookingId) {
    requirePositiveBookingId(bookingId);

    if (weddingBookingQueryPort.findWeddingBookingById(bookingId).isEmpty()) {
      throw new EntityNotFoundException("Wedding booking not found with id: " + bookingId);
    }

    return incidentalReceiptQueryPort.findIncidentalReceiptsByWeddingBookingId(bookingId).stream()
        .map(IncidentalReceiptResult::from)
        .toList();
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
