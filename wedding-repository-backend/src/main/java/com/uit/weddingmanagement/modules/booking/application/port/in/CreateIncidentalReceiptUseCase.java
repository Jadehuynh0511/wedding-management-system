package com.uit.weddingmanagement.modules.booking.application.port.in;

import com.uit.weddingmanagement.modules.booking.application.model.command.CreateIncidentalReceiptCommand;
import com.uit.weddingmanagement.modules.booking.application.model.result.IncidentalReceiptResult;

public interface CreateIncidentalReceiptUseCase {

  IncidentalReceiptResult createIncidentalReceipt(
      Long bookingId, CreateIncidentalReceiptCommand command);
}
