package com.uit.weddingmanagement.modules.booking.presentation.mapper;

import com.uit.weddingmanagement.modules.booking.application.model.result.AvailableHallResult;
import com.uit.weddingmanagement.modules.booking.application.model.result.CancellationReceiptResult;
import com.uit.weddingmanagement.modules.booking.application.model.result.DepositReceiptResult;
import com.uit.weddingmanagement.modules.booking.application.model.result.DepositSlipResult;
import com.uit.weddingmanagement.modules.booking.application.model.result.IncidentalReceiptItemResult;
import com.uit.weddingmanagement.modules.booking.application.model.result.IncidentalReceiptResult;
import com.uit.weddingmanagement.modules.booking.application.model.result.InvoiceDetailResult;
import com.uit.weddingmanagement.modules.booking.application.model.result.InvoicePreviewResult;
import com.uit.weddingmanagement.modules.booking.application.model.result.InvoiceResult;
import com.uit.weddingmanagement.modules.booking.application.model.result.InvoiceSummaryResult;
import com.uit.weddingmanagement.modules.booking.application.model.result.WeddingBookingDetailResult;
import com.uit.weddingmanagement.modules.booking.application.model.result.WeddingBookingMenuItemResult;
import com.uit.weddingmanagement.modules.booking.application.model.result.WeddingBookingServiceResult;
import com.uit.weddingmanagement.modules.booking.application.model.result.WeddingBookingSummaryResult;
import com.uit.weddingmanagement.modules.booking.presentation.dto.response.AvailableHallResponse;
import com.uit.weddingmanagement.modules.booking.presentation.dto.response.CancellationReceiptResponse;
import com.uit.weddingmanagement.modules.booking.presentation.dto.response.DepositReceiptResponse;
import com.uit.weddingmanagement.modules.booking.presentation.dto.response.DepositSlipResponse;
import com.uit.weddingmanagement.modules.booking.presentation.dto.response.IncidentalReceiptItemResponse;
import com.uit.weddingmanagement.modules.booking.presentation.dto.response.IncidentalReceiptResponse;
import com.uit.weddingmanagement.modules.booking.presentation.dto.response.InvoiceDetailResponse;
import com.uit.weddingmanagement.modules.booking.presentation.dto.response.InvoicePreviewResponse;
import com.uit.weddingmanagement.modules.booking.presentation.dto.response.InvoiceResponse;
import com.uit.weddingmanagement.modules.booking.presentation.dto.response.InvoiceSummaryResponse;
import com.uit.weddingmanagement.modules.booking.presentation.dto.response.WeddingBookingDetailResponse;
import com.uit.weddingmanagement.modules.booking.presentation.dto.response.WeddingBookingMenuItemResponse;
import com.uit.weddingmanagement.modules.booking.presentation.dto.response.WeddingBookingServiceResponse;
import com.uit.weddingmanagement.modules.booking.presentation.dto.response.WeddingBookingSummaryResponse;

import org.springframework.stereotype.Component;

/**
 * Mapper chuyển đổi toàn bộ booking Result objects sang presentation Response DTOs.
 * Được tách ra từ BookingController để tuân thủ SRP.
 */
@Component
public class BookingPresentationMapper {

    public WeddingBookingSummaryResponse toResponse(WeddingBookingSummaryResult result) {
        return new WeddingBookingSummaryResponse(
                result.id(),
                result.hallId(),
                result.hallName(),
                result.shiftId(),
                result.shiftName(),
                result.groomName(),
                result.brideName(),
                result.coupleName(),
                result.celebrationDate(),
                result.tableCount(),
                result.status());
    }

    public WeddingBookingDetailResponse toResponse(WeddingBookingDetailResult result) {
        return new WeddingBookingDetailResponse(
                result.id(),
                result.hallId(),
                result.hallName(),
                result.shiftId(),
                result.shiftName(),
                result.groomName(),
                result.brideName(),
                result.groomPhoneNumber(),
                result.bridePhoneNumber(),
                result.bookingDate(),
                result.celebrationDate(),
                result.tableCount(),
                result.reservedTableCount(),
                result.tablePrice(),
                result.hallTotalAmount(),
                result.status(),
                result.notes(),
                result.menuItems().stream().map(this::toResponse).toList(),
                result.services().stream().map(this::toResponse).toList(),
                toResponse(result.depositReceipt()));
    }

    public DepositSlipResponse toResponse(DepositSlipResult result) {
        return new DepositSlipResponse(
                result.weddingBookingId(),
                result.hallId(),
                result.hallName(),
                result.shiftId(),
                result.shiftName(),
                result.groomName(),
                result.brideName(),
                result.coupleName(),
                result.bookingDate(),
                result.celebrationDate(),
                result.tableCount(),
                result.reservedTableCount(),
                result.hallTotalAmount(),
                toResponse(result.depositReceipt()));
    }

    public DepositReceiptResponse toResponse(DepositReceiptResult result) {
        return new DepositReceiptResponse(
                result.id(),
                result.weddingBookingId(),
                result.userId(),
                result.receivedAt(),
                result.amount(),
                result.paymentMethod(),
                result.notes());
    }

    public IncidentalReceiptResponse toResponse(IncidentalReceiptResult result) {
        return new IncidentalReceiptResponse(
                result.id(),
                result.weddingBookingId(),
                result.userId(),
                result.recordedAt(),
                result.totalAmount(),
                result.notes(),
                result.items().stream().map(this::toResponse).toList());
    }

    public IncidentalReceiptItemResponse toResponse(IncidentalReceiptItemResult result) {
        return new IncidentalReceiptItemResponse(
                result.id(),
                result.serviceId(),
                result.serviceName(),
                result.unitName(),
                result.quantity(),
                result.appliedUnitPrice(),
                result.lineTotal(),
                result.notes());
    }

    public InvoicePreviewResponse toResponse(InvoicePreviewResult result) {
        return new InvoicePreviewResponse(
                result.weddingBookingId(),
                result.calculatedAt(),
                result.graceDeadlineAt(),
                result.hallTotalAmount(),
                result.menuItemsTotalAmount(),
                result.servicesTotalAmount(),
                result.incidentalsTotalAmount(),
                result.subtotalAmount(),
                result.depositAmount(),
                result.outstandingAmount(),
                result.latePaymentPenaltyEnabled(),
                result.latePaymentPenaltyRate(),
                result.latePaymentPenaltyDays(),
                result.latePaymentPenaltyAmount(),
                result.finalAmount());
    }

    public InvoiceResponse toResponse(InvoiceResult result) {
        return new InvoiceResponse(
                result.id(),
                result.weddingBookingId(),
                result.userId(),
                result.paidAt(),
                result.graceDeadlineAt(),
                result.hallTotalAmount(),
                result.menuItemsTotalAmount(),
                result.servicesTotalAmount(),
                result.incidentalsTotalAmount(),
                result.subtotalAmount(),
                result.depositAmount(),
                result.outstandingAmount(),
                result.latePaymentPenaltyEnabled(),
                result.latePaymentPenaltyRate(),
                result.latePaymentPenaltyDays(),
                result.latePaymentPenaltyAmount(),
                result.finalAmount(),
                result.notes());
    }

    public InvoiceSummaryResponse toResponse(InvoiceSummaryResult result) {
        return new InvoiceSummaryResponse(
                result.id(),
                result.weddingBookingId(),
                result.hallId(),
                result.hallName(),
                result.shiftId(),
                result.shiftName(),
                result.groomName(),
                result.brideName(),
                result.coupleName(),
                result.celebrationDate(),
                result.paidAt(),
                result.finalAmount());
    }

    public InvoiceDetailResponse toResponse(InvoiceDetailResult result) {
        return new InvoiceDetailResponse(
                result.id(),
                result.weddingBookingId(),
                result.userId(),
                result.hallId(),
                result.hallName(),
                result.shiftId(),
                result.shiftName(),
                result.groomName(),
                result.brideName(),
                result.coupleName(),
                result.groomPhoneNumber(),
                result.bridePhoneNumber(),
                result.bookingDate(),
                result.celebrationDate(),
                result.tableCount(),
                result.reservedTableCount(),
                result.tablePrice(),
                result.paidAt(),
                result.hallTotalAmount(),
                result.menuItemsTotalAmount(),
                result.servicesTotalAmount(),
                result.incidentalsTotalAmount(),
                result.subtotalAmount(),
                result.depositAmount(),
                result.outstandingAmount(),
                result.latePaymentPenaltyAmount(),
                result.finalAmount(),
                result.notes(),
                result.menuItems().stream().map(this::toResponse).toList(),
                result.services().stream().map(this::toResponse).toList(),
                result.incidentalReceipts().stream().map(this::toResponse).toList());
    }

    public CancellationReceiptResponse toResponse(CancellationReceiptResult result) {
        return new CancellationReceiptResponse(
                result.id(),
                result.weddingBookingId(),
                result.userId(),
                result.cancelledAt(),
                result.daysBeforeCelebration(),
                result.cancellationDeadlineDays(),
                result.configuredDepositRefundPercentage(),
                result.appliedDepositRefundPercentage(),
                result.refundAmount(),
                result.reason());
    }

    public AvailableHallResponse toResponse(AvailableHallResult result) {
        return new AvailableHallResponse(
                result.id(),
                result.hallTypeId(),
                result.hallTypeName(),
                result.minimumTablePrice(),
                result.hallName(),
                result.maxCapacity(),
                result.tablePrice(),
                result.status(),
                result.description());
    }

    public WeddingBookingMenuItemResponse toResponse(WeddingBookingMenuItemResult result) {
        return new WeddingBookingMenuItemResponse(
                result.id(),
                result.menuItemId(),
                result.menuItemName(),
                result.quantity(),
                result.priceSnapshot(),
                result.lineTotal(),
                result.notes());
    }

    public WeddingBookingServiceResponse toResponse(WeddingBookingServiceResult result) {
        return new WeddingBookingServiceResponse(
                result.id(),
                result.serviceId(),
                result.serviceName(),
                result.unitName(),
                result.quantity(),
                result.priceSnapshot(),
                result.lineTotal(),
                result.notes());
    }
}
