package com.uit.weddingmanagement.modules.booking.application.port.out;

import com.uit.weddingmanagement.modules.booking.domain.model.Invoice;

public interface InvoiceCommandPort {

  Invoice saveInvoice(Invoice invoice);
}
