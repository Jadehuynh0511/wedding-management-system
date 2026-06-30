package com.uit.weddingmanagement.modules.booking.application.usecase;

import com.uit.weddingmanagement.modules.booking.application.model.result.InvoiceDetailResult;
import com.uit.weddingmanagement.modules.booking.application.port.in.GetInvoiceDetailUseCase;
import com.uit.weddingmanagement.modules.booking.application.port.out.IncidentalReceiptQueryPort;
import com.uit.weddingmanagement.modules.booking.application.port.out.InvoiceQueryPort;
import com.uit.weddingmanagement.modules.booking.application.port.out.WeddingBookingQueryPort;
import com.uit.weddingmanagement.modules.booking.domain.model.Invoice;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBooking;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetInvoiceDetailService implements GetInvoiceDetailUseCase {

  private final InvoiceQueryPort invoiceQueryPort;
  private final WeddingBookingQueryPort weddingBookingQueryPort;
  private final IncidentalReceiptQueryPort incidentalReceiptQueryPort;

  public GetInvoiceDetailService(
      InvoiceQueryPort invoiceQueryPort,
      WeddingBookingQueryPort weddingBookingQueryPort,
      IncidentalReceiptQueryPort incidentalReceiptQueryPort) {
    this.invoiceQueryPort = invoiceQueryPort;
    this.weddingBookingQueryPort = weddingBookingQueryPort;
    this.incidentalReceiptQueryPort = incidentalReceiptQueryPort;
  }

  @Override
  public InvoiceDetailResult getInvoiceDetail(Long invoiceId) {
    requirePositiveInvoiceId(invoiceId);

    Invoice invoice =
        invoiceQueryPort
            .findInvoiceById(invoiceId)
            .orElseThrow(() -> new EntityNotFoundException("Invoice not found with id: " + invoiceId));
    WeddingBooking weddingBooking =
        weddingBookingQueryPort
            .findWeddingBookingById(invoice.weddingBookingId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Wedding booking not found with id: " + invoice.weddingBookingId()));

    return InvoiceDetailResult.from(
        invoice,
        weddingBooking,
        incidentalReceiptQueryPort.findIncidentalReceiptsByWeddingBookingId(
            invoice.weddingBookingId()));
  }

  private void requirePositiveInvoiceId(Long invoiceId) {
    if (invoiceId == null) {
      throw new IllegalArgumentException("Invoice id is required.");
    }

    if (invoiceId <= 0) {
      throw new IllegalArgumentException("Invoice id must be greater than 0.");
    }
  }
}
