package com.uit.weddingmanagement.modules.catalog.application.model.command;

import com.uit.weddingmanagement.modules.catalog.domain.model.MenuItemStatus;
import java.math.BigDecimal;

public record UpdateMenuItemCommand(
    String itemName,
    String itemCategory,
    BigDecimal currentPrice,
    MenuItemStatus status,
    String description) {}
