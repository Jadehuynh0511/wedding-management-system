package com.uit.weddingmanagement.modules.booking.presentation.dto.response;

import com.uit.weddingmanagement.modules.catalog.domain.model.HallStatus;
import java.math.BigDecimal;

public record AvailableHallResponse(
    Long id,
    Long hallTypeId,
    String hallTypeName,
    BigDecimal minimumTablePrice,
    String hallName,
    Integer maxCapacity,
    BigDecimal tablePrice,
    HallStatus status,
    String description) {}
