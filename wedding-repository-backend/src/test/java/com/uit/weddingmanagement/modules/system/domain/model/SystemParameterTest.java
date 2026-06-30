package com.uit.weddingmanagement.modules.system.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class SystemParameterTest {

  @Test
  void shouldRejectWhenMinimumDepositPercentageMatchesCurrentValue() {
    SystemParameter systemParameter =
        new SystemParameter(
            SystemParameter.SINGLETON_ID,
            new BigDecimal("50.00"),
            true,
            new BigDecimal("1.00"),
            15,
            new BigDecimal("50.00"));

    assertThatThrownBy(
            () -> systemParameter.updateMinimumDepositPercentage(new BigDecimal("50.00")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Minimum deposit percentage must be different from current value.");
  }

  @Test
  void shouldUpdateLatePaymentPenaltyWithPositiveRate() {
    SystemParameter systemParameter =
        new SystemParameter(
            SystemParameter.SINGLETON_ID,
            new BigDecimal("50.00"),
            true,
            new BigDecimal("1.00"),
            15,
            new BigDecimal("50.00"));

    SystemParameter updatedSystemParameter =
        systemParameter.updateLatePaymentPenalty(false, new BigDecimal("2.50"));

    assertThat(updatedSystemParameter.latePaymentPenaltyEnabled()).isFalse();
    assertThat(updatedSystemParameter.latePaymentPenaltyRate()).isEqualByComparingTo("2.50");
  }

  @Test
  void shouldRejectCancellationPolicyWhenRefundPercentageIsAbove100() {
    SystemParameter systemParameter =
        new SystemParameter(
            SystemParameter.SINGLETON_ID,
            new BigDecimal("50.00"),
            true,
            new BigDecimal("1.00"),
            15,
            new BigDecimal("50.00"));

    assertThatThrownBy(
            () ->
                systemParameter.updateCancellationPolicy(20, new BigDecimal("120.00")))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Percentage must be greater than or equal to 0 and less than or equal to 100.");
  }
}
