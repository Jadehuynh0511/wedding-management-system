package com.uit.weddingmanagement.modules.booking.application.port.out;

import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBooking;

import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingStatus;

public interface WeddingBookingCommandPort {

  WeddingBooking saveWeddingBooking(WeddingBooking weddingBooking);

  WeddingBooking updateWeddingBookingStatus(Long bookingId, WeddingBookingStatus status);
}
