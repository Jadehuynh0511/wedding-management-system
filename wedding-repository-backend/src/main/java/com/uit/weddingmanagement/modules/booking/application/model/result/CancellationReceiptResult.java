package com.uit.weddingmanagement.modules.booking.application.model.result;

import com.uit.weddingmanagement.modules.booking.domain.model.CancellationComputation;
import com.uit.weddingmanagement.modules.booking.domain.model.CancellationReceipt;
import java.math.BigDecimal;
import java.time.Instant;

public record CancellationReceiptResult(
    Long id,
    Long weddingBookingId,
    Long userId,
    Instant cancelledAt,
    Integer daysBeforeCelebration,
    Integer cancellationDeadlineDays,
    BigDecimal configuredDepositRefundPercentage,
    BigDecimal appliedDepositRefundPercentage,
    BigDecimal refundAmount,
    String reason) {

  public static CancellationReceiptResult from(
      CancellationReceipt cancellationReceipt, CancellationComputation cancellationComputation) {
    return new CancellationReceiptResult(
        cancellationReceipt.id(),
        cancellationReceipt.weddingBookingId(),
        cancellationReceipt.userId(),
        cancellationReceipt.cancelledAt(),
        cancellationReceipt.daysBeforeCelebration(),
        cancellationComputation.cancellationDeadlineDays(),
        cancellationComputation.configuredDepositRefundPercentage(),
        cancellationReceipt.appliedDepositRefundPercentage(),
        cancellationReceipt.refundAmount(),
        cancellationReceipt.reason());
  }
}
