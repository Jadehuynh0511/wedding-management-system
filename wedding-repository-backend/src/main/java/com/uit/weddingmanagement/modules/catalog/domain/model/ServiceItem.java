package com.uit.weddingmanagement.modules.catalog.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

// Domain model của dịch vụ tự giữ invariant để mọi luồng CRUD và đổi giá đều đi qua cùng một luật.
public record ServiceItem(
    Long id,
    String serviceName,
    String serviceCategory,
    String unitName,
    BigDecimal currentPrice,
    Instant priceEffectiveFrom,
    ServiceItemStatus status,
    String description) {

  public ServiceItem {
    if (id != null && id <= 0) {
      throw new IllegalArgumentException("Service item id must be greater than 0.");
    }

    serviceName = normalizeServiceName(serviceName);
    serviceCategory = normalizeServiceCategory(serviceCategory);
    unitName = normalizeUnitName(unitName);
    currentPrice = requireCurrentPrice(currentPrice);
    priceEffectiveFrom =
        requirePriceEffectiveFrom(priceEffectiveFrom, "Service item price effective from is required.");
    status = requireStatus(status);
    description = normalizeDescription(description);
  }

  public static ServiceItem create(
      String serviceName,
      String serviceCategory,
      String unitName,
      BigDecimal currentPrice,
      ServiceItemStatus status,
      Instant priceEffectiveFrom,
      String description) {
    // Khi tạo mới mà client chưa truyền trạng thái, domain mặc định là đang hoạt động để bám
    // theo behavior hiện tại của schema seed.
    ServiceItemStatus normalizedStatus =
        status == null ? ServiceItemStatus.HOAT_DONG : status;

    return new ServiceItem(
        null,
        serviceName,
        serviceCategory,
        unitName,
        currentPrice,
        priceEffectiveFrom,
        normalizedStatus,
        description);
  }

  public ServiceItem updateInfo(
      String serviceName,
      String serviceCategory,
      String unitName,
      ServiceItemStatus status,
      String description) {
    if (id == null) {
      throw new IllegalStateException("Cannot update a service item without id.");
    }

    return new ServiceItem(
        id,
        serviceName,
        serviceCategory,
        unitName,
        currentPrice,
        priceEffectiveFrom,
        status,
        description);
  }

  public ServiceItem changePrice(BigDecimal newPrice, Instant changedAt) {
    if (id == null) {
      throw new IllegalStateException("Cannot update service item price without id.");
    }

    BigDecimal normalizedNewPrice = requireCurrentPrice(newPrice);
    Instant normalizedChangedAt =
        requirePriceEffectiveFrom(changedAt, "Service item price change timestamp is required.");

    if (normalizedNewPrice.compareTo(currentPrice) == 0) {
      throw new IllegalArgumentException("New service price must be different from current price.");
    }

    return new ServiceItem(
        id,
        serviceName,
        serviceCategory,
        unitName,
        normalizedNewPrice,
        normalizedChangedAt,
        status,
        description);
  }

  public ServicePriceHistory closeCurrentPricePeriod(Instant effectiveTo) {
    if (id == null) {
      throw new IllegalStateException("Cannot create price history for a service item without id.");
    }

    Instant normalizedEffectiveTo =
        requirePriceEffectiveFrom(
            effectiveTo, "Service item price history effective to is required.");

    return new ServicePriceHistory(id, id, currentPrice, priceEffectiveFrom, normalizedEffectiveTo);
  }

  public boolean isActive() {
    return status.isActive();
  }

  private static String normalizeServiceName(String serviceName) {
    if (serviceName == null || serviceName.isBlank()) {
      throw new IllegalArgumentException("Service item name is required.");
    }

    return serviceName.trim().replaceAll("\\s+", " ");
  }

  private static String normalizeServiceCategory(String serviceCategory) {
    if (serviceCategory == null || serviceCategory.isBlank()) {
      throw new IllegalArgumentException("Service item category is required.");
    }

    return serviceCategory.trim().replaceAll("\\s+", " ");
  }

  private static String normalizeUnitName(String unitName) {
    if (unitName == null || unitName.isBlank()) {
      throw new IllegalArgumentException("Service item unit name is required.");
    }

    return unitName.trim().replaceAll("\\s+", " ");
  }

  private static BigDecimal requireCurrentPrice(BigDecimal currentPrice) {
    if (currentPrice == null) {
      throw new IllegalArgumentException("Service item current price is required.");
    }

    if (currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Service item current price must be greater than 0.");
    }

    return currentPrice;
  }

  private static Instant requirePriceEffectiveFrom(Instant priceEffectiveFrom, String message) {
    if (priceEffectiveFrom == null) {
      throw new IllegalArgumentException(message);
    }

    return priceEffectiveFrom;
  }

  private static ServiceItemStatus requireStatus(ServiceItemStatus status) {
    if (status == null) {
      throw new IllegalArgumentException("Service item status is required.");
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
