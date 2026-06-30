package com.uit.weddingmanagement.modules.catalog.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class HallTypeTest {

  @Test
  void shouldNormalizeFieldsWhenCreatingHallType() {
    HallType hallType =
        HallType.create(
            "  Ruby   Elite  ", new BigDecimal("4500000.00"), "  Premium concept  ");

    assertThat(hallType.id()).isNull();
    assertThat(hallType.hallTypeName()).isEqualTo("Ruby Elite");
    assertThat(hallType.minimumTablePrice()).isEqualByComparingTo("4500000.00");
    assertThat(hallType.description()).isEqualTo("Premium concept");
  }

  @Test
  void shouldRejectWhenMinimumTablePriceIsNotPositive() {
    assertThatThrownBy(() -> HallType.create("Ruby", BigDecimal.ZERO, null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Minimum table price must be greater than 0.");
  }

  @Test
  void shouldRejectWhenUpdatingHallTypeWithoutId() {
    HallType hallType = HallType.create("Ruby", new BigDecimal("3200000.00"), null);

    assertThatThrownBy(
            () -> hallType.update("Ruby Premium", new BigDecimal("4200000.00"), "Updated"))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Cannot update a hall type without id.");
  }
}
