package com.uit.weddingmanagement.modules.catalog.presentation.dto.response;

import com.uit.weddingmanagement.modules.catalog.domain.model.MenuItemStatus;
import java.math.BigDecimal;

public record MenuItemResponse(
    Long id,
    String itemName,
    String itemCategory,
    BigDecimal currentPrice,
    MenuItemStatus status,
    boolean available,
    String description) {}
