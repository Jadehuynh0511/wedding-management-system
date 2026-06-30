package com.uit.weddingmanagement.modules.catalog.application.model.command;

import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItemStatus;
import java.math.BigDecimal;

public record CreateServiceItemCommand(
    String serviceName,
    String serviceCategory,
    String unitName,
    BigDecimal currentPrice,
    ServiceItemStatus status,
    String description) {}
