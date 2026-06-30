package com.uit.weddingmanagement.modules.booking.application.usecase;

import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.auth.application.port.out.CurrentUserPort;
import com.uit.weddingmanagement.modules.auth.domain.model.AuthenticatedUser;
import com.uit.weddingmanagement.modules.booking.application.model.command.CreateIncidentalReceiptCommand;
import com.uit.weddingmanagement.modules.booking.application.model.command.CreateIncidentalReceiptItemCommand;
import com.uit.weddingmanagement.modules.booking.application.model.result.IncidentalReceiptResult;
import com.uit.weddingmanagement.modules.booking.application.port.in.CreateIncidentalReceiptUseCase;
import com.uit.weddingmanagement.modules.booking.application.port.out.IncidentalReceiptCommandPort;
import com.uit.weddingmanagement.modules.booking.application.port.out.WeddingBookingQueryPort;
import com.uit.weddingmanagement.modules.booking.domain.model.IncidentalReceipt;
import com.uit.weddingmanagement.modules.booking.domain.model.IncidentalReceiptItem;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBooking;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingStatus;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItem;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(isolation = Isolation.READ_COMMITTED)
public class CreateIncidentalReceiptService implements CreateIncidentalReceiptUseCase {

  private final WeddingBookingQueryPort weddingBookingQueryPort;
  private final IncidentalReceiptCommandPort incidentalReceiptCommandPort;
  private final ServiceItemQueryPort serviceItemQueryPort;
  private final CurrentUserPort currentUserPort;

  public CreateIncidentalReceiptService(
      WeddingBookingQueryPort weddingBookingQueryPort,
      IncidentalReceiptCommandPort incidentalReceiptCommandPort,
      ServiceItemQueryPort serviceItemQueryPort,
      CurrentUserPort currentUserPort) {
    this.weddingBookingQueryPort = weddingBookingQueryPort;
    this.incidentalReceiptCommandPort = incidentalReceiptCommandPort;
    this.serviceItemQueryPort = serviceItemQueryPort;
    this.currentUserPort = currentUserPort;
  }

  @Override
  @AuditAction(
      action = "INCIDENTAL_RECEIPT_CREATE",
      module = "BILLING",
      targetType = "INCIDENTAL_RECEIPT",
      targetIdExpression = "#result.id",
      targetLabelExpression = "'Wedding booking ' + #result.weddingBookingId",
      successDescriptionExpression =
          "'Created incidental receipt ' + #result.id + ' for wedding booking ' + #result.weddingBookingId",
      failureDescriptionExpression =
          "'Failed to create incidental receipt for wedding booking ' + #bookingId",
      detailsExpression = "#command")
  public IncidentalReceiptResult createIncidentalReceipt(
      Long bookingId, CreateIncidentalReceiptCommand command) {
    requirePositiveId(
        bookingId,
        "Wedding booking id is required.",
        "Wedding booking id must be greater than 0.");

    if (command == null) {
      throw new IllegalArgumentException("Incidental receipt payload is required.");
    }

    WeddingBooking weddingBooking = loadWeddingBookingForUpdate(bookingId);
    ensureBookingEligibleForIncidentals(weddingBooking);
    List<IncidentalReceiptItem> items = buildItems(command.items());
    AuthenticatedUser currentUser = currentUserPort.getCurrentUser();

    IncidentalReceipt incidentalReceipt =
        IncidentalReceipt.create(
            weddingBooking.id(), currentUser.id(), Instant.now(), command.notes(), items);

    return IncidentalReceiptResult.from(
        incidentalReceiptCommandPort.saveIncidentalReceipt(incidentalReceipt));
  }

  private WeddingBooking loadWeddingBookingForUpdate(Long bookingId) {
    return weddingBookingQueryPort
        .findWeddingBookingByIdForUpdate(bookingId)
        .orElseThrow(
            () -> new EntityNotFoundException("Wedding booking not found with id: " + bookingId));
  }

  private void ensureBookingEligibleForIncidentals(WeddingBooking weddingBooking) {
    if (weddingBooking.status() == WeddingBookingStatus.DA_THANH_TOAN) {
      throw new IllegalArgumentException(
          "Cannot create incidental receipt for a fully paid wedding booking.");
    }

    if (weddingBooking.status() == WeddingBookingStatus.DA_HUY) {
      throw new IllegalArgumentException(
          "Cannot create incidental receipt for a cancelled wedding booking.");
    }
  }

  private List<IncidentalReceiptItem> buildItems(
      List<CreateIncidentalReceiptItemCommand> itemCommands) {
    if (itemCommands == null || itemCommands.isEmpty()) {
      throw new IllegalArgumentException("At least one incidental service item is required.");
    }

    return itemCommands.stream().map(this::toItem).toList();
  }

  private IncidentalReceiptItem toItem(CreateIncidentalReceiptItemCommand command) {
    requirePositiveId(
        command.serviceId(),
        "Service id is required.",
        "Service id must be greater than 0.");

    ServiceItem serviceItem =
        serviceItemQueryPort
            .findServiceItemById(command.serviceId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Service item not found with id: " + command.serviceId()));

    if (!serviceItem.isActive()) {
      throw new IllegalArgumentException(
          "Service item is not active for incidental receipt: " + serviceItem.serviceName());
    }

    return IncidentalReceiptItem.create(
        serviceItem.id(),
        serviceItem.serviceName(),
        serviceItem.unitName(),
        command.quantity(),
        serviceItem.currentPrice(),
        command.notes());
  }

  private void requirePositiveId(Long value, String nullMessage, String negativeMessage) {
    if (value == null) {
      throw new IllegalArgumentException(nullMessage);
    }

    if (value <= 0) {
      throw new IllegalArgumentException(negativeMessage);
    }
  }
}
