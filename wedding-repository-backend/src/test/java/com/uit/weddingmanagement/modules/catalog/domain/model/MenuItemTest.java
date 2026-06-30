package com.uit.weddingmanagement.modules.catalog.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MenuItemTest {

  @Test
  void shouldNormalizeFieldsAndDefaultStatusWhenCreatingMenuItem() {
    MenuItem menuItem =
        MenuItem.create(
            "  Gỏi   hải sản  ",
            "  Khai   vị  ",
            new BigDecimal("125000.00"),
            null,
            "  Món mở đầu  ");

    assertThat(menuItem.id()).isNull();
    assertThat(menuItem.itemName()).isEqualTo("Gỏi hải sản");
    assertThat(menuItem.itemCategory()).isEqualTo("Khai vị");
    assertThat(menuItem.currentPrice()).isEqualByComparingTo("125000.00");
    assertThat(menuItem.status()).isEqualTo(MenuItemStatus.CON);
    assertThat(menuItem.isAvailable()).isTrue();
    assertThat(menuItem.description()).isEqualTo("Món mở đầu");
  }

  @Test
  void shouldRejectWhenCurrentPriceIsNotPositive() {
    assertThatThrownBy(
            () ->
                MenuItem.create(
                    "Gỏi hải sản", "Khai vị", BigDecimal.ZERO, MenuItemStatus.CON, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Menu item current price must be greater than 0.");
  }

  @Test
  void shouldRejectWhenUpdatingMenuItemWithoutId() {
    MenuItem menuItem =
        MenuItem.create(
            "Gỏi hải sản", "Khai vị", new BigDecimal("125000.00"), MenuItemStatus.CON, null);

    assertThatThrownBy(
            () ->
                menuItem.update(
                    "Gỏi cá hồi",
                    "Khai vị",
                    new BigDecimal("145000.00"),
                    MenuItemStatus.HET,
                    "Updated"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Cannot update a menu item without id.");
  }
}
