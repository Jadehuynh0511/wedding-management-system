package com.uit.weddingmanagement.modules.system.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.modules.system.application.port.out.SystemParameterQueryPort;
import com.uit.weddingmanagement.modules.system.domain.model.SystemParameter;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetSystemSettingsServiceTest {

  @Mock private SystemParameterQueryPort systemParameterQueryPort;

  @Test
  void shouldReturnCurrentSystemSettings() {
    when(systemParameterQueryPort.getSystemParameter())
        .thenReturn(
            Optional.of(
                new SystemParameter(
                    SystemParameter.SINGLETON_ID,
                    new BigDecimal("50.00"),
                    true,
                    new BigDecimal("1.00"),
                    15,
                    new BigDecimal("50.00"))));

    GetSystemSettingsService getSystemSettingsService =
        new GetSystemSettingsService(systemParameterQueryPort);

    var result = getSystemSettingsService.getSystemSettings();

    assertThat(result.id()).isEqualTo(SystemParameter.SINGLETON_ID);
    assertThat(result.depositRule().minimumDepositPercentage()).isEqualByComparingTo("50.00");
    assertThat(result.latePaymentPenaltyRule().latePaymentPenaltyEnabled()).isTrue();
    assertThat(result.cancellationRule().cancellationDeadlineDays()).isEqualTo(15);
  }
}
