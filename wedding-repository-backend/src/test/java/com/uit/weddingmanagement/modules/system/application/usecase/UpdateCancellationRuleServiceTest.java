package com.uit.weddingmanagement.modules.system.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.modules.system.application.model.command.UpdateCancellationRuleCommand;
import com.uit.weddingmanagement.modules.system.application.port.out.SystemParameterCommandPort;
import com.uit.weddingmanagement.modules.system.application.port.out.SystemParameterQueryPort;
import com.uit.weddingmanagement.modules.system.domain.model.SystemParameter;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateCancellationRuleServiceTest {

  @Mock private SystemParameterQueryPort systemParameterQueryPort;

  @Mock private SystemParameterCommandPort systemParameterCommandPort;

  @Captor private ArgumentCaptor<SystemParameter> systemParameterCaptor;

  @Test
  void shouldUpdateCancellationRule() {
    SystemParameter existingSystemParameter =
        new SystemParameter(
            SystemParameter.SINGLETON_ID,
            new BigDecimal("50.00"),
            true,
            new BigDecimal("1.00"),
            15,
            new BigDecimal("50.00"));

    when(systemParameterQueryPort.getSystemParameter()).thenReturn(Optional.of(existingSystemParameter));
    when(systemParameterCommandPort.saveSystemParameter(any(SystemParameter.class)))
        .thenAnswer(invocation -> invocation.getArgument(0, SystemParameter.class));

    UpdateCancellationRuleService updateCancellationRuleService =
        new UpdateCancellationRuleService(systemParameterQueryPort, systemParameterCommandPort);

    var result =
        updateCancellationRuleService.updateCancellationRule(
            new UpdateCancellationRuleCommand(20, new BigDecimal("40.00")));

    assertThat(result.cancellationRule().cancellationDeadlineDays()).isEqualTo(20);
    assertThat(result.cancellationRule().depositRefundPercentage()).isEqualByComparingTo("40.00");

    verify(systemParameterCommandPort).saveSystemParameter(systemParameterCaptor.capture());
    assertThat(systemParameterCaptor.getValue().cancellationDeadlineDays()).isEqualTo(20);
    assertThat(systemParameterCaptor.getValue().depositRefundPercentage())
        .isEqualByComparingTo("40.00");
  }
}
