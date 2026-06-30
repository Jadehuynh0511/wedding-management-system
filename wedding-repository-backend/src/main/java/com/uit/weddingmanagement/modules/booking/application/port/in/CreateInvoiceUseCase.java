package com.uit.weddingmanagement.modules.booking.application.port.in;

import com.uit.weddingmanagement.modules.booking.application.model.command.CreateInvoiceCommand;
import com.uit.weddingmanagement.modules.booking.application.model.result.InvoiceResult;

public interface CreateInvoiceUseCase {

  InvoiceResult createInvoice(Long bookingId, CreateInvoiceCommand command);
}
