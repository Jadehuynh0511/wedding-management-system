package com.uit.weddingmanagement.modules.booking.application.port.in;

import com.uit.weddingmanagement.modules.booking.application.model.command.CreateWeddingBookingCommand;
import com.uit.weddingmanagement.modules.booking.application.model.result.WeddingBookingDetailResult;

public interface CreateWeddingBookingUseCase {

  WeddingBookingDetailResult createWeddingBooking(CreateWeddingBookingCommand command);
}
