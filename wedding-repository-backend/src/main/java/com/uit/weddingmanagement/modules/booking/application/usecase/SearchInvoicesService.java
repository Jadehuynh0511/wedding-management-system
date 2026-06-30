package com.uit.weddingmanagement.modules.booking.application.usecase;

import com.uit.weddingmanagement.modules.booking.application.model.result.InvoiceSummaryResult;
import com.uit.weddingmanagement.modules.booking.application.port.in.SearchInvoicesUseCase;
import com.uit.weddingmanagement.modules.booking.application.port.out.InvoiceQueryPort;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SearchInvoicesService implements SearchInvoicesUseCase {

  private final InvoiceQueryPort invoiceQueryPort;

  public SearchInvoicesService(InvoiceQueryPort invoiceQueryPort) {
    this.invoiceQueryPort = invoiceQueryPort;
  }

  @Override
  public Page<InvoiceSummaryResult> searchInvoices(SearchInvoicesQuery query) {
    requireQuery(query);
    requirePositiveHallIdIfPresent(query.hallId());

    return invoiceQueryPort
        .searchInvoices(
            normalizeSearchText(query.groomName()),
            normalizeSearchText(query.brideName()),
            query.hallId(),
            query.celebrationDate(),
            query.pageable())
        .map(InvoiceSummaryResult::from);
  }

  private void requireQuery(SearchInvoicesQuery query) {
    if (query == null) {
      throw new IllegalArgumentException("Search invoices query is required.");
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
