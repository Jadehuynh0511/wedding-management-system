package com.uit.weddingmanagement.modules.booking.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record InvoiceComputation(
    Instant calculatedAt,
    Instant graceDeadlineAt,
    BigDecimal hallTotalAmount,
    BigDecimal menuItemsTotalAmount,
    BigDecimal servicesTotalAmount,
    BigDecimal incidentalsTotalAmount,
    BigDecimal subtotalAmount,
    BigDecimal depositAmount,
    BigDecimal outstandingAmount,
    boolean latePaymentPenaltyEnabled,
    BigDecimal latePaymentPenaltyRate,
    long latePaymentPenaltyDays,
    BigDecimal latePaymentPenaltyAmount,
    BigDecimal finalAmount) {

  public InvoiceComputation {
    calculatedAt = requireInstant(calculatedAt, "Invoice calculation time is required.");
    graceDeadlineAt = requireInstant(graceDeadlineAt, "Invoice grace deadline is required.");
    hallTotalAmount = requireNonNegativeAmount(hallTotalAmount, "Hall total amount is required.");
    menuItemsTotalAmount =
        requireNonNegativeAmount(menuItemsTotalAmount, "Menu items total amount is required.");
    servicesTotalAmount =
        requireNonNegativeAmount(servicesTotalAmount, "Services total amount is required.");
    incidentalsTotalAmount =
        requireNonNegativeAmount(
            incidentalsTotalAmount, "Incidentals total amount is required.");
    subtotalAmount = requireNonNegativeAmount(subtotalAmount, "Subtotal amount is required.");
    depositAmount = requireNonNegativeAmount(depositAmount, "Deposit amount is required.");
    outstandingAmount =
        requireNonNegativeAmount(outstandingAmount, "Outstanding amount is required.");
    latePaymentPenaltyRate =
        requireNonNegativeAmount(
            latePaymentPenaltyRate, "Late payment penalty rate is required.");
    latePaymentPenaltyAmount =
        requireNonNegativeAmount(
            latePaymentPenaltyAmount, "Late payment penalty amount is required.");
    finalAmount = requireNonNegativeAmount(finalAmount, "Final amount is required.");
    latePaymentPenaltyDays = requireNonNegativeDays(latePaymentPenaltyDays);

    ensureSubtotalMatches(
        hallTotalAmount,
        menuItemsTotalAmount,
        servicesTotalAmount,
        incidentalsTotalAmount,
        subtotalAmount);
    ensureOutstandingMatches(subtotalAmount, depositAmount, outstandingAmount);
    ensureFinalAmountMatches(outstandingAmount, latePaymentPenaltyAmount, finalAmount);
    ensurePenaltyDaysConsistency(
        latePaymentPenaltyEnabled,
        outstandingAmount,
        latePaymentPenaltyDays,
        latePaymentPenaltyAmount);
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
      throw new IllegalArgumentException("Invoice computation amount must be non-negative.");
    }

    return value;
  }

  private static long requireNonNegativeDays(long latePaymentPenaltyDays) {
    if (latePaymentPenaltyDays < 0) {
      throw new IllegalArgumentException("Late payment penalty days must be non-negative.");
    }

    return latePaymentPenaltyDays;
  }

  private static void ensureSubtotalMatches(
      BigDecimal hallTotalAmount,
      BigDecimal menuItemsTotalAmount,
      BigDecimal servicesTotalAmount,
      BigDecimal incidentalsTotalAmount,
      BigDecimal subtotalAmount) {
    BigDecimal expectedSubtotal =
        hallTotalAmount.add(menuItemsTotalAmount).add(servicesTotalAmount).add(incidentalsTotalAmount);
    if (subtotalAmount.compareTo(expectedSubtotal) != 0) {
      throw new IllegalArgumentException("Subtotal amount must equal hall + menu + service + incidental totals.");
    }
  }

  private static void ensureOutstandingMatches(
      BigDecimal subtotalAmount, BigDecimal depositAmount, BigDecimal outstandingAmount) {
    BigDecimal expectedOutstanding = subtotalAmount.subtract(depositAmount).max(BigDecimal.ZERO);
    if (outstandingAmount.compareTo(expectedOutstanding) != 0) {
      throw new IllegalArgumentException("Outstanding amount must equal max(subtotal - deposit, 0).");
    }
  }

  private static void ensureFinalAmountMatches(
      BigDecimal outstandingAmount,
      BigDecimal latePaymentPenaltyAmount,
      BigDecimal finalAmount) {
    BigDecimal expectedFinalAmount = outstandingAmount.add(latePaymentPenaltyAmount);
    if (finalAmount.compareTo(expectedFinalAmount) != 0) {
      throw new IllegalArgumentException("Final amount must equal outstanding amount plus late payment penalty.");
    }
  }

  private static void ensurePenaltyDaysConsistency(
      boolean latePaymentPenaltyEnabled,
      BigDecimal outstandingAmount,
      long latePaymentPenaltyDays,
      BigDecimal latePaymentPenaltyAmount) {
    if ((!latePaymentPenaltyEnabled || outstandingAmount.signum() == 0)
        && (latePaymentPenaltyDays != 0 || latePaymentPenaltyAmount.signum() != 0)) {
      throw new IllegalArgumentException(
          "Late payment penalty must be zero when penalty is disabled or outstanding amount is zero.");
    }
  }
}
