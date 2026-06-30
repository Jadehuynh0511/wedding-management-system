package com.uit.weddingmanagement.modules.booking.application.port.in;

import com.uit.weddingmanagement.modules.booking.application.model.result.WeddingBookingDetailResult;

public interface GetWeddingBookingDetailUseCase {

  WeddingBookingDetailResult getWeddingBookingDetail(Long bookingId);
}
