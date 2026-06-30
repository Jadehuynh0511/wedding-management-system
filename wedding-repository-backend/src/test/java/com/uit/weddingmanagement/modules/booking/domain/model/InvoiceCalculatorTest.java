package com.uit.weddingmanagement.modules.booking.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.uit.weddingmanagement.modules.catalog.domain.model.Shift;
import com.uit.weddingmanagement.modules.system.domain.model.SystemParameter;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;

class InvoiceCalculatorTest {

  @Test
  void shouldCalculateInvoiceWithoutLatePenaltyWithinGracePeriod() {
    InvoiceComputation invoiceComputation =
        InvoiceCalculator.calculate(
            createWeddingBooking(WeddingBookingStatus.DA_XAC_NHAN),
            createShift(),
            new BigDecimal("1500000.00"),
            createSystemParameter(true, "1.50"),
            Instant.parse("2026-08-16T14:59:59Z"));

    assertThat(invoiceComputation.graceDeadlineAt()).isEqualTo(Instant.parse("2026-08-16T15:00:00Z"));
    assertThat(invoiceComputation.hallTotalAmount()).isEqualByComparingTo("100000000.00");
    assertThat(invoiceComputation.menuItemsTotalAmount()).isEqualByComparingTo("3000000.00");
    assertThat(invoiceComputation.servicesTotalAmount()).isEqualByComparingTo("5000000.00");
    assertThat(invoiceComputation.incidentalsTotalAmount()).isEqualByComparingTo("1500000.00");
    assertThat(invoiceComputation.subtotalAmount()).isEqualByComparingTo("109500000.00");
    assertThat(invoiceComputation.outstandingAmount()).isEqualByComparingTo("59500000.00");
    assertThat(invoiceComputation.latePaymentPenaltyDays()).isZero();
    assertThat(invoiceComputation.latePaymentPenaltyAmount()).isEqualByComparingTo("0.00");
    assertThat(invoiceComputation.finalAmount()).isEqualByComparingTo("59500000.00");
  }

  @Test
  void shouldRoundUpLatePaymentPenaltyDaysAfterGracePeriod() {
    InvoiceComputation invoiceComputation =
        InvoiceCalculator.calculate(
            createWeddingBooking(WeddingBookingStatus.DA_XAC_NHAN),
            createShift(),
            new BigDecimal("1500000.00"),
            createSystemParameter(true, "1.50"),
            Instant.parse("2026-08-16T16:00:00Z"));

    assertThat(invoiceComputation.latePaymentPenaltyDays()).isEqualTo(1);
    assertThat(invoiceComputation.latePaymentPenaltyAmount()).isEqualByComparingTo("892500.00");
    assertThat(invoiceComputation.finalAmount()).isEqualByComparingTo("60392500.00");
  }

  private WeddingBooking createWeddingBooking(WeddingBookingStatus status) {
    return new WeddingBooking(
        51L,
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
        status,
        "Ban gan san khau",
        List.of(
            BookingMenuItem.create(11L, "Soup cua", 1, new BigDecimal("150000.00"), null)),
        List.of(
            BookingService.create(
                21L, "Photobooth", "goi", 2, new BigDecimal("2500000.00"), null)),
        new DepositReceipt(
            300L,
            51L,
            88L,
            Instant.parse("2026-06-01T08:00:00Z"),
            new BigDecimal("50000000.00"),
            PaymentMethod.CHUYEN_KHOAN,
            null));
  }

  private Shift createShift() {
    return new Shift(2L, "Evening", LocalTime.of(18, 0), LocalTime.of(22, 0), null);
  }

  private SystemParameter createSystemParameter(boolean penaltyEnabled, String penaltyRate) {
    return new SystemParameter(
        SystemParameter.SINGLETON_ID,
        new BigDecimal("50.00"),
        penaltyEnabled,
        new BigDecimal(penaltyRate),
        15,
        new BigDecimal("50.00"));
  }
}
