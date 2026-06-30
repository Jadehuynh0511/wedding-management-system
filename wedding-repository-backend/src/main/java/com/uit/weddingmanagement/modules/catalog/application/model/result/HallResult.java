package com.uit.weddingmanagement.modules.catalog.application.model.result;

import com.uit.weddingmanagement.modules.catalog.domain.model.Hall;
import com.uit.weddingmanagement.modules.catalog.domain.model.HallStatus;
import java.math.BigDecimal;

public record HallResult(
    Long id,
    Long hallTypeId,
    String hallTypeName,
    BigDecimal minimumTablePrice,
    String hallName,
    Integer maxCapacity,
    BigDecimal tablePrice,
    HallStatus status,
    String description) {

  public static HallResult from(Hall hall) {
    return new HallResult(
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
