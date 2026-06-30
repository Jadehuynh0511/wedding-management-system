package com.uit.weddingmanagement.modules.booking.domain.model;

import com.uit.weddingmanagement.modules.system.domain.model.SystemParameter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public final class CancellationCalculator {

  private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
  private static final ZoneId BUSINESS_TIME_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

  private CancellationCalculator() {}

  public static CancellationComputation calculate(
      WeddingBooking weddingBooking, SystemParameter systemParameter, Instant cancelledAt) {
    WeddingBooking normalizedWeddingBooking = requireWeddingBooking(weddingBooking);
    SystemParameter normalizedSystemParameter = requireSystemParameter(systemParameter);
    Instant normalizedCancelledAt = requireInstant(cancelledAt, "Cancellation time is required.");

    LocalDate cancellationDate = normalizedCancelledAt.atZone(BUSINESS_TIME_ZONE).toLocalDate();
    long rawDaysBeforeCelebration =
        ChronoUnit.DAYS.between(cancellationDate, normalizedWeddingBooking.celebrationDate());

    if (rawDaysBeforeCelebration < 0) {
      throw new IllegalArgumentException("Cannot cancel a wedding booking after the celebration date.");
    }

    int daysBeforeCelebration = Math.toIntExact(rawDaysBeforeCelebration);
    BigDecimal configuredDepositRefundPercentage =
        normalizedSystemParameter.depositRefundPercentage();
    BigDecimal appliedDepositRefundPercentage =
        daysBeforeCelebration > normalizedSystemParameter.cancellationDeadlineDays()
            ? configuredDepositRefundPercentage
            : BigDecimal.ZERO;
    BigDecimal refundAmount =
        normalizedWeddingBooking
            .depositReceipt()
            .amount()
            .multiply(appliedDepositRefundPercentage)
            .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);

    return new CancellationComputation(
        normalizedCancelledAt,
        daysBeforeCelebration,
        normalizedSystemParameter.cancellationDeadlineDays(),
        configuredDepositRefundPercentage,
        appliedDepositRefundPercentage,
        refundAmount);
  }

  private static WeddingBooking requireWeddingBooking(WeddingBooking weddingBooking) {
    if (weddingBooking == null) {
      throw new IllegalArgumentException("Wedding booking is required.");
    }

    return weddingBooking;
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
}
