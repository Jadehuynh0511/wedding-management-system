package com.uit.weddingmanagement.modules.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.common.exception.ResourceInUseException;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemReferenceQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItem;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItemStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteServiceItemServiceTest {

  @Mock private ServiceItemQueryPort serviceItemQueryPort;

  @Mock private ServiceItemCommandPort serviceItemCommandPort;

  @Mock private ServiceItemReferenceQueryPort serviceItemReferenceQueryPort;

  @Test
  void shouldRejectDeleteWhenServiceIsReferenced() {
    ServiceItem existingServiceItem =
        new ServiceItem(
            41L,
            "Photobooth",
            "Media",
            "gói",
            new BigDecimal("6000000.00"),
            Instant.parse("2026-05-01T00:00:00Z"),
            ServiceItemStatus.HOAT_DONG,
            null);

    when(serviceItemQueryPort.findServiceItemById(41L)).thenReturn(Optional.of(existingServiceItem));
    when(serviceItemReferenceQueryPort.existsAnyServiceReferenceByServiceItemId(41L)).thenReturn(true);

    DeleteServiceItemService deleteServiceItemService =
        new DeleteServiceItemService(
            serviceItemQueryPort, serviceItemCommandPort, serviceItemReferenceQueryPort);

    assertThatThrownBy(() -> deleteServiceItemService.deleteServiceItem(41L))
        .isInstanceOf(ResourceInUseException.class)
        .hasMessage(
            "Service item is currently used by existing wedding bookings or incidental receipts and cannot be deleted.");

    verify(serviceItemCommandPort, never()).deleteServiceItemById(41L);
  }
}
