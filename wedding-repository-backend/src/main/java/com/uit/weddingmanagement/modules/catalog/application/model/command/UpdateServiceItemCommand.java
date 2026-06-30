package com.uit.weddingmanagement.modules.catalog.application.model.command;

import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItemStatus;

public record UpdateServiceItemCommand(
    String serviceName,
    String serviceCategory,
    String unitName,
    ServiceItemStatus status,
    String description) {}
