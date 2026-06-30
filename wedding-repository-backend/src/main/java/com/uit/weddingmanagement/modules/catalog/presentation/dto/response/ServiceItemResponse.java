package com.uit.weddingmanagement.modules.catalog.presentation.dto.response;

import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItemStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record ServiceItemResponse(
    Long id,
    String serviceName,
    String serviceCategory,
    String unitName,
    BigDecimal currentPrice,
    Instant priceEffectiveFrom,
    ServiceItemStatus status,
    boolean active,
    String description) {}
