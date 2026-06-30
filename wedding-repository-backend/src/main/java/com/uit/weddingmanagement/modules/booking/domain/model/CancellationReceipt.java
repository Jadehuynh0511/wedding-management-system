package com.uit.weddingmanagement.modules.booking.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record CancellationReceipt(
    Long id,
    Long weddingBookingId,
    Long userId,
    Instant cancelledAt,
    Integer daysBeforeCelebration,
    BigDecimal appliedDepositRefundPercentage,
    BigDecimal refundAmount,
    String reason) {

  public CancellationReceipt {
    if (id != null && id <= 0) {
      throw new IllegalArgumentException("Cancellation receipt id must be greater than 0.");
    }

    weddingBookingId =
        requirePositiveId(
            weddingBookingId,
            "Wedding booking id is required.",
            "Wedding booking id must be greater than 0.");
    userId = requireNullablePositiveId(userId, "Cancellation receipt user id must be greater than 0.");
    cancelledAt = requireInstant(cancelledAt, "Cancellation time is required.");
    daysBeforeCelebration =
        requireNonNegativeInteger(
            daysBeforeCelebration,
            "Days before celebration is required.",
            "Days before celebration must be greater than or equal to 0.");
    appliedDepositRefundPercentage =
        requirePercentage(
            appliedDepositRefundPercentage,
            "Applied deposit refund percentage is required.");
    refundAmount = requireNonNegativeAmount(refundAmount, "Refund amount is required.");
    reason = normalizeRequiredText(reason, "Cancellation reason is required.");
  }

  public static CancellationReceipt create(
      Long weddingBookingId,
      Long userId,
      String reason,
      CancellationComputation cancellationComputation) {
    CancellationComputation normalizedCancellationComputation =
        requireComputation(cancellationComputation);

    return new CancellationReceipt(
        null,
        weddingBookingId,
        userId,
        normalizedCancellationComputation.cancelledAt(),
        normalizedCancellationComputation.daysBeforeCelebration(),
        normalizedCancellationComputation.appliedDepositRefundPercentage(),
        normalizedCancellationComputation.refundAmount(),
        reason);
  }

  private static CancellationComputation requireComputation(
      CancellationComputation cancellationComputation) {
    if (cancellationComputation == null) {
      throw new IllegalArgumentException("Cancellation computation is required.");
    }

    return cancellationComputation;
  }

  private static Long requirePositiveId(
      Long value, String nullMessage, String negativeMessage) {
    if (value == null) {
      throw new IllegalArgumentException(nullMessage);
    }

    if (value <= 0) {
      throw new IllegalArgumentException(negativeMessage);
    }

    return value;
  }

  private static Long requireNullablePositiveId(Long value, String message) {
    if (value == null) {
      return null;
    }

    if (value <= 0) {
      throw new IllegalArgumentException(message);
    }

    return value;
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

  private static BigDecimal requirePercentage(BigDecimal value, String message) {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }

    if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(new BigDecimal("100")) > 0) {
      throw new IllegalArgumentException(
          "Applied deposit refund percentage must be greater than or equal to 0 and less than or equal to 100.");
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

  private static String normalizeRequiredText(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(message);
    }

    return value.trim().replaceAll("\\s+", " ");
  }
}
