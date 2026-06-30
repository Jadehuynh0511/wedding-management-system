package com.uit.weddingmanagement.modules.catalog.application.model.result;

import com.uit.weddingmanagement.modules.catalog.domain.model.HallType;
import java.math.BigDecimal;

public record HallTypeResult(
    Long id, String hallTypeName, BigDecimal minimumTablePrice, String description) {

  public static HallTypeResult from(HallType hallType) {
    return new HallTypeResult(
        hallType.id(),
        hallType.hallTypeName(),
        hallType.minimumTablePrice(),
        hallType.description());
  }
}
