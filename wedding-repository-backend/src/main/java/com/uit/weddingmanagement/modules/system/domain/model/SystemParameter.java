package com.uit.weddingmanagement.modules.system.domain.model;

import java.math.BigDecimal;

// Aggregate singleton giữ toàn bộ các quy định hệ thống đang có hiệu lực tại thời điểm hiện tại.
public record SystemParameter(
    Short id,
    BigDecimal minimumDepositPercentage,
    boolean latePaymentPenaltyEnabled,
    BigDecimal latePaymentPenaltyRate,
    Integer cancellationDeadlineDays,
    BigDecimal depositRefundPercentage) {

  // Bảng system_parameters là singleton row id=1, nên domain model cũng enforce invariant này.
  public static final short SINGLETON_ID = 1;

  public SystemParameter {
    id = requireSingletonId(id);
    minimumDepositPercentage =
        requireStrictPositivePercentage(
            minimumDepositPercentage, "Minimum deposit percentage is required.");
    latePaymentPenaltyRate =
        requireStoredPenaltyRate(
            latePaymentPenaltyRate, "Late payment penalty rate is required.");
    cancellationDeadlineDays = requireCancellationDeadlineDays(cancellationDeadlineDays);
    depositRefundPercentage =
        requireNonNegativePercentage(
            depositRefundPercentage, "Deposit refund percentage is required.");
  }

  public SystemParameter updateMinimumDepositPercentage(BigDecimal newMinimumDepositPercentage) {
    BigDecimal normalizedMinimumDepositPercentage =
        requireStrictPositivePercentage(
            newMinimumDepositPercentage, "Minimum deposit percentage is required.");

    if (normalizedMinimumDepositPercentage.compareTo(minimumDepositPercentage) == 0) {
      throw new IllegalArgumentException(
          "Minimum deposit percentage must be different from current value.");
    }

    return new SystemParameter(
        id,
        normalizedMinimumDepositPercentage,
        latePaymentPenaltyEnabled,
        latePaymentPenaltyRate,
        cancellationDeadlineDays,
        depositRefundPercentage);
  }

  public SystemParameter updateLatePaymentPenalty(
      boolean latePaymentPenaltyEnabled, BigDecimal newLatePaymentPenaltyRate) {
    BigDecimal normalizedLatePaymentPenaltyRate =
        requireStrictPositivePercentage(
            newLatePaymentPenaltyRate, "Late payment penalty rate is required.");

    return new SystemParameter(
        id,
        minimumDepositPercentage,
        latePaymentPenaltyEnabled,
        normalizedLatePaymentPenaltyRate,
        cancellationDeadlineDays,
        depositRefundPercentage);
  }

  public SystemParameter updateCancellationPolicy(
      Integer newCancellationDeadlineDays, BigDecimal newDepositRefundPercentage) {
    Integer normalizedCancellationDeadlineDays =
        requireCancellationDeadlineDays(newCancellationDeadlineDays);
    BigDecimal normalizedDepositRefundPercentage =
        requireNonNegativePercentage(
            newDepositRefundPercentage, "Deposit refund percentage is required.");

    return new SystemParameter(
        id,
        minimumDepositPercentage,
        latePaymentPenaltyEnabled,
        latePaymentPenaltyRate,
        normalizedCancellationDeadlineDays,
        normalizedDepositRefundPercentage);
  }

  private static Short requireSingletonId(Short id) {
    if (id == null) {
      throw new IllegalArgumentException("System parameter id is required.");
    }

    if (id != SINGLETON_ID) {
      throw new IllegalArgumentException("System parameter id must be 1.");
    }

    return id;
  }

  private static BigDecimal requireStrictPositivePercentage(BigDecimal value, String message) {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }

    if (value.compareTo(BigDecimal.ZERO) <= 0 || value.compareTo(new BigDecimal("100")) > 0) {
      throw new IllegalArgumentException("Percentage must be greater than 0 and less than or equal to 100.");
    }

    return value;
  }

  private static BigDecimal requireNonNegativePercentage(BigDecimal value, String message) {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }

    if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(new BigDecimal("100")) > 0) {
      throw new IllegalArgumentException(
          "Percentage must be greater than or equal to 0 and less than or equal to 100.");
    }

    return value;
  }

  private static BigDecimal requireStoredPenaltyRate(BigDecimal value, String message) {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }

    if (value.compareTo(BigDecimal.ZERO) < 0 || value.compareTo(new BigDecimal("100")) > 0) {
      throw new IllegalArgumentException(
          "Late payment penalty rate must be greater than or equal to 0 and less than or equal to 100.");
    }

    return value;
  }

  private static Integer requireCancellationDeadlineDays(Integer cancellationDeadlineDays) {
    if (cancellationDeadlineDays == null) {
      throw new IllegalArgumentException("Cancellation deadline days is required.");
    }

    if (cancellationDeadlineDays <= 0) {
      throw new IllegalArgumentException(
          "Cancellation deadline days must be greater than 0.");
    }

    return cancellationDeadlineDays;
  }
}
