package com.uit.weddingmanagement.modules.booking.application.port.out;

import com.uit.weddingmanagement.modules.booking.domain.model.CancellationReceipt;

public interface CancellationReceiptCommandPort {

  CancellationReceipt saveCancellationReceipt(CancellationReceipt cancellationReceipt);
}
