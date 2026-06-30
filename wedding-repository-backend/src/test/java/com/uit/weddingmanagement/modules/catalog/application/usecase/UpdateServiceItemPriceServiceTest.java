package com.uit.weddingmanagement.modules.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.modules.catalog.application.model.command.UpdateServiceItemPriceCommand;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServicePriceHistoryCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServicePriceHistoryQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItem;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItemStatus;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServicePriceHistory;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateServiceItemPriceServiceTest {

  @Mock private ServiceItemQueryPort serviceItemQueryPort;

  @Mock private ServiceItemCommandPort serviceItemCommandPort;

  @Mock private ServicePriceHistoryCommandPort servicePriceHistoryCommandPort;

  @Mock private ServicePriceHistoryQueryPort servicePriceHistoryQueryPort;

  @Captor private ArgumentCaptor<ServiceItem> serviceItemCaptor;

  @Captor private ArgumentCaptor<ServicePriceHistory> servicePriceHistoryCaptor;

  @Test
  void shouldInsertPriceHistoryAndUpdateCurrentPrice() {
    ServiceItem existingServiceItem =
        new ServiceItem(
            33L,
            "Màn hình LED",
            "Kỹ thuật",
            "gói",
            new BigDecimal("10000000.00"),
            Instant.parse("2026-05-01T00:00:00Z"),
            ServiceItemStatus.HOAT_DONG,
            null);
    AtomicReference<ServicePriceHistory> savedHistoryRef = new AtomicReference<>();

    when(serviceItemQueryPort.findServiceItemById(33L)).thenReturn(Optional.of(existingServiceItem));
    when(servicePriceHistoryCommandPort.saveServicePriceHistory(any(ServicePriceHistory.class)))
        .thenAnswer(
            invocation -> {
              ServicePriceHistory servicePriceHistory =
                  invocation.getArgument(0, ServicePriceHistory.class);
              ServicePriceHistory savedServicePriceHistory =
                  new ServicePriceHistory(
                      71L,
                      servicePriceHistory.serviceItemId(),
                      servicePriceHistory.oldPrice(),
                      servicePriceHistory.effectiveFrom(),
                      servicePriceHistory.effectiveTo());
              savedHistoryRef.set(savedServicePriceHistory);
              return savedServicePriceHistory;
            });
    when(serviceItemCommandPort.saveServiceItem(any(ServiceItem.class)))
        .thenAnswer(invocation -> invocation.getArgument(0, ServiceItem.class));
    when(servicePriceHistoryQueryPort.findServicePriceHistoriesByServiceItemId(33L))
        .thenAnswer(invocation -> List.of(savedHistoryRef.get()));

    UpdateServiceItemPriceService updateServiceItemPriceService =
        new UpdateServiceItemPriceService(
            serviceItemQueryPort,
            serviceItemCommandPort,
            servicePriceHistoryCommandPort,
            servicePriceHistoryQueryPort);

    var result =
        updateServiceItemPriceService.updateServiceItemPrice(
            33L, new UpdateServiceItemPriceCommand(new BigDecimal("11500000.00")));

    assertThat(result.currentPrice()).isEqualByComparingTo("11500000.00");
    assertThat(result.priceHistory()).hasSize(1);
    assertThat(result.priceHistory().get(0).oldPrice()).isEqualByComparingTo("10000000.00");
    assertThat(result.priceHistory().get(0).effectiveFrom())
        .isEqualTo(Instant.parse("2026-05-01T00:00:00Z"));
    assertThat(result.priceHistory().get(0).effectiveTo()).isEqualTo(result.priceEffectiveFrom());

    verify(servicePriceHistoryCommandPort).saveServicePriceHistory(servicePriceHistoryCaptor.capture());
    verify(serviceItemCommandPort).saveServiceItem(serviceItemCaptor.capture());

    ServicePriceHistory savedServicePriceHistory = servicePriceHistoryCaptor.getValue();
    ServiceItem savedServiceItem = serviceItemCaptor.getValue();
    assertThat(savedServicePriceHistory.oldPrice()).isEqualByComparingTo("10000000.00");
    assertThat(savedServicePriceHistory.effectiveTo()).isEqualTo(savedServiceItem.priceEffectiveFrom());
    assertThat(savedServiceItem.currentPrice()).isEqualByComparingTo("11500000.00");
  }

  @Test
  void shouldRejectWhenNewPriceMatchesCurrentPrice() {
    ServiceItem existingServiceItem =
        new ServiceItem(
            33L,
            "Màn hình LED",
            "Kỹ thuật",
            "gói",
            new BigDecimal("10000000.00"),
            Instant.parse("2026-05-01T00:00:00Z"),
            ServiceItemStatus.HOAT_DONG,
            null);

    when(serviceItemQueryPort.findServiceItemById(33L)).thenReturn(Optional.of(existingServiceItem));

    UpdateServiceItemPriceService updateServiceItemPriceService =
        new UpdateServiceItemPriceService(
            serviceItemQueryPort,
            serviceItemCommandPort,
            servicePriceHistoryCommandPort,
            servicePriceHistoryQueryPort);

    assertThatThrownBy(
            () ->
                updateServiceItemPriceService.updateServiceItemPrice(
                    33L, new UpdateServiceItemPriceCommand(new BigDecimal("10000000.00"))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("New service price must be different from current price.");

    verify(servicePriceHistoryCommandPort, never()).saveServicePriceHistory(any(ServicePriceHistory.class));
    verify(serviceItemCommandPort, never()).saveServiceItem(any(ServiceItem.class));
  }
}
