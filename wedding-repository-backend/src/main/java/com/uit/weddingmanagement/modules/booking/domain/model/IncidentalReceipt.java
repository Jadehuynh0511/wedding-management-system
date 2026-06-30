package com.uit.weddingmanagement.modules.booking.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record IncidentalReceipt(
    Long id,
    Long weddingBookingId,
    Long userId,
    Instant recordedAt,
    BigDecimal totalAmount,
    String notes,
    List<IncidentalReceiptItem> items) {

  public IncidentalReceipt {
    if (id != null && id <= 0) {
      throw new IllegalArgumentException("Incidental receipt id must be greater than 0.");
    }

    weddingBookingId =
        requirePositiveId(
            weddingBookingId,
            "Wedding booking id is required.",
            "Wedding booking id must be greater than 0.");
    userId = normalizeOptionalPositiveId(userId, "User id must be greater than 0.");
    recordedAt = requireRecordedAt(recordedAt);
    items = requireItems(items);
    totalAmount = requireTotalAmount(totalAmount, items);
    notes = normalizeOptionalText(notes);

    ensureDistinctServices(items);
  }

  public static IncidentalReceipt create(
      Long weddingBookingId,
      Long userId,
      Instant recordedAt,
      String notes,
      List<IncidentalReceiptItem> items) {
    List<IncidentalReceiptItem> normalizedItems = requireItems(items);

    return new IncidentalReceipt(
        null,
        weddingBookingId,
        userId,
        recordedAt,
        calculateTotalAmount(normalizedItems),
        notes,
        normalizedItems);
  }

  public BigDecimal calculateTotalAmount() {
    return calculateTotalAmount(items);
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

  private static Long normalizeOptionalPositiveId(Long value, String negativeMessage) {
    if (value == null) {
      return null;
    }

    if (value <= 0) {
      throw new IllegalArgumentException(negativeMessage);
    }

    return value;
  }

  private static Instant requireRecordedAt(Instant recordedAt) {
    if (recordedAt == null) {
      throw new IllegalArgumentException("Recorded timestamp is required.");
    }

    return recordedAt;
  }

  private static List<IncidentalReceiptItem> requireItems(List<IncidentalReceiptItem> items) {
    if (items == null || items.isEmpty()) {
      throw new IllegalArgumentException("At least one incidental service item is required.");
    }

    return List.copyOf(items);
  }

  private static BigDecimal requireTotalAmount(
      BigDecimal totalAmount, List<IncidentalReceiptItem> items) {
    if (totalAmount == null) {
      throw new IllegalArgumentException("Total amount is required.");
    }

    BigDecimal expectedTotalAmount = calculateTotalAmount(items);
    if (totalAmount.compareTo(expectedTotalAmount) != 0) {
      throw new IllegalArgumentException(
          "Incidental receipt total amount must equal the sum of all item line totals.");
    }

    return totalAmount;
  }

  private static BigDecimal calculateTotalAmount(List<IncidentalReceiptItem> items) {
    return items.stream()
        .map(IncidentalReceiptItem::lineTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private static String normalizeOptionalText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return value.trim();
  }

  private static void ensureDistinctServices(List<IncidentalReceiptItem> items) {
    Set<Long> serviceIds = new LinkedHashSet<>();

    for (IncidentalReceiptItem item : items) {
      if (!serviceIds.add(item.serviceId())) {
        throw new IllegalArgumentException(
            "Duplicate service is not allowed in a single incidental receipt.");
      }
    }
  }
}
