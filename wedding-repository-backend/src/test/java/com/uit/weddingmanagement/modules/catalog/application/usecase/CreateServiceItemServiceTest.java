package com.uit.weddingmanagement.modules.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.common.exception.DuplicateResourceException;
import com.uit.weddingmanagement.modules.catalog.application.model.command.CreateServiceItemCommand;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItem;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItemStatus;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateServiceItemServiceTest {

  @Mock private ServiceItemQueryPort serviceItemQueryPort;

  @Mock private ServiceItemCommandPort serviceItemCommandPort;

  @Captor private ArgumentCaptor<ServiceItem> serviceItemCaptor;

  @Test
  void shouldCreateServiceItemWhenRequestIsValid() {
    when(serviceItemQueryPort.existsServiceItemByName("Trang trí sân khấu")).thenReturn(false);
    when(serviceItemCommandPort.saveServiceItem(any(ServiceItem.class)))
        .thenReturn(
            new ServiceItem(
                18L,
                "Trang trí sân khấu",
                "Trang trí",
                "gói",
                new BigDecimal("12000000.00"),
                java.time.Instant.parse("2026-05-28T08:00:00Z"),
                ServiceItemStatus.HOAT_DONG,
                "Backdrop trung tâm"));

    CreateServiceItemService createServiceItemService =
        new CreateServiceItemService(serviceItemQueryPort, serviceItemCommandPort);

    var result =
        createServiceItemService.createServiceItem(
            new CreateServiceItemCommand(
                "  Trang   trí   sân khấu  ",
                "  Trang   trí  ",
                "  gói  ",
                new BigDecimal("12000000.00"),
                null,
                "  Backdrop trung tâm  "));

    assertThat(result.id()).isEqualTo(18L);
    assertThat(result.serviceName()).isEqualTo("Trang trí sân khấu");
    assertThat(result.serviceCategory()).isEqualTo("Trang trí");
    assertThat(result.unitName()).isEqualTo("gói");
    assertThat(result.currentPrice()).isEqualByComparingTo("12000000.00");
    assertThat(result.status()).isEqualTo(ServiceItemStatus.HOAT_DONG);
    assertThat(result.active()).isTrue();

    verify(serviceItemCommandPort).saveServiceItem(serviceItemCaptor.capture());
    ServiceItem savedServiceItem = serviceItemCaptor.getValue();
    assertThat(savedServiceItem.id()).isNull();
    assertThat(savedServiceItem.serviceName()).isEqualTo("Trang trí sân khấu");
    assertThat(savedServiceItem.serviceCategory()).isEqualTo("Trang trí");
    assertThat(savedServiceItem.unitName()).isEqualTo("gói");
    assertThat(savedServiceItem.currentPrice()).isEqualByComparingTo("12000000.00");
    assertThat(savedServiceItem.status()).isEqualTo(ServiceItemStatus.HOAT_DONG);
    assertThat(savedServiceItem.priceEffectiveFrom()).isNotNull();
    assertThat(savedServiceItem.description()).isEqualTo("Backdrop trung tâm");
  }

  @Test
  void shouldRejectDuplicateServiceItemNameIgnoringCase() {
    when(serviceItemQueryPort.existsServiceItemByName("trang trí sân khấu")).thenReturn(true);

    CreateServiceItemService createServiceItemService =
        new CreateServiceItemService(serviceItemQueryPort, serviceItemCommandPort);

    assertThatThrownBy(
            () ->
                createServiceItemService.createServiceItem(
                    new CreateServiceItemCommand(
                        " trang trí sân khấu ",
                        "Trang trí",
                        "gói",
                        new BigDecimal("12000000.00"),
                        ServiceItemStatus.HOAT_DONG,
                        null)))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessage("Service item name already exists: trang trí sân khấu");

    verify(serviceItemCommandPort, never()).saveServiceItem(any(ServiceItem.class));
  }
}
