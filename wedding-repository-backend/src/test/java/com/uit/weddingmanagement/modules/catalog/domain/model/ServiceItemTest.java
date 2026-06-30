package com.uit.weddingmanagement.modules.catalog.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class ServiceItemTest {

  @Test
  void shouldDefaultStatusAndNormalizeFieldsWhenCreatingServiceItem() {
    ServiceItem serviceItem =
        ServiceItem.create(
            "  Trang   trí   sân khấu  ",
            "  Trang   trí  ",
            "  gói  ",
            new BigDecimal("12000000.00"),
            null,
            Instant.parse("2026-05-28T08:00:00Z"),
            "  Backdrop trung tâm  ");

    assertThat(serviceItem.id()).isNull();
    assertThat(serviceItem.serviceName()).isEqualTo("Trang trí sân khấu");
    assertThat(serviceItem.serviceCategory()).isEqualTo("Trang trí");
    assertThat(serviceItem.unitName()).isEqualTo("gói");
    assertThat(serviceItem.status()).isEqualTo(ServiceItemStatus.HOAT_DONG);
    assertThat(serviceItem.isActive()).isTrue();
    assertThat(serviceItem.description()).isEqualTo("Backdrop trung tâm");
  }

  @Test
  void shouldCreatePriceHistoryAndApplyNewEffectiveFromWhenChangingPrice() {
    ServiceItem serviceItem =
        new ServiceItem(
            14L,
            "Màn hình LED",
            "Kỹ thuật",
            "gói",
            new BigDecimal("10000000.00"),
            Instant.parse("2026-05-01T00:00:00Z"),
            ServiceItemStatus.HOAT_DONG,
            null);

    Instant changedAt = Instant.parse("2026-05-28T09:30:00Z");

    ServicePriceHistory servicePriceHistory = serviceItem.closeCurrentPricePeriod(changedAt);
    ServiceItem updatedServiceItem =
        serviceItem.changePrice(new BigDecimal("11500000.00"), changedAt);

    assertThat(servicePriceHistory.oldPrice()).isEqualByComparingTo("10000000.00");
    assertThat(servicePriceHistory.effectiveFrom())
        .isEqualTo(Instant.parse("2026-05-01T00:00:00Z"));
    assertThat(servicePriceHistory.effectiveTo()).isEqualTo(changedAt);
    assertThat(updatedServiceItem.currentPrice()).isEqualByComparingTo("11500000.00");
    assertThat(updatedServiceItem.priceEffectiveFrom()).isEqualTo(changedAt);
  }

  @Test
  void shouldRejectWhenChangingPriceToTheSameValue() {
    ServiceItem serviceItem =
        new ServiceItem(
            14L,
            "Màn hình LED",
            "Kỹ thuật",
            "gói",
            new BigDecimal("10000000.00"),
            Instant.parse("2026-05-01T00:00:00Z"),
            ServiceItemStatus.HOAT_DONG,
            null);

    assertThatThrownBy(
            () ->
                serviceItem.changePrice(
                    new BigDecimal("10000000.00"), Instant.parse("2026-05-28T09:30:00Z")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("New service price must be different from current price.");
  }
}
