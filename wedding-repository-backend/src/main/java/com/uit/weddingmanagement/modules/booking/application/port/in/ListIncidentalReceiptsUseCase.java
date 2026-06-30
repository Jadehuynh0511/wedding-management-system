package com.uit.weddingmanagement.modules.booking.application.port.in;

import com.uit.weddingmanagement.modules.booking.application.model.result.IncidentalReceiptResult;
import java.util.List;

public interface ListIncidentalReceiptsUseCase {

  List<IncidentalReceiptResult> listIncidentalReceipts(Long bookingId);
}
