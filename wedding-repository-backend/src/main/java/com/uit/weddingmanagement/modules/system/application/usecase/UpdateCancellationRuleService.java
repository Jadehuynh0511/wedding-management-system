package com.uit.weddingmanagement.modules.system.application.usecase;

import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.system.application.model.result.SystemSettingsResult;
import com.uit.weddingmanagement.modules.system.application.model.command.UpdateCancellationRuleCommand;
import com.uit.weddingmanagement.modules.system.application.port.in.UpdateCancellationRuleUseCase;
import com.uit.weddingmanagement.modules.system.application.port.out.SystemParameterCommandPort;
import com.uit.weddingmanagement.modules.system.application.port.out.SystemParameterQueryPort;
import com.uit.weddingmanagement.modules.system.domain.model.SystemParameter;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateCancellationRuleService implements UpdateCancellationRuleUseCase {

  private final SystemParameterQueryPort systemParameterQueryPort;
  private final SystemParameterCommandPort systemParameterCommandPort;

  public UpdateCancellationRuleService(
      SystemParameterQueryPort systemParameterQueryPort,
      SystemParameterCommandPort systemParameterCommandPort) {
    this.systemParameterQueryPort = systemParameterQueryPort;
    this.systemParameterCommandPort = systemParameterCommandPort;
  }

  @Override
  @AuditAction(
      action = "SYSTEM_RULE_UPDATE_CANCELLATION",
      module = "SYSTEM",
      targetType = "SYSTEM_PARAMETER",
      targetIdExpression = "#result.id",
      successDescriptionExpression = "'Updated cancellation rule'",
      failureDescriptionExpression = "'Failed to update cancellation rule'",
      detailsExpression = "#command")
  public SystemSettingsResult updateCancellationRule(UpdateCancellationRuleCommand command) {
    SystemParameter updatedSystemParameter =
        loadSystemParameter()
            .updateCancellationPolicy(
                command.cancellationDeadlineDays(), command.depositRefundPercentage());

    return SystemSettingsResult.from(systemParameterCommandPort.saveSystemParameter(updatedSystemParameter));
  }

  private SystemParameter loadSystemParameter() {
    return systemParameterQueryPort
        .getSystemParameter()
        .orElseThrow(() -> new EntityNotFoundException("System settings have not been initialized."));
  }
}
