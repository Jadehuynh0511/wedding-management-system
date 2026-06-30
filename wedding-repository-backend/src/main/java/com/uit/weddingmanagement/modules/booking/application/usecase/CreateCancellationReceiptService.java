package com.uit.weddingmanagement.modules.booking.application.usecase;

import com.uit.weddingmanagement.common.exception.DuplicateResourceException;
import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.auth.application.port.out.CurrentUserPort;
import com.uit.weddingmanagement.modules.auth.domain.model.AuthenticatedUser;
import com.uit.weddingmanagement.modules.booking.application.model.result.CancellationReceiptResult;
import com.uit.weddingmanagement.modules.booking.application.model.command.CreateCancellationReceiptCommand;
import com.uit.weddingmanagement.modules.booking.application.port.in.CreateCancellationReceiptUseCase;
import com.uit.weddingmanagement.modules.booking.application.port.out.CancellationReceiptCommandPort;
import com.uit.weddingmanagement.modules.booking.application.port.out.WeddingBookingCommandPort;
import com.uit.weddingmanagement.modules.booking.application.port.out.WeddingBookingQueryPort;
import com.uit.weddingmanagement.modules.booking.domain.model.CancellationCalculator;
import com.uit.weddingmanagement.modules.booking.domain.model.CancellationComputation;
import com.uit.weddingmanagement.modules.booking.domain.model.CancellationReceipt;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBooking;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingStatus;
import com.uit.weddingmanagement.modules.system.application.port.out.SystemParameterQueryPort;
import com.uit.weddingmanagement.modules.system.domain.model.SystemParameter;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
public class CreateCancellationReceiptService implements CreateCancellationReceiptUseCase {

  private final WeddingBookingQueryPort weddingBookingQueryPort;
  private final WeddingBookingCommandPort weddingBookingCommandPort;
  private final CancellationReceiptCommandPort cancellationReceiptCommandPort;
  private final SystemParameterQueryPort systemParameterQueryPort;
  private final CurrentUserPort currentUserPort;

  public CreateCancellationReceiptService(
      WeddingBookingQueryPort weddingBookingQueryPort,
      WeddingBookingCommandPort weddingBookingCommandPort,
      CancellationReceiptCommandPort cancellationReceiptCommandPort,
      SystemParameterQueryPort systemParameterQueryPort,
      CurrentUserPort currentUserPort) {
    this.weddingBookingQueryPort = weddingBookingQueryPort;
    this.weddingBookingCommandPort = weddingBookingCommandPort;
    this.cancellationReceiptCommandPort = cancellationReceiptCommandPort;
    this.systemParameterQueryPort = systemParameterQueryPort;
    this.currentUserPort = currentUserPort;
  }

  @Override
  @AuditAction(
      action = "CANCELLATION_RECEIPT_CREATE",
      module = "BILLING",
      targetType = "CANCELLATION_RECEIPT",
      targetIdExpression = "#result.id",
      targetLabelExpression = "'Wedding booking ' + #result.weddingBookingId",
      successDescriptionExpression =
          "'Created cancellation receipt ' + #result.id + ' for wedding booking ' + #result.weddingBookingId",
      failureDescriptionExpression =
          "'Failed to create cancellation receipt for wedding booking ' + #bookingId",
      detailsExpression = "#command")
  public CancellationReceiptResult createCancellationReceipt(
      Long bookingId, CreateCancellationReceiptCommand command) {
    requirePositiveBookingId(bookingId);

    if (command == null) {
      throw new IllegalArgumentException("Cancellation receipt payload is required.");
    }

    WeddingBooking weddingBooking = loadWeddingBookingForUpdate(bookingId);
    ensureBookingEligibleForCancellation(weddingBooking);
    SystemParameter systemParameter = loadSystemParameter();
    AuthenticatedUser currentUser = currentUserPort.getCurrentUser();
    CancellationComputation cancellationComputation =
        CancellationCalculator.calculate(weddingBooking, systemParameter, Instant.now());
    CancellationReceipt cancellationReceipt =
        CancellationReceipt.create(
            bookingId, currentUser.id(), command.reason(), cancellationComputation);

    try {
      CancellationReceipt savedCancellationReceipt =
          cancellationReceiptCommandPort.saveCancellationReceipt(cancellationReceipt);
      // Slot availability is derived from wedding_bookings excluding DA_HUY, so status change
      // alone is enough to free the hall + shift for that celebration date.
      weddingBookingCommandPort.updateWeddingBookingStatus(
          bookingId, weddingBooking.markAsCancelled().status());
      return CancellationReceiptResult.from(savedCancellationReceipt, cancellationComputation);
    } catch (DataIntegrityViolationException exception) {
      throw new DuplicateResourceException(
          "Cancellation receipt already exists for the selected wedding booking.");
    }
  }

  private WeddingBooking loadWeddingBookingForUpdate(Long bookingId) {
    return weddingBookingQueryPort
        .findWeddingBookingByIdForUpdate(bookingId)
        .orElseThrow(
            () -> new EntityNotFoundException("Wedding booking not found with id: " + bookingId));
  }

  private SystemParameter loadSystemParameter() {
    return systemParameterQueryPort
        .getSystemParameter()
        .orElseThrow(() -> new EntityNotFoundException("System parameter row was not found."));
  }

  private void ensureBookingEligibleForCancellation(WeddingBooking weddingBooking) {
    if (weddingBooking.status() == WeddingBookingStatus.DA_HUY) {
      throw new IllegalArgumentException(
          "Cannot create cancellation receipt for an already cancelled wedding booking.");
    }

    if (weddingBooking.status() == WeddingBookingStatus.DA_THANH_TOAN) {
      throw new IllegalArgumentException(
          "Cannot create cancellation receipt for a fully paid wedding booking.");
    }
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
