package com.uit.weddingmanagement.modules.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.common.exception.DuplicateResourceException;
import com.uit.weddingmanagement.modules.catalog.application.model.command.UpdateMenuItemCommand;
import com.uit.weddingmanagement.modules.catalog.application.port.out.MenuItemCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.MenuItemQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.MenuItem;
import com.uit.weddingmanagement.modules.catalog.domain.model.MenuItemStatus;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateMenuItemServiceTest {

  @Mock private MenuItemQueryPort menuItemQueryPort;

  @Mock private MenuItemCommandPort menuItemCommandPort;

  @Captor private ArgumentCaptor<MenuItem> menuItemCaptor;

  @Test
  void shouldUpdateMenuItemWhenRequestIsValid() {
    MenuItem currentMenuItem =
        new MenuItem(
            5L,
            "Gỏi hải sản",
            "Khai vị",
            new BigDecimal("125000.00"),
            MenuItemStatus.CON,
            "Món mở đầu");

    when(menuItemQueryPort.findMenuItemById(5L)).thenReturn(Optional.of(currentMenuItem));
    when(menuItemQueryPort.existsMenuItemByNameAndIdNot("Gỏi cá hồi", 5L)).thenReturn(false);
    when(menuItemCommandPort.saveMenuItem(any(MenuItem.class)))
        .thenReturn(
            new MenuItem(
                5L,
                "Gỏi cá hồi",
                "Khai vị",
                new BigDecimal("145000.00"),
                MenuItemStatus.HET,
                "Phiên bản mới"));

    UpdateMenuItemService updateMenuItemService =
        new UpdateMenuItemService(menuItemQueryPort, menuItemCommandPort);

    var result =
        updateMenuItemService.updateMenuItem(
            5L,
            new UpdateMenuItemCommand(
                "  Gỏi   cá hồi  ",
                "  Khai   vị  ",
                new BigDecimal("145000.00"),
                MenuItemStatus.HET,
                "  Phiên bản mới  "));

    assertThat(result.id()).isEqualTo(5L);
    assertThat(result.itemName()).isEqualTo("Gỏi cá hồi");
    assertThat(result.itemCategory()).isEqualTo("Khai vị");
    assertThat(result.currentPrice()).isEqualByComparingTo("145000.00");
    assertThat(result.status()).isEqualTo(MenuItemStatus.HET);
    assertThat(result.available()).isFalse();

    verify(menuItemCommandPort).saveMenuItem(menuItemCaptor.capture());
    MenuItem savedMenuItem = menuItemCaptor.getValue();
    assertThat(savedMenuItem.id()).isEqualTo(5L);
    assertThat(savedMenuItem.itemName()).isEqualTo("Gỏi cá hồi");
    assertThat(savedMenuItem.itemCategory()).isEqualTo("Khai vị");
    assertThat(savedMenuItem.currentPrice()).isEqualByComparingTo("145000.00");
    assertThat(savedMenuItem.status()).isEqualTo(MenuItemStatus.HET);
    assertThat(savedMenuItem.description()).isEqualTo("Phiên bản mới");
  }

  @Test
  void shouldRejectDuplicateMenuItemNameIgnoringCaseWhenUpdating() {
    MenuItem currentMenuItem =
        new MenuItem(
            5L,
            "Gỏi hải sản",
            "Khai vị",
            new BigDecimal("125000.00"),
            MenuItemStatus.CON,
            "Món mở đầu");

    when(menuItemQueryPort.findMenuItemById(5L)).thenReturn(Optional.of(currentMenuItem));
    when(menuItemQueryPort.existsMenuItemByNameAndIdNot("gỏi cá hồi", 5L)).thenReturn(true);

    UpdateMenuItemService updateMenuItemService =
        new UpdateMenuItemService(menuItemQueryPort, menuItemCommandPort);

    assertThatThrownBy(
            () ->
                updateMenuItemService.updateMenuItem(
                    5L,
                    new UpdateMenuItemCommand(
                        " gỏi cá hồi ",
                        "Khai vị",
                        new BigDecimal("145000.00"),
                        MenuItemStatus.CON,
                        null)))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessage("Menu item name already exists: gỏi cá hồi");

    verify(menuItemCommandPort, never()).saveMenuItem(any(MenuItem.class));
  }
}
