package com.uit.weddingmanagement.modules.booking.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class IncidentalReceiptTest {

  @Test
  void shouldCalculateTotalAmountWhenCreatingIncidentalReceipt() {
    IncidentalReceipt incidentalReceipt =
        IncidentalReceipt.create(
            51L,
            99L,
            Instant.parse("2026-06-01T10:15:30Z"),
            "  Them photobooth va banh ngot  ",
            List.of(
                IncidentalReceiptItem.create(
                    21L, "Photobooth", "goi", 1, new BigDecimal("4500000.00"), null),
                IncidentalReceiptItem.create(
                    22L, "Banh ngot mini", "set", 3, new BigDecimal("350000.00"), "Ban VIP")));

    assertThat(incidentalReceipt.id()).isNull();
    assertThat(incidentalReceipt.weddingBookingId()).isEqualTo(51L);
    assertThat(incidentalReceipt.totalAmount()).isEqualByComparingTo("5550000.00");
    assertThat(incidentalReceipt.calculateTotalAmount()).isEqualByComparingTo("5550000.00");
    assertThat(incidentalReceipt.notes()).isEqualTo("Them photobooth va banh ngot");
    assertThat(incidentalReceipt.items()).hasSize(2);
  }

  @Test
  void shouldRejectDuplicateServiceWithinSameIncidentalReceipt() {
    assertThatThrownBy(
            () ->
                IncidentalReceipt.create(
                    51L,
                    99L,
                    Instant.parse("2026-06-01T10:15:30Z"),
                    null,
                    List.of(
                        IncidentalReceiptItem.create(
                            21L, "Photobooth", "goi", 1, new BigDecimal("4500000.00"), null),
                        IncidentalReceiptItem.create(
                            21L,
                            "Photobooth",
                            "goi",
                            2,
                            new BigDecimal("4500000.00"),
                            "Tang them camera"))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Duplicate service is not allowed in a single incidental receipt.");
  }
}
