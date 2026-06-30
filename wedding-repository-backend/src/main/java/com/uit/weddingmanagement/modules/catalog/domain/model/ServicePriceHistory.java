package com.uit.weddingmanagement.modules.catalog.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

// Lịch sử giá được tách riêng để mỗi lần đổi giá đều có thể truy vết khoảng hiệu lực của mức giá cũ.
public record ServicePriceHistory(
    Long id, Long serviceItemId, BigDecimal oldPrice, Instant effectiveFrom, Instant effectiveTo) {

  public ServicePriceHistory {
    if (id != null && id <= 0) {
      throw new IllegalArgumentException("Service price history id must be greater than 0.");
    }

    if (serviceItemId == null || serviceItemId <= 0) {
      throw new IllegalArgumentException("Service item id is required.");
    }

    oldPrice = requireOldPrice(oldPrice);
    effectiveFrom = requireTimestamp(effectiveFrom, "Service price history effective from is required.");
    effectiveTo = requireTimestamp(effectiveTo, "Service price history effective to is required.");

    if (effectiveTo.isBefore(effectiveFrom)) {
      throw new IllegalArgumentException(
          "Service price history effective to must be greater than or equal to effective from.");
    }
  }

  private static BigDecimal requireOldPrice(BigDecimal oldPrice) {
    if (oldPrice == null) {
      throw new IllegalArgumentException("Service price history old price is required.");
    }

    if (oldPrice.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Service price history old price must be greater than 0.");
    }

    return oldPrice;
  }

  private static Instant requireTimestamp(Instant timestamp, String message) {
    if (timestamp == null) {
      throw new IllegalArgumentException(message);
    }

    return timestamp;
  }
}
