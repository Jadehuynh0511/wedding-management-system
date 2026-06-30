package com.uit.weddingmanagement.modules.catalog.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.modules.catalog.application.port.out.MenuItemQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.MenuItem;
import com.uit.weddingmanagement.modules.catalog.domain.model.MenuItemStatus;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListMenuItemsServiceTest {

  @Mock private MenuItemQueryPort menuItemQueryPort;

  @Test
  void shouldReturnOnlyAvailableMenuItemsWhenFilterIsTrue() {
    when(menuItemQueryPort.findMenuItems(true))
        .thenReturn(
            List.of(
                new MenuItem(
                    3L,
                    "Gỏi hải sản",
                    "Khai vị",
                    new BigDecimal("125000.00"),
                    MenuItemStatus.CON,
                    null)));

    ListMenuItemsService listMenuItemsService = new ListMenuItemsService(menuItemQueryPort);

    var result = listMenuItemsService.listMenuItems(true);

    assertThat(result).hasSize(1);
    assertThat(result.getFirst().available()).isTrue();
    assertThat(result.getFirst().status()).isEqualTo(MenuItemStatus.CON);
    verify(menuItemQueryPort).findMenuItems(true);
  }
}
