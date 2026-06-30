package com.uit.weddingmanagement.modules.booking.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record CancellationComputation(
    Instant cancelledAt,
    Integer daysBeforeCelebration,
    Integer cancellationDeadlineDays,
    BigDecimal configuredDepositRefundPercentage,
    BigDecimal appliedDepositRefundPercentage,
    BigDecimal refundAmount) {

  public CancellationComputation {
    cancelledAt = requireInstant(cancelledAt, "Cancellation time is required.");
    daysBeforeCelebration =
        requireNonNegativeInteger(
            daysBeforeCelebration,
            "Days before celebration is required.",
            "Days before celebration must be greater than or equal to 0.");
    cancellationDeadlineDays =
        requirePositiveInteger(
            cancellationDeadlineDays,
            "Cancellation deadline days is required.",
            "Cancellation deadline days must be greater than 0.");
    configuredDepositRefundPercentage =
        requirePercentage(
            configuredDepositRefundPercentage,
            "Configured deposit refund percentage is required.");
    appliedDepositRefundPercentage =
        requirePercentage(
            appliedDepositRefundPercentage,
            "Applied deposit refund percentage is required.");
    refundAmount = requireNonNegativeAmount(refundAmount, "Refund amount is required.");

    ensureAppliedPercentageMatchesDeadline(
        daysBeforeCelebration,
        cancellationDeadlineDays,
        configuredDepositRefundPercentage,
        appliedDepositRefundPercentage);
  }

  private static Instant requireInstant(Instant value, String message) {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }

    return value;
  }

  private static Integer requireNonNegativeInteger(
      Integer value, String nullMessage, String negativeMessage) {
    if (value == null) {
      throw new IllegalArgumentException(nullMessage);
    }

    if (value < 0) {
      throw new IllegalArgumentException(negativeMessage);
    }

    return value;
  }

  private static Integer requirePositiveInteger(
      Integer value, String nullMessage, String negativeMessage) {
    if (value == null) {
      throw new IllegalArgumentException(nullMessage);
    }

    if (value <= 0) {
      throw new IllegalArgumentException(negativeMessage);
    }

    return value;
  }

  private static BigDecimal requirePercentage(BigDecimal value, String message) {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }

    if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(new BigDecimal("100")) > 0) {
      throw new IllegalArgumentException(
          "Cancellation refund percentage must be greater than or equal to 0 and less than or equal to 100.");
    }

    return value;
  }

  private static BigDecimal requireNonNegativeAmount(BigDecimal value, String message) {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }

    if (value.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Refund amount must be greater than or equal to 0.");
    }

    return value;
  }

  private static void ensureAppliedPercentageMatchesDeadline(
      Integer daysBeforeCelebration,
      Integer cancellationDeadlineDays,
      BigDecimal configuredDepositRefundPercentage,
      BigDecimal appliedDepositRefundPercentage) {
    BigDecimal expectedAppliedPercentage =
        daysBeforeCelebration > cancellationDeadlineDays
            ? configuredDepositRefundPercentage
            : BigDecimal.ZERO;

    if (appliedDepositRefundPercentage.compareTo(expectedAppliedPercentage) != 0) {
      throw new IllegalArgumentException(
          "Applied deposit refund percentage does not match cancellation deadline rule.");
    }
  }
}
