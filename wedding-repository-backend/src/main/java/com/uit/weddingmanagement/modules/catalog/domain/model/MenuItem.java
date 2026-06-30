package com.uit.weddingmanagement.modules.catalog.domain.model;

import java.math.BigDecimal;

// Domain model của món ăn tự giữ invariant cốt lõi để mọi luồng tạo/cập nhật đều dùng chung
// một bộ luật nghiệp vụ thống nhất.
public record MenuItem(
    Long id,
    String itemName,
    String itemCategory,
    BigDecimal currentPrice,
    MenuItemStatus status,
    String description) {

  public MenuItem {
    if (id != null && id <= 0) {
      throw new IllegalArgumentException("Menu item id must be greater than 0.");
    }

    itemName = normalizeItemName(itemName);
    itemCategory = normalizeItemCategory(itemCategory);
    currentPrice = requireCurrentPrice(currentPrice);
    status = requireStatus(status);
    description = normalizeDescription(description);
  }

  public static MenuItem create(
      String itemName,
      String itemCategory,
      BigDecimal currentPrice,
      MenuItemStatus status,
      String description) {
    // Khi tạo mới mà client chưa truyền trạng thái, domain tự mặc định là CON để bám theo
    // behavior của schema seed hiện tại.
    MenuItemStatus normalizedStatus = status == null ? MenuItemStatus.CON : status;

    return new MenuItem(
        null, itemName, itemCategory, currentPrice, normalizedStatus, description);
  }

  public MenuItem update(
      String itemName,
      String itemCategory,
      BigDecimal currentPrice,
      MenuItemStatus status,
      String description) {
    if (id == null) {
      throw new IllegalStateException("Cannot update a menu item without id.");
    }

    return new MenuItem(id, itemName, itemCategory, currentPrice, status, description);
  }

  public boolean isAvailable() {
    return status.isAvailable();
  }

  private static String normalizeItemName(String itemName) {
    if (itemName == null || itemName.isBlank()) {
      throw new IllegalArgumentException("Menu item name is required.");
    }

    return itemName.trim().replaceAll("\\s+", " ");
  }

  private static String normalizeItemCategory(String itemCategory) {
    if (itemCategory == null || itemCategory.isBlank()) {
      throw new IllegalArgumentException("Menu item category is required.");
    }

    return itemCategory.trim().replaceAll("\\s+", " ");
  }

  private static BigDecimal requireCurrentPrice(BigDecimal currentPrice) {
    if (currentPrice == null) {
      throw new IllegalArgumentException("Menu item current price is required.");
    }

    if (currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Menu item current price must be greater than 0.");
    }

    return currentPrice;
  }

  private static MenuItemStatus requireStatus(MenuItemStatus status) {
    if (status == null) {
      throw new IllegalArgumentException("Menu item status is required.");
    }

    return status;
  }

  private static String normalizeDescription(String description) {
    if (description == null || description.isBlank()) {
      return null;
    }

    return description.trim();
  }
}
