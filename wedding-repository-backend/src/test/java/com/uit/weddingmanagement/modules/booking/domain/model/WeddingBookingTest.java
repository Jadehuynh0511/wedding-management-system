package com.uit.weddingmanagement.modules.booking.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class WeddingBookingTest {

  @Test
  void shouldDefaultStatusToConfirmedWhenCreatingWeddingBooking() {
    WeddingBooking weddingBooking =
        WeddingBooking.create(
            7L,
            "Sunrise Hall",
            2L,
            "Evening",
            "Minh",
            "Lan",
            "0909000111",
            "0909000222",
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 8, 15),
            20,
            2,
            new BigDecimal("5000000.00"),
            "Ban gan san khau",
            List.of(
                BookingMenuItem.create(11L, "Soup cua", 1, new BigDecimal("150000.00"), null)),
            List.of(
                BookingService.create(
                    21L, "Trang tri hoa tuoi", "goi", 1, new BigDecimal("5000000.00"), null)),
            DepositReceipt.create(
                99L,
                Instant.parse("2026-06-01T08:00:00Z"),
                new BigDecimal("50000000.00"),
                PaymentMethod.CHUYEN_KHOAN,
                null));

    assertThat(weddingBooking.id()).isNull();
    assertThat(weddingBooking.status()).isEqualTo(WeddingBookingStatus.DA_XAC_NHAN);
    assertThat(weddingBooking.menuItems()).hasSize(1);
    assertThat(weddingBooking.services()).hasSize(1);
  }

  @Test
  void shouldCalculateHallTotalAmountFromTableCountAndTablePrice() {
    WeddingBooking weddingBooking = createWeddingBooking();

    assertThat(weddingBooking.calculateHallTotalAmount()).isEqualByComparingTo("100000000.00");
  }

  @Test
  void shouldCalculateMenuItemsTotalAmountUsingPerTableSnapshotAndConfirmedTables() {
    WeddingBooking weddingBooking = createWeddingBooking();

    assertThat(weddingBooking.calculateMenuItemsTotalAmount()).isEqualByComparingTo("3000000.00");
  }

  @Test
  void shouldRejectDuplicateMenuItemWithinSameWeddingBooking() {
    assertThatThrownBy(
            () ->
                WeddingBooking.create(
                    7L,
                    "Sunrise Hall",
                    2L,
                    "Evening",
                    "Minh",
                    "Lan",
                    null,
                    "0909000222",
                    LocalDate.of(2026, 6, 1),
                    LocalDate.of(2026, 8, 15),
                    20,
                    0,
                    new BigDecimal("5000000.00"),
                    null,
                    List.of(
                        BookingMenuItem.create(11L, "Soup cua", 1, new BigDecimal("150000.00"), null),
                        BookingMenuItem.create(11L, "Soup cua", 1, new BigDecimal("150000.00"), null)),
                    List.of(),
                    DepositReceipt.create(
                        99L,
                        Instant.parse("2026-06-01T08:00:00Z"),
                        new BigDecimal("50000000.00"),
                        PaymentMethod.TIEN_MAT,
                        null)))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Duplicate menu item is not allowed in a single wedding booking.");
  }

  private WeddingBooking createWeddingBooking() {
    return new WeddingBooking(
        44L,
        7L,
        "Sunrise Hall",
        2L,
        "Evening",
        "Minh",
        "Lan",
        "0909000111",
        "0909000222",
        LocalDate.of(2026, 6, 1),
        LocalDate.of(2026, 8, 15),
        20,
        2,
        new BigDecimal("5000000.00"),
        WeddingBookingStatus.DA_XAC_NHAN,
        null,
        List.of(BookingMenuItem.create(11L, "Soup cua", 1, new BigDecimal("150000.00"), null)),
        List.of(),
        new DepositReceipt(
            55L,
            44L,
            99L,
            Instant.parse("2026-06-01T08:00:00Z"),
            new BigDecimal("50000000.00"),
            PaymentMethod.CHUYEN_KHOAN,
            null));
  }
}
