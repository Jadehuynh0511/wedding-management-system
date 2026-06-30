package com.uit.weddingmanagement.modules.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.common.exception.DuplicateResourceException;
import com.uit.weddingmanagement.modules.catalog.application.model.command.CreateMenuItemCommand;
import com.uit.weddingmanagement.modules.catalog.application.port.out.MenuItemCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.MenuItemQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.MenuItem;
import com.uit.weddingmanagement.modules.catalog.domain.model.MenuItemStatus;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateMenuItemServiceTest {

  @Mock private MenuItemQueryPort menuItemQueryPort;

  @Mock private MenuItemCommandPort menuItemCommandPort;

  @Captor private ArgumentCaptor<MenuItem> menuItemCaptor;

  @Test
  void shouldCreateMenuItemWhenRequestIsValid() {
    when(menuItemQueryPort.existsMenuItemByName("Gỏi hải sản")).thenReturn(false);
    when(menuItemCommandPort.saveMenuItem(any(MenuItem.class)))
        .thenReturn(
            new MenuItem(
                12L,
                "Gỏi hải sản",
                "Khai vị",
                new BigDecimal("125000.00"),
                MenuItemStatus.CON,
                "Món mở đầu"));

    CreateMenuItemService createMenuItemService =
        new CreateMenuItemService(menuItemQueryPort, menuItemCommandPort);

    var result =
        createMenuItemService.createMenuItem(
            new CreateMenuItemCommand(
                "  Gỏi   hải sản  ",
                "  Khai   vị  ",
                new BigDecimal("125000.00"),
                null,
                "  Món mở đầu  "));

    assertThat(result.id()).isEqualTo(12L);
    assertThat(result.itemName()).isEqualTo("Gỏi hải sản");
    assertThat(result.itemCategory()).isEqualTo("Khai vị");
    assertThat(result.currentPrice()).isEqualByComparingTo("125000.00");
    assertThat(result.status()).isEqualTo(MenuItemStatus.CON);
    assertThat(result.available()).isTrue();

    verify(menuItemCommandPort).saveMenuItem(menuItemCaptor.capture());
    MenuItem savedMenuItem = menuItemCaptor.getValue();
    assertThat(savedMenuItem.id()).isNull();
    assertThat(savedMenuItem.itemName()).isEqualTo("Gỏi hải sản");
    assertThat(savedMenuItem.itemCategory()).isEqualTo("Khai vị");
    assertThat(savedMenuItem.currentPrice()).isEqualByComparingTo("125000.00");
    assertThat(savedMenuItem.status()).isEqualTo(MenuItemStatus.CON);
    assertThat(savedMenuItem.description()).isEqualTo("Món mở đầu");
  }

  @Test
  void shouldRejectDuplicateMenuItemNameIgnoringCase() {
    when(menuItemQueryPort.existsMenuItemByName("gỏi hải sản")).thenReturn(true);

    CreateMenuItemService createMenuItemService =
        new CreateMenuItemService(menuItemQueryPort, menuItemCommandPort);

    assertThatThrownBy(
            () ->
                createMenuItemService.createMenuItem(
                    new CreateMenuItemCommand(
                        " gỏi hải sản ",
                        "Khai vị",
                        new BigDecimal("125000.00"),
                        MenuItemStatus.CON,
                        null)))
        .isInstanceOf(DuplicateResourceException.class)
        .hasMessage("Menu item name already exists: gỏi hải sản");

    verify(menuItemCommandPort, never()).saveMenuItem(any(MenuItem.class));
  }
}
