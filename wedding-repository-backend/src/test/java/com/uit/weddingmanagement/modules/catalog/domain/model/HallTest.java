package com.uit.weddingmanagement.modules.catalog.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class HallTest {

  @Test
  void shouldDefaultStatusAndNormalizeFieldsWhenCreatingHall() {
    HallType hallType = new HallType(3L, "Emerald", new BigDecimal("5500000.00"), "Premium");

    Hall hall =
        Hall.create(
            hallType,
            "  Grand   Palace  ",
            420,
            new BigDecimal("6200000.00"),
            null,
            "  Sảnh trung tâm  ");

    assertThat(hall.id()).isNull();
    assertThat(hall.hallName()).isEqualTo("Grand Palace");
    assertThat(hall.status()).isEqualTo(HallStatus.TRONG);
    assertThat(hall.description()).isEqualTo("Sảnh trung tâm");
  }

  @Test
  void shouldRejectWhenTablePriceIsBelowHallTypeMinimum() {
    HallType hallType = new HallType(3L, "Emerald", new BigDecimal("5500000.00"), "Premium");

    assertThatThrownBy(
            () ->
                Hall.create(
                    hallType,
                    "Grand Palace",
                    420,
                    new BigDecimal("5400000.00"),
                    HallStatus.TRONG,
                    null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Table price must be greater than or equal to minimum table price of the hall type.");
  }

  @Test
  void shouldRejectWhenUpdatingWithoutStatus() {
    HallType hallType = new HallType(5L, "Diamond", new BigDecimal("6800000.00"), "VIP");
    Hall existingHall =
        new Hall(
            21L,
            hallType,
            "Royal Garden",
            500,
            new BigDecimal("7000000.00"),
            HallStatus.TRONG,
            null);

    assertThatThrownBy(
            () ->
                existingHall.update(
                    hallType,
                    "Royal Garden Plus",
                    550,
                    new BigDecimal("7200000.00"),
                    null,
                    null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Hall status is required.");
  }
}
