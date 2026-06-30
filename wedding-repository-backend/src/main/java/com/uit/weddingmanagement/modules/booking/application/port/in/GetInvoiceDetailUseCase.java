package com.uit.weddingmanagement.modules.booking.application.port.in;

import com.uit.weddingmanagement.modules.booking.application.model.result.InvoiceDetailResult;

public interface GetInvoiceDetailUseCase {

  InvoiceDetailResult getInvoiceDetail(Long invoiceId);
}
