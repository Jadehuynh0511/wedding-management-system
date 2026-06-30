package com.uit.weddingmanagement.modules.booking.application.port.in;

import com.uit.weddingmanagement.modules.booking.application.model.result.CancellationReceiptResult;
import com.uit.weddingmanagement.modules.booking.application.model.command.CreateCancellationReceiptCommand;

public interface CreateCancellationReceiptUseCase {

  CancellationReceiptResult createCancellationReceipt(
      Long bookingId, CreateCancellationReceiptCommand command);
}
