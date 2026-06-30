package com.uit.weddingmanagement.modules.booking.domain.model;

import java.math.BigDecimal;

// Snapshot món ăn phải được chốt ngay tại lúc đặt tiệc để giá về sau thay đổi
// cũng không làm sai lịch sử nghiệp vụ của booking đã tạo.
public record BookingMenuItem(
    Long id,
    Long menuItemId,
    String menuItemName,
    Integer quantity,
    BigDecimal priceSnapshot,
    BigDecimal lineTotal,
    String notes) {

  public BookingMenuItem {
    if (id != null && id <= 0) {
      throw new IllegalArgumentException("Booking menu item id must be greater than 0.");
    }

    menuItemId = requirePositiveId(menuItemId, "Menu item id is required.");
    menuItemName = normalizeRequiredText(menuItemName, "Menu item name is required.");
    quantity = requirePositiveInteger(quantity, "Menu item quantity is required.");
    priceSnapshot = requirePositiveAmount(priceSnapshot, "Menu item price snapshot is required.");
    lineTotal = requireLineTotal(quantity, priceSnapshot, lineTotal);
    notes = normalizeOptionalText(notes);
  }

  public static BookingMenuItem create(
      Long menuItemId, String menuItemName, Integer quantity, BigDecimal priceSnapshot, String notes) {
    BigDecimal normalizedPriceSnapshot =
        requirePositiveAmount(priceSnapshot, "Menu item price snapshot is required.");
    Integer normalizedQuantity =
        requirePositiveInteger(quantity, "Menu item quantity is required.");

    return new BookingMenuItem(
        null,
        menuItemId,
        menuItemName,
        normalizedQuantity,
        normalizedPriceSnapshot,
        calculateLineTotal(normalizedQuantity, normalizedPriceSnapshot),
        notes);
  }

  static BigDecimal calculateLineTotal(Integer quantity, BigDecimal priceSnapshot) {
    return priceSnapshot.multiply(BigDecimal.valueOf(quantity.longValue()));
  }

  public BigDecimal calculateBookingLineTotal(Integer tableCount) {
    if (tableCount == null) {
      throw new IllegalArgumentException("Table count is required for booking menu total.");
    }

    if (tableCount <= 0) {
      throw new IllegalArgumentException("Table count must be greater than 0 for booking menu total.");
    }

    return lineTotal.multiply(BigDecimal.valueOf(tableCount.longValue()));
  }

  private static Long requirePositiveId(Long value, String message) {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }

    if (value <= 0) {
      throw new IllegalArgumentException("Menu item id must be greater than 0.");
    }

    return value;
  }

  private static Integer requirePositiveInteger(Integer value, String message) {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }

    if (value <= 0) {
      throw new IllegalArgumentException("Menu item quantity must be greater than 0.");
    }

    return value;
  }

  private static BigDecimal requirePositiveAmount(BigDecimal value, String message) {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }

    if (value.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Menu item price snapshot must be greater than 0.");
    }

    return value;
  }

  private static BigDecimal requireLineTotal(
      Integer quantity, BigDecimal priceSnapshot, BigDecimal lineTotal) {
    if (lineTotal == null) {
      throw new IllegalArgumentException("Menu item line total is required.");
    }

    BigDecimal expectedLineTotal = calculateLineTotal(quantity, priceSnapshot);
    if (lineTotal.compareTo(expectedLineTotal) != 0) {
      throw new IllegalArgumentException("Menu item line total must match quantity x price snapshot.");
    }

    return lineTotal;
  }

  private static String normalizeRequiredText(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(message);
    }

    return value.trim().replaceAll("\\s+", " ");
  }

  private static String normalizeOptionalText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return value.trim();
  }
}
