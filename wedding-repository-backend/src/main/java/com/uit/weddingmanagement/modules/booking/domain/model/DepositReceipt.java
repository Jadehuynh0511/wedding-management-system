package com.uit.weddingmanagement.modules.booking.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

// Phiếu cọc là record bất biến về mặt nghiệp vụ: sau khi đã tạo thì chỉ được
// đọc lại, không cho sửa/xóa để bám đúng BM10.
public record DepositReceipt(
    Long id,
    Long weddingBookingId,
    Long userId,
    Instant receivedAt,
    BigDecimal amount,
    PaymentMethod paymentMethod,
    String notes) {

  public DepositReceipt {
    if (id != null && id <= 0) {
      throw new IllegalArgumentException("Deposit receipt id must be greater than 0.");
    }

    if (weddingBookingId != null && weddingBookingId <= 0) {
      throw new IllegalArgumentException("Wedding booking id must be greater than 0.");
    }

    if (userId != null && userId <= 0) {
      throw new IllegalArgumentException("Deposit receipt user id must be greater than 0.");
    }

    receivedAt = requireReceivedAt(receivedAt);
    amount = requireAmount(amount);
    paymentMethod = requirePaymentMethod(paymentMethod);
    notes = normalizeOptionalText(notes);
  }

  public static DepositReceipt create(
      Long userId, Instant receivedAt, BigDecimal amount, PaymentMethod paymentMethod, String notes) {
    PaymentMethod normalizedPaymentMethod =
        paymentMethod == null ? PaymentMethod.TIEN_MAT : paymentMethod;

    return new DepositReceipt(null, null, userId, receivedAt, amount, normalizedPaymentMethod, notes);
  }

  private static Instant requireReceivedAt(Instant receivedAt) {
    if (receivedAt == null) {
      throw new IllegalArgumentException("Deposit receipt timestamp is required.");
    }

    return receivedAt;
  }

  private static BigDecimal requireAmount(BigDecimal amount) {
    if (amount == null) {
      throw new IllegalArgumentException("Deposit receipt amount is required.");
    }

    if (amount.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Deposit receipt amount must be greater than 0.");
    }

    return amount;
  }

  private static PaymentMethod requirePaymentMethod(PaymentMethod paymentMethod) {
    if (paymentMethod == null) {
      throw new IllegalArgumentException("Deposit receipt payment method is required.");
    }

    return paymentMethod;
  }

  private static String normalizeOptionalText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return value.trim();
  }
}
