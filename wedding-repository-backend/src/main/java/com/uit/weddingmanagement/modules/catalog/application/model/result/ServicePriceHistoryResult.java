package com.uit.weddingmanagement.modules.catalog.application.model.result;

import com.uit.weddingmanagement.modules.catalog.domain.model.ServicePriceHistory;
import java.math.BigDecimal;
import java.time.Instant;

public record ServicePriceHistoryResult(
    Long id, Long serviceItemId, BigDecimal oldPrice, Instant effectiveFrom, Instant effectiveTo) {

  public static ServicePriceHistoryResult from(ServicePriceHistory servicePriceHistory) {
    return new ServicePriceHistoryResult(
        servicePriceHistory.id(),
        servicePriceHistory.serviceItemId(),
        servicePriceHistory.oldPrice(),
        servicePriceHistory.effectiveFrom(),
        servicePriceHistory.effectiveTo());
  }
}
