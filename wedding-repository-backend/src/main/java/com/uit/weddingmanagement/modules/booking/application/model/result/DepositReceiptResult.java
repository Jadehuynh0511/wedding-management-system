package com.uit.weddingmanagement.modules.booking.application.model.result;

import com.uit.weddingmanagement.modules.booking.domain.model.DepositReceipt;
import com.uit.weddingmanagement.modules.booking.domain.model.PaymentMethod;
import java.math.BigDecimal;
import java.time.Instant;

public record DepositReceiptResult(
    Long id,
    Long weddingBookingId,
    Long userId,
    Instant receivedAt,
    BigDecimal amount,
    PaymentMethod paymentMethod,
    String notes) {

  public static DepositReceiptResult from(DepositReceipt depositReceipt) {
    return new DepositReceiptResult(
        depositReceipt.id(),
        depositReceipt.weddingBookingId(),
        depositReceipt.userId(),
        depositReceipt.receivedAt(),
        depositReceipt.amount(),
        depositReceipt.paymentMethod(),
        depositReceipt.notes());
  }
}
