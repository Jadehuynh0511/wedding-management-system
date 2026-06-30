package com.uit.weddingmanagement.modules.catalog.application.model.result;

import com.uit.weddingmanagement.modules.catalog.domain.model.MenuItem;
import com.uit.weddingmanagement.modules.catalog.domain.model.MenuItemStatus;
import java.math.BigDecimal;

public record MenuItemResult(
    Long id,
    String itemName,
    String itemCategory,
    BigDecimal currentPrice,
    MenuItemStatus status,
    boolean available,
    String description) {

  public static MenuItemResult from(MenuItem menuItem) {
    return new MenuItemResult(
        menuItem.id(),
        menuItem.itemName(),
        menuItem.itemCategory(),
        menuItem.currentPrice(),
        menuItem.status(),
        menuItem.isAvailable(),
        menuItem.description());
  }
}
