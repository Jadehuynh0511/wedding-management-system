package com.uit.weddingmanagement.modules.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServicePriceHistoryQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItem;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItemStatus;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServicePriceHistory;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetServiceItemServiceTest {

  @Mock private ServiceItemQueryPort serviceItemQueryPort;

  @Mock private ServicePriceHistoryQueryPort servicePriceHistoryQueryPort;

  @Test
  void shouldReturnServiceDetailWithPriceHistory() {
    ServiceItem existingServiceItem =
        new ServiceItem(
            53L,
            "Photobooth",
            "Media",
            "gói",
            new BigDecimal("6000000.00"),
            Instant.parse("2026-05-28T08:00:00Z"),
            ServiceItemStatus.HOAT_DONG,
            "Khu vực chụp ảnh lưu niệm");
    ServicePriceHistory servicePriceHistory =
        new ServicePriceHistory(
            12L,
            53L,
            new BigDecimal("5500000.00"),
            Instant.parse("2026-04-01T00:00:00Z"),
            Instant.parse("2026-05-28T08:00:00Z"));

    when(serviceItemQueryPort.findServiceItemById(53L)).thenReturn(Optional.of(existingServiceItem));
    when(servicePriceHistoryQueryPort.findServicePriceHistoriesByServiceItemId(53L))
        .thenReturn(List.of(servicePriceHistory));

    GetServiceItemService getServiceItemService =
        new GetServiceItemService(serviceItemQueryPort, servicePriceHistoryQueryPort);

    var result = getServiceItemService.getServiceItem(53L);

    assertThat(result.id()).isEqualTo(53L);
    assertThat(result.currentPrice()).isEqualByComparingTo("6000000.00");
    assertThat(result.priceHistory()).hasSize(1);
    assertThat(result.priceHistory().get(0).oldPrice()).isEqualByComparingTo("5500000.00");
  }
}
