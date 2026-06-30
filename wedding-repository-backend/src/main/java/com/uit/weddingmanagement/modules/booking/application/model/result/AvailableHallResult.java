package com.uit.weddingmanagement.modules.booking.application.model.result;

import com.uit.weddingmanagement.modules.catalog.domain.model.Hall;
import com.uit.weddingmanagement.modules.catalog.domain.model.HallStatus;
import java.math.BigDecimal;

public record AvailableHallResult(
    Long id,
    Long hallTypeId,
    String hallTypeName,
    BigDecimal minimumTablePrice,
    String hallName,
    Integer maxCapacity,
    BigDecimal tablePrice,
    HallStatus status,
    String description) {

  public static AvailableHallResult from(Hall hall) {
    return new AvailableHallResult(
        hall.id(),
        hall.hallType().id(),
        hall.hallType().hallTypeName(),
        hall.hallType().minimumTablePrice(),
        hall.hallName(),
        hall.maxCapacity(),
        hall.tablePrice(),
        hall.status(),
        hall.description());
  }
}
