package com.uit.weddingmanagement.modules.catalog.application.model.command;

import com.uit.weddingmanagement.modules.catalog.domain.model.HallStatus;
import java.math.BigDecimal;

public record UpdateHallCommand(
    Long hallTypeId,
    String hallName,
    Integer maxCapacity,
    BigDecimal tablePrice,
    HallStatus status,
    String description) {}
