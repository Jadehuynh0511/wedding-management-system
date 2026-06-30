package com.uit.weddingmanagement.modules.booking.application.port.out;

import com.uit.weddingmanagement.modules.booking.domain.model.Invoice;
import com.uit.weddingmanagement.modules.booking.domain.model.InvoiceSummary;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InvoiceQueryPort {

  Page<InvoiceSummary> searchInvoices(
      String groomName,
      String brideName,
      Long hallId,
      LocalDate celebrationDate,
      Pageable pageable);

  Optional<Invoice> findInvoiceById(Long invoiceId);
}
