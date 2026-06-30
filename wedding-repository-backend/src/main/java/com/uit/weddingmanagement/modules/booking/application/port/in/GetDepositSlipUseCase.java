package com.uit.weddingmanagement.modules.booking.application.port.in;

import com.uit.weddingmanagement.modules.booking.application.model.result.DepositSlipResult;

public interface GetDepositSlipUseCase {

  DepositSlipResult getDepositSlip(Long bookingId);
}
