package com.uit.weddingmanagement.modules.catalog.application.model.result;

import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItem;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItemStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ServiceItemDetailResult(
    Long id,
    String serviceName,
    String serviceCategory,
    String unitName,
    BigDecimal currentPrice,
    Instant priceEffectiveFrom,
    ServiceItemStatus status,
    boolean active,
    String description,
    List<ServicePriceHistoryResult> priceHistory) {

  public static ServiceItemDetailResult from(
      ServiceItem serviceItem, List<ServicePriceHistoryResult> priceHistory) {
    return new ServiceItemDetailResult(
        serviceItem.id(),
        serviceItem.serviceName(),
        serviceItem.serviceCategory(),
        serviceItem.unitName(),
        serviceItem.currentPrice(),
        serviceItem.priceEffectiveFrom(),
        serviceItem.status(),
        serviceItem.isActive(),
        serviceItem.description(),
        List.copyOf(priceHistory));
  }
}
