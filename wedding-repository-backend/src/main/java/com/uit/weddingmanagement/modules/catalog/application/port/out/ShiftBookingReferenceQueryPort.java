package com.uit.weddingmanagement.modules.catalog.application.port.out;

public interface ShiftBookingReferenceQueryPort {

  boolean existsWeddingBookingByShiftId(Long shiftId);
}
