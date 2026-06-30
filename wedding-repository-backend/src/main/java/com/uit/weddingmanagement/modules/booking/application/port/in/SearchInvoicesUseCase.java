package com.uit.weddingmanagement.modules.booking.application.port.in;

import com.uit.weddingmanagement.modules.booking.application.model.result.InvoiceSummaryResult;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchInvoicesUseCase {

  Page<InvoiceSummaryResult> searchInvoices(SearchInvoicesQuery query);

  record SearchInvoicesQuery(
      String groomName,
      String brideName,
      Long hallId,
      LocalDate celebrationDate,
      Pageable pageable) {}
}
