package com.uit.weddingmanagement.modules.booking.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Invoice(
    Long id,
    Long weddingBookingId,
    Long userId,
    Instant paidAt,
    BigDecimal hallTotalAmount,
    BigDecimal menuItemsTotalAmount,
    BigDecimal servicesTotalAmount,
    BigDecimal incidentalsTotalAmount,
    BigDecimal depositAmount,
    BigDecimal latePaymentPenaltyAmount,
    BigDecimal finalAmount,
    String notes) {

  public Invoice {
    if (id != null && id <= 0) {
      throw new IllegalArgumentException("Invoice id must be greater than 0.");
    }

    weddingBookingId =
        requirePositiveId(
            weddingBookingId,
            "Wedding booking id is required.",
            "Wedding booking id must be greater than 0.");
    userId = requireNullablePositiveId(userId, "Invoice user id must be greater than 0.");
    paidAt = requireInstant(paidAt, "Invoice paid time is required.");
    hallTotalAmount = requireNonNegativeAmount(hallTotalAmount, "Hall total amount is required.");
    menuItemsTotalAmount =
        requireNonNegativeAmount(menuItemsTotalAmount, "Menu items total amount is required.");
    servicesTotalAmount =
        requireNonNegativeAmount(servicesTotalAmount, "Services total amount is required.");
    incidentalsTotalAmount =
        requireNonNegativeAmount(
            incidentalsTotalAmount, "Incidentals total amount is required.");
    depositAmount = requireNonNegativeAmount(depositAmount, "Deposit amount is required.");
    latePaymentPenaltyAmount =
        requireNonNegativeAmount(
            latePaymentPenaltyAmount, "Late payment penalty amount is required.");
    finalAmount = requireNonNegativeAmount(finalAmount, "Final amount is required.");
    notes = normalizeOptionalText(notes);

    ensureFinalAmountMatches(
        hallTotalAmount,
        menuItemsTotalAmount,
        servicesTotalAmount,
        incidentalsTotalAmount,
        depositAmount,
        latePaymentPenaltyAmount,
        finalAmount);
  }

  public static Invoice create(
      Long weddingBookingId,
      Long userId,
      Instant paidAt,
      String notes,
      InvoiceComputation computation) {
    InvoiceComputation normalizedComputation = requireComputation(computation);

    return new Invoice(
        null,
        weddingBookingId,
        userId,
        paidAt,
        normalizedComputation.hallTotalAmount(),
        normalizedComputation.menuItemsTotalAmount(),
        normalizedComputation.servicesTotalAmount(),
        normalizedComputation.incidentalsTotalAmount(),
        normalizedComputation.depositAmount(),
        normalizedComputation.latePaymentPenaltyAmount(),
        normalizedComputation.finalAmount(),
        notes);
  }

  public BigDecimal calculateSubtotalAmount() {
    return hallTotalAmount
        .add(menuItemsTotalAmount)
        .add(servicesTotalAmount)
        .add(incidentalsTotalAmount);
  }

  public BigDecimal calculateOutstandingAmount() {
    return calculateSubtotalAmount().subtract(depositAmount).max(BigDecimal.ZERO);
  }

  private static InvoiceComputation requireComputation(InvoiceComputation computation) {
    if (computation == null) {
      throw new IllegalArgumentException("Invoice computation is required.");
    }

    return computation;
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

  private static BigDecimal requireNonNegativeAmount(BigDecimal value, String message) {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }

    if (value.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Invoice amount must be greater than or equal to 0.");
    }

    return value;
  }

  private static String normalizeOptionalText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return value.trim();
  }

  private static void ensureFinalAmountMatches(
      BigDecimal hallTotalAmount,
      BigDecimal menuItemsTotalAmount,
      BigDecimal servicesTotalAmount,
      BigDecimal incidentalsTotalAmount,
      BigDecimal depositAmount,
      BigDecimal latePaymentPenaltyAmount,
      BigDecimal finalAmount) {
    BigDecimal subtotalAmount =
        hallTotalAmount.add(menuItemsTotalAmount).add(servicesTotalAmount).add(incidentalsTotalAmount);
    BigDecimal expectedFinalAmount =
        subtotalAmount.subtract(depositAmount).max(BigDecimal.ZERO).add(latePaymentPenaltyAmount);

    if (finalAmount.compareTo(expectedFinalAmount) != 0) {
      throw new IllegalArgumentException(
          "Invoice final amount must equal max(subtotal - deposit, 0) plus late payment penalty.");
    }
  }
}
