package com.uit.weddingmanagement.modules.booking.domain.model;

import java.math.BigDecimal;

public record IncidentalReceiptItem(
    Long id,
    Long serviceId,
    String serviceName,
    String unitName,
    Integer quantity,
    BigDecimal appliedUnitPrice,
    BigDecimal lineTotal,
    String notes) {

  public IncidentalReceiptItem {
    if (id != null && id <= 0) {
      throw new IllegalArgumentException("Incidental receipt item id must be greater than 0.");
    }

    serviceId =
        requirePositiveId(
            serviceId, "Service id is required.", "Service id must be greater than 0.");
    serviceName = normalizeRequiredText(serviceName, "Service name is required.");
    unitName = normalizeRequiredText(unitName, "Unit name is required.");
    quantity =
        requirePositiveInteger(
            quantity, "Service quantity is required.", "Service quantity must be greater than 0.");
    appliedUnitPrice = requirePositiveAmount(appliedUnitPrice, "Applied unit price is required.");
    lineTotal = requireLineTotal(lineTotal, quantity, appliedUnitPrice);
    notes = normalizeOptionalText(notes);
  }

  public static IncidentalReceiptItem create(
      Long serviceId,
      String serviceName,
      String unitName,
      Integer quantity,
      BigDecimal appliedUnitPrice,
      String notes) {
    return new IncidentalReceiptItem(
        null,
        serviceId,
        serviceName,
        unitName,
        quantity,
        appliedUnitPrice,
        calculateLineTotal(quantity, appliedUnitPrice),
        notes);
  }

  private static Long requirePositiveId(Long value, String nullMessage, String negativeMessage) {
    if (value == null) {
      throw new IllegalArgumentException(nullMessage);
    }

    if (value <= 0) {
      throw new IllegalArgumentException(negativeMessage);
    }

    return value;
  }

  private static String normalizeRequiredText(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(message);
    }

    return value.trim().replaceAll("\\s+", " ");
  }

  private static Integer requirePositiveInteger(
      Integer value, String nullMessage, String negativeMessage) {
    if (value == null) {
      throw new IllegalArgumentException(nullMessage);
    }

    if (value <= 0) {
      throw new IllegalArgumentException(negativeMessage);
    }

    return value;
  }

  private static BigDecimal requirePositiveAmount(BigDecimal value, String message) {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }

    if (value.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Applied unit price must be greater than 0.");
    }

    return value;
  }

  private static BigDecimal requireLineTotal(
      BigDecimal lineTotal, Integer quantity, BigDecimal appliedUnitPrice) {
    if (lineTotal == null) {
      throw new IllegalArgumentException("Line total is required.");
    }

    BigDecimal expectedLineTotal = calculateLineTotal(quantity, appliedUnitPrice);
    if (lineTotal.compareTo(expectedLineTotal) != 0) {
      throw new IllegalArgumentException(
          "Incidental receipt item line total must equal quantity multiplied by applied unit price.");
    }

    return lineTotal;
  }

  private static BigDecimal calculateLineTotal(Integer quantity, BigDecimal appliedUnitPrice) {
    return appliedUnitPrice.multiply(BigDecimal.valueOf(quantity.longValue()));
  }

  private static String normalizeOptionalText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return value.trim();
  }
}
