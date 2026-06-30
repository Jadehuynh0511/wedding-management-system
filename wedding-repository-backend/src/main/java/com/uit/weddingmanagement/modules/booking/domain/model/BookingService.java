package com.uit.weddingmanagement.modules.booking.domain.model;

import java.math.BigDecimal;

// Dịch vụ đi kèm cũng được snapshot theo đúng rule M3 để tách biệt với luồng
// "dịch vụ phát sinh" ở M4 vốn sẽ dùng giá hiện hành thay vì giá đã chốt.
public record BookingService(
    Long id,
    Long serviceId,
    String serviceName,
    String unitName,
    Integer quantity,
    BigDecimal priceSnapshot,
    BigDecimal lineTotal,
    String notes) {

  public BookingService {
    if (id != null && id <= 0) {
      throw new IllegalArgumentException("Booking service id must be greater than 0.");
    }

    serviceId = requirePositiveId(serviceId, "Service id is required.");
    serviceName = normalizeRequiredText(serviceName, "Service name is required.");
    unitName = normalizeRequiredText(unitName, "Service unit name is required.");
    quantity = requirePositiveInteger(quantity, "Service quantity is required.");
    priceSnapshot = requirePositiveAmount(priceSnapshot, "Service price snapshot is required.");
    lineTotal = requireLineTotal(quantity, priceSnapshot, lineTotal);
    notes = normalizeOptionalText(notes);
  }

  public static BookingService create(
      Long serviceId,
      String serviceName,
      String unitName,
      Integer quantity,
      BigDecimal priceSnapshot,
      String notes) {
    BigDecimal normalizedPriceSnapshot =
        requirePositiveAmount(priceSnapshot, "Service price snapshot is required.");
    Integer normalizedQuantity = requirePositiveInteger(quantity, "Service quantity is required.");

    return new BookingService(
        null,
        serviceId,
        serviceName,
        unitName,
        normalizedQuantity,
        normalizedPriceSnapshot,
        calculateLineTotal(normalizedQuantity, normalizedPriceSnapshot),
        notes);
  }

  static BigDecimal calculateLineTotal(Integer quantity, BigDecimal priceSnapshot) {
    return priceSnapshot.multiply(BigDecimal.valueOf(quantity.longValue()));
  }

  private static Long requirePositiveId(Long value, String message) {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }

    if (value <= 0) {
      throw new IllegalArgumentException("Service id must be greater than 0.");
    }

    return value;
  }

  private static Integer requirePositiveInteger(Integer value, String message) {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }

    if (value <= 0) {
      throw new IllegalArgumentException("Service quantity must be greater than 0.");
    }

    return value;
  }

  private static BigDecimal requirePositiveAmount(BigDecimal value, String message) {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }

    if (value.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Service price snapshot must be greater than 0.");
    }

    return value;
  }

  private static BigDecimal requireLineTotal(
      Integer quantity, BigDecimal priceSnapshot, BigDecimal lineTotal) {
    if (lineTotal == null) {
      throw new IllegalArgumentException("Service line total is required.");
    }

    BigDecimal expectedLineTotal = calculateLineTotal(quantity, priceSnapshot);
    if (lineTotal.compareTo(expectedLineTotal) != 0) {
      throw new IllegalArgumentException("Service line total must match quantity x price snapshot.");
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
