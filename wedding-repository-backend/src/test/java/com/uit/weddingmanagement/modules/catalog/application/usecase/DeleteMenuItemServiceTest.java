package com.uit.weddingmanagement.modules.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.common.exception.ResourceInUseException;
import com.uit.weddingmanagement.modules.catalog.application.port.out.MenuItemBookingReferenceQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.MenuItemCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.MenuItemQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.MenuItem;
import com.uit.weddingmanagement.modules.catalog.domain.model.MenuItemStatus;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteMenuItemServiceTest {

  @Mock private MenuItemQueryPort menuItemQueryPort;

  @Mock private MenuItemCommandPort menuItemCommandPort;

  @Mock private MenuItemBookingReferenceQueryPort menuItemBookingReferenceQueryPort;

  @Test
  void shouldDeleteMenuItemWhenNoBookingReferencesIt() {
    when(menuItemQueryPort.findMenuItemById(5L))
        .thenReturn(
            Optional.of(
                new MenuItem(
                    5L,
                    "Gỏi hải sản",
                    "Khai vị",
                    new BigDecimal("125000.00"),
                    MenuItemStatus.CON,
                    null)));
    when(menuItemBookingReferenceQueryPort.existsBookingMenuItemByMenuItemId(5L))
        .thenReturn(false);

    DeleteMenuItemService deleteMenuItemService =
        new DeleteMenuItemService(
            menuItemQueryPort, menuItemCommandPort, menuItemBookingReferenceQueryPort);

    deleteMenuItemService.deleteMenuItem(5L);

    verify(menuItemCommandPort).deleteMenuItemById(5L);
  }

  @Test
  void shouldRejectDeleteWhenMenuItemIsInUse() {
    when(menuItemQueryPort.findMenuItemById(5L))
        .thenReturn(
            Optional.of(
                new MenuItem(
                    5L,
                    "Gỏi hải sản",
                    "Khai vị",
                    new BigDecimal("125000.00"),
                    MenuItemStatus.CON,
                    null)));
    when(menuItemBookingReferenceQueryPort.existsBookingMenuItemByMenuItemId(5L))
        .thenReturn(true);

    DeleteMenuItemService deleteMenuItemService =
        new DeleteMenuItemService(
            menuItemQueryPort, menuItemCommandPort, menuItemBookingReferenceQueryPort);

    assertThatThrownBy(() -> deleteMenuItemService.deleteMenuItem(5L))
        .isInstanceOf(ResourceInUseException.class)
        .hasMessage(
            "Menu item is currently used by existing wedding bookings and cannot be deleted.");

    verify(menuItemCommandPort, never()).deleteMenuItemById(5L);
  }
}
