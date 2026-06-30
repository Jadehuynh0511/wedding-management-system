package com.uit.weddingmanagement.modules.booking.application.usecase;

import com.uit.weddingmanagement.common.exception.DuplicateResourceException;
import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.auth.application.port.out.CurrentUserPort;
import com.uit.weddingmanagement.modules.auth.domain.model.AuthenticatedUser;
import com.uit.weddingmanagement.modules.booking.application.model.command.CreateInvoiceCommand;
import com.uit.weddingmanagement.modules.booking.application.model.result.InvoiceResult;
import com.uit.weddingmanagement.modules.booking.application.port.in.CreateInvoiceUseCase;
import com.uit.weddingmanagement.modules.booking.application.port.out.IncidentalReceiptQueryPort;
import com.uit.weddingmanagement.modules.booking.application.port.out.InvoiceCommandPort;
import com.uit.weddingmanagement.modules.booking.application.port.out.WeddingBookingCommandPort;
import com.uit.weddingmanagement.modules.booking.application.port.out.WeddingBookingQueryPort;
import com.uit.weddingmanagement.modules.booking.domain.model.Invoice;
import com.uit.weddingmanagement.modules.booking.domain.model.InvoiceCalculator;
import com.uit.weddingmanagement.modules.booking.domain.model.InvoiceComputation;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBooking;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingStatus;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ShiftQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.Shift;
import com.uit.weddingmanagement.modules.system.application.port.out.SystemParameterQueryPort;
import com.uit.weddingmanagement.modules.system.domain.model.SystemParameter;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
public class CreateInvoiceService implements CreateInvoiceUseCase {

  private final WeddingBookingQueryPort weddingBookingQueryPort;
  private final WeddingBookingCommandPort weddingBookingCommandPort;
  private final InvoiceCommandPort invoiceCommandPort;
  private final IncidentalReceiptQueryPort incidentalReceiptQueryPort;
  private final ShiftQueryPort shiftQueryPort;
  private final SystemParameterQueryPort systemParameterQueryPort;
  private final CurrentUserPort currentUserPort;

  public CreateInvoiceService(
      WeddingBookingQueryPort weddingBookingQueryPort,
      WeddingBookingCommandPort weddingBookingCommandPort,
      InvoiceCommandPort invoiceCommandPort,
      IncidentalReceiptQueryPort incidentalReceiptQueryPort,
      ShiftQueryPort shiftQueryPort,
      SystemParameterQueryPort systemParameterQueryPort,
      CurrentUserPort currentUserPort) {
    this.weddingBookingQueryPort = weddingBookingQueryPort;
    this.weddingBookingCommandPort = weddingBookingCommandPort;
    this.invoiceCommandPort = invoiceCommandPort;
    this.incidentalReceiptQueryPort = incidentalReceiptQueryPort;
    this.shiftQueryPort = shiftQueryPort;
    this.systemParameterQueryPort = systemParameterQueryPort;
    this.currentUserPort = currentUserPort;
  }

  @Override
  @AuditAction(
      action = "INVOICE_CREATE",
      module = "BILLING",
      targetType = "INVOICE",
      targetIdExpression = "#result.id",
      targetLabelExpression = "'Wedding booking ' + #result.weddingBookingId",
      successDescriptionExpression =
          "'Created invoice ' + #result.id + ' for wedding booking ' + #result.weddingBookingId",
      failureDescriptionExpression =
          "'Failed to create invoice for wedding booking ' + #bookingId",
      detailsExpression = "#command")
  public InvoiceResult createInvoice(Long bookingId, CreateInvoiceCommand command) {
    requirePositiveBookingId(bookingId);

    WeddingBooking weddingBooking = loadWeddingBookingForUpdate(bookingId);
    ensureBookingEligibleForPayment(weddingBooking);

    Shift shift = loadShift(weddingBooking.shiftId());
    SystemParameter systemParameter = loadSystemParameter();
    BigDecimal incidentalsTotalAmount =
        incidentalReceiptQueryPort.sumIncidentalReceiptTotalAmountByWeddingBookingId(bookingId);
    Instant paidAt = Instant.now();
    InvoiceComputation invoiceComputation =
        InvoiceCalculator.calculate(
            weddingBooking, shift, incidentalsTotalAmount, systemParameter, paidAt);
    AuthenticatedUser currentUser = currentUserPort.getCurrentUser();
    Invoice invoice =
        Invoice.create(
            bookingId,
            currentUser.id(),
            paidAt,
            command == null ? null : command.notes(),
            invoiceComputation);

    try {
      Invoice savedInvoice = invoiceCommandPort.saveInvoice(invoice);
      weddingBookingCommandPort.updateWeddingBookingStatus(
          weddingBooking.id(), weddingBooking.markAsPaid().status());
      return InvoiceResult.from(savedInvoice, invoiceComputation);
    } catch (DataIntegrityViolationException exception) {
      throw new DuplicateResourceException(
          "Invoice already exists for the selected wedding booking.");
    }
  }

  private WeddingBooking loadWeddingBookingForUpdate(Long bookingId) {
    return weddingBookingQueryPort
        .findWeddingBookingByIdForUpdate(bookingId)
        .orElseThrow(
            () -> new EntityNotFoundException("Wedding booking not found with id: " + bookingId));
  }

  private Shift loadShift(Long shiftId) {
    return shiftQueryPort
        .findShiftById(shiftId)
        .orElseThrow(() -> new EntityNotFoundException("Shift not found with id: " + shiftId));
  }

  private SystemParameter loadSystemParameter() {
    return systemParameterQueryPort
        .getSystemParameter()
        .orElseThrow(() -> new EntityNotFoundException("System parameter row was not found."));
  }

  private void ensureBookingEligibleForPayment(WeddingBooking weddingBooking) {
    if (weddingBooking.status() == WeddingBookingStatus.DA_THANH_TOAN) {
      throw new IllegalArgumentException(
          "Cannot create invoice for a fully paid wedding booking.");
    }

    if (weddingBooking.status() == WeddingBookingStatus.DA_HUY) {
      throw new IllegalArgumentException(
          "Cannot create invoice for a cancelled wedding booking.");
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
