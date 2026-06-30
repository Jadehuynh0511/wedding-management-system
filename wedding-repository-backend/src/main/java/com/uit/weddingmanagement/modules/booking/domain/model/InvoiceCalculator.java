package com.uit.weddingmanagement.modules.booking.domain.model;

import com.uit.weddingmanagement.modules.catalog.domain.model.Shift;
import com.uit.weddingmanagement.modules.system.domain.model.SystemParameter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public final class InvoiceCalculator {

  private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
  private static final Duration PAYMENT_GRACE_PERIOD = Duration.ofHours(24);
  private static final long MINUTES_PER_DAY = 24L * 60L;
  // Shift times are configured in local business time, so payment deadlines must use the same zone.
  private static final ZoneId BUSINESS_TIME_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

  private InvoiceCalculator() {}

  public static InvoiceComputation calculate(
      WeddingBooking weddingBooking,
      Shift shift,
      BigDecimal incidentalsTotalAmount,
      SystemParameter systemParameter,
      Instant calculatedAt) {
    WeddingBooking normalizedWeddingBooking = requireWeddingBooking(weddingBooking);
    Shift normalizedShift = requireMatchingShift(shift, normalizedWeddingBooking.shiftId());
    BigDecimal normalizedIncidentalsTotalAmount =
        requireNonNegativeAmount(
            incidentalsTotalAmount, "Incidentals total amount is required.");
    SystemParameter normalizedSystemParameter = requireSystemParameter(systemParameter);
    Instant normalizedCalculatedAt =
        requireInstant(calculatedAt, "Invoice calculation time is required.");

    BigDecimal hallTotalAmount = normalizedWeddingBooking.calculateHallTotalAmount();
    BigDecimal menuItemsTotalAmount = normalizedWeddingBooking.calculateMenuItemsTotalAmount();
    BigDecimal servicesTotalAmount = normalizedWeddingBooking.calculateServicesTotalAmount();
    BigDecimal subtotalAmount =
        hallTotalAmount
            .add(menuItemsTotalAmount)
            .add(servicesTotalAmount)
            .add(normalizedIncidentalsTotalAmount);
    BigDecimal depositAmount = normalizedWeddingBooking.depositReceipt().amount();
    BigDecimal outstandingAmount = subtotalAmount.subtract(depositAmount).max(BigDecimal.ZERO);
    Instant graceDeadlineAt =
        ZonedDateTime.of(
                normalizedWeddingBooking.celebrationDate(),
                normalizedShift.endTime(),
                BUSINESS_TIME_ZONE)
            .plus(PAYMENT_GRACE_PERIOD)
            .toInstant();

    long latePaymentPenaltyDays =
        calculateLatePaymentPenaltyDays(
            normalizedSystemParameter.latePaymentPenaltyEnabled(),
            graceDeadlineAt,
            normalizedCalculatedAt,
            outstandingAmount);
    BigDecimal latePaymentPenaltyAmount =
        calculateLatePaymentPenaltyAmount(
            outstandingAmount,
            normalizedSystemParameter.latePaymentPenaltyRate(),
            latePaymentPenaltyDays);
    BigDecimal finalAmount = outstandingAmount.add(latePaymentPenaltyAmount);

    return new InvoiceComputation(
        normalizedCalculatedAt,
        graceDeadlineAt,
        hallTotalAmount,
        menuItemsTotalAmount,
        servicesTotalAmount,
        normalizedIncidentalsTotalAmount,
        subtotalAmount,
        depositAmount,
        outstandingAmount,
        normalizedSystemParameter.latePaymentPenaltyEnabled(),
        normalizedSystemParameter.latePaymentPenaltyRate(),
        latePaymentPenaltyDays,
        latePaymentPenaltyAmount,
        finalAmount);
  }

  private static WeddingBooking requireWeddingBooking(WeddingBooking weddingBooking) {
    if (weddingBooking == null) {
      throw new IllegalArgumentException("Wedding booking is required.");
    }

    return weddingBooking;
  }

  private static Shift requireMatchingShift(Shift shift, Long expectedShiftId) {
    if (shift == null) {
      throw new IllegalArgumentException("Shift is required.");
    }

    if (!shift.id().equals(expectedShiftId)) {
      throw new IllegalArgumentException("Shift does not match wedding booking.");
    }

    return shift;
  }

  private static SystemParameter requireSystemParameter(SystemParameter systemParameter) {
    if (systemParameter == null) {
      throw new IllegalArgumentException("System parameter is required.");
    }

    return systemParameter;
  }

  private static Instant requireInstant(Instant value, String message) {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }

    return value;
  }

  private static BigDecimal requireNonNegativeAmount(BigDecimal value, String message) {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }

    if (value.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Incidentals total amount must be greater than or equal to 0.");
    }

    return value;
  }

  private static long calculateLatePaymentPenaltyDays(
      boolean latePaymentPenaltyEnabled,
      Instant graceDeadlineAt,
      Instant calculatedAt,
      BigDecimal outstandingAmount) {
    if (!latePaymentPenaltyEnabled
        || outstandingAmount.signum() == 0
        || !calculatedAt.isAfter(graceDeadlineAt)) {
      return 0;
    }

    long overdueMinutes = Duration.between(graceDeadlineAt, calculatedAt).toMinutes();
    return ((overdueMinutes - 1) / MINUTES_PER_DAY) + 1;
  }

  private static BigDecimal calculateLatePaymentPenaltyAmount(
      BigDecimal outstandingAmount, BigDecimal latePaymentPenaltyRate, long latePaymentPenaltyDays) {
    if (latePaymentPenaltyDays == 0) {
      return BigDecimal.ZERO;
    }

    return outstandingAmount
        .multiply(latePaymentPenaltyRate)
        .multiply(BigDecimal.valueOf(latePaymentPenaltyDays))
        .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);
  }
}
