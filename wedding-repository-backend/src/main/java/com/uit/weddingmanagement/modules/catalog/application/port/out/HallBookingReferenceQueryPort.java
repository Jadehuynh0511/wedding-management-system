package com.uit.weddingmanagement.modules.catalog.application.port.out;

public interface HallBookingReferenceQueryPort {

  boolean existsWeddingBookingByHallId(Long hallId);
}
