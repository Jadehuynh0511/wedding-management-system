package com.uit.weddingmanagement.modules.catalog.application.model.result;

import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItem;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItemStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record ServiceItemResult(
    Long id,
    String serviceName,
    String serviceCategory,
    String unitName,
    BigDecimal currentPrice,
    Instant priceEffectiveFrom,
    ServiceItemStatus status,
    boolean active,
    String description) {

  public static ServiceItemResult from(ServiceItem serviceItem) {
    return new ServiceItemResult(
        serviceItem.id(),
        serviceItem.serviceName(),
        serviceItem.serviceCategory(),
        serviceItem.unitName(),
        serviceItem.currentPrice(),
        serviceItem.priceEffectiveFrom(),
        serviceItem.status(),
        serviceItem.isActive(),
        serviceItem.description());
  }
}
