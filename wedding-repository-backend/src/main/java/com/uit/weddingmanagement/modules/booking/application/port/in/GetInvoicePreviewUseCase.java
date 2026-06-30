package com.uit.weddingmanagement.modules.booking.application.port.in;

import com.uit.weddingmanagement.modules.booking.application.model.result.InvoicePreviewResult;

public interface GetInvoicePreviewUseCase {

  InvoicePreviewResult getInvoicePreview(Long bookingId);
}
