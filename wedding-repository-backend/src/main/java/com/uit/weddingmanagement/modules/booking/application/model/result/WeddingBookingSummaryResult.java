package com.uit.weddingmanagement.modules.booking.application.model.result;

import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingStatus;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingSummary;
import java.time.LocalDate;

public record WeddingBookingSummaryResult(
    Long id,
    Long hallId,
    String hallName,
    Long shiftId,
    String shiftName,
    String groomName,
    String brideName,
    String coupleName,
    LocalDate celebrationDate,
    Integer tableCount,
    WeddingBookingStatus status) {

  public static WeddingBookingSummaryResult from(WeddingBookingSummary weddingBookingSummary) {
    return new WeddingBookingSummaryResult(
        weddingBookingSummary.id(),
        weddingBookingSummary.hallId(),
        weddingBookingSummary.hallName(),
        weddingBookingSummary.shiftId(),
        weddingBookingSummary.shiftName(),
        weddingBookingSummary.groomName(),
        weddingBookingSummary.brideName(),
        weddingBookingSummary.coupleName(),
        weddingBookingSummary.celebrationDate(),
        weddingBookingSummary.tableCount(),
        weddingBookingSummary.status());
  }
}
