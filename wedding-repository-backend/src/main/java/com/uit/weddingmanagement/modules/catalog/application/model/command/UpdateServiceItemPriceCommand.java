package com.uit.weddingmanagement.modules.catalog.application.model.command;

import java.math.BigDecimal;

public record UpdateServiceItemPriceCommand(BigDecimal newPrice) {}
