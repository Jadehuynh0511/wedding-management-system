package com.uit.weddingmanagement.modules.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.modules.catalog.application.model.command.UpdateServiceItemCommand;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItem;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItemStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateServiceItemServiceTest {

  @Mock private ServiceItemQueryPort serviceItemQueryPort;

  @Mock private ServiceItemCommandPort serviceItemCommandPort;

  @Captor private ArgumentCaptor<ServiceItem> serviceItemCaptor;

  @Test
  void shouldUpdateMetadataWithoutChangingCurrentPrice() {
    ServiceItem existingServiceItem =
        new ServiceItem(
            25L,
            "Màn hình LED",
            "Kỹ thuật",
            "gói",
            new BigDecimal("10000000.00"),
            Instant.parse("2026-05-01T00:00:00Z"),
            ServiceItemStatus.HOAT_DONG,
            "Mô tả cũ");

    when(serviceItemQueryPort.findServiceItemById(25L)).thenReturn(Optional.of(existingServiceItem));
    when(serviceItemQueryPort.existsServiceItemByNameAndIdNot("Màn hình LED Pro", 25L))
        .thenReturn(false);
    when(serviceItemCommandPort.saveServiceItem(any(ServiceItem.class)))
        .thenAnswer(invocation -> invocation.getArgument(0, ServiceItem.class));

    UpdateServiceItemService updateServiceItemService =
        new UpdateServiceItemService(serviceItemQueryPort, serviceItemCommandPort);

    var result =
        updateServiceItemService.updateServiceItem(
            25L,
            new UpdateServiceItemCommand(
                "  Màn   hình   LED Pro  ",
                "  Kỹ   thuật  ",
                "  gói  ",
                ServiceItemStatus.NGUNG_HOAT_DONG,
                "  Mô tả mới  "));

    assertThat(result.id()).isEqualTo(25L);
    assertThat(result.serviceName()).isEqualTo("Màn hình LED Pro");
    assertThat(result.currentPrice()).isEqualByComparingTo("10000000.00");
    assertThat(result.priceEffectiveFrom()).isEqualTo(Instant.parse("2026-05-01T00:00:00Z"));
    assertThat(result.status()).isEqualTo(ServiceItemStatus.NGUNG_HOAT_DONG);
    assertThat(result.active()).isFalse();

    verify(serviceItemCommandPort).saveServiceItem(serviceItemCaptor.capture());
    ServiceItem savedServiceItem = serviceItemCaptor.getValue();
    assertThat(savedServiceItem.currentPrice()).isEqualByComparingTo("10000000.00");
    assertThat(savedServiceItem.priceEffectiveFrom())
        .isEqualTo(Instant.parse("2026-05-01T00:00:00Z"));
    assertThat(savedServiceItem.description()).isEqualTo("Mô tả mới");
  }
}
