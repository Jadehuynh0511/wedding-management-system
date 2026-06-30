package com.uit.weddingmanagement.modules.booking.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.modules.booking.application.port.in.SearchInvoicesUseCase;
import com.uit.weddingmanagement.modules.booking.application.port.out.InvoiceQueryPort;
import com.uit.weddingmanagement.modules.booking.domain.model.InvoiceSummary;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class SearchInvoicesServiceTest {

  @Mock private InvoiceQueryPort invoiceQueryPort;

  @Test
  void shouldSearchInvoicesWithNormalizedFilters() {
    PageRequest pageable = PageRequest.of(0, 10);
    when(invoiceQueryPort.searchInvoices(
            "Minh", null, 7L, LocalDate.of(2026, 8, 15), pageable))
        .thenReturn(
            new PageImpl<>(
                List.of(
                    new InvoiceSummary(
                        901L,
                        51L,
                        7L,
                        "Sunrise Hall",
                        2L,
                        "Evening",
                        "Minh",
                        "Lan",
                        LocalDate.of(2026, 8, 15),
                        Instant.parse("2026-08-16T15:00:00Z"),
                        new BigDecimal("60392500.00"))),
                pageable,
                1));

    SearchInvoicesService service = new SearchInvoicesService(invoiceQueryPort);

    var result =
        service.searchInvoices(
            new SearchInvoicesUseCase.SearchInvoicesQuery(
                "  Minh  ", "   ", 7L, LocalDate.of(2026, 8, 15), pageable));

    assertThat(result.getContent()).hasSize(1);
    assertThat(result.getContent().getFirst().id()).isEqualTo(901L);
    assertThat(result.getContent().getFirst().coupleName()).isEqualTo("Minh & Lan");
    assertThat(result.getContent().getFirst().finalAmount())
        .isEqualByComparingTo("60392500.00");
  }

  @Test
  void shouldRejectNonPositiveHallId() {
    SearchInvoicesService service = new SearchInvoicesService(invoiceQueryPort);

    assertThatThrownBy(
            () ->
                service.searchInvoices(
                    new SearchInvoicesUseCase.SearchInvoicesQuery(
                        null, null, 0L, null, PageRequest.of(0, 10))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Hall id must be greater than 0.");
  }
}
