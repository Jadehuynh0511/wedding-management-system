package com.uit.weddingmanagement.modules.system.application.usecase;

import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.system.application.model.result.SystemSettingsResult;
import com.uit.weddingmanagement.modules.system.application.model.command.UpdateDepositRuleCommand;
import com.uit.weddingmanagement.modules.system.application.port.in.UpdateDepositRuleUseCase;
import com.uit.weddingmanagement.modules.system.application.port.out.SystemParameterCommandPort;
import com.uit.weddingmanagement.modules.system.application.port.out.SystemParameterQueryPort;
import com.uit.weddingmanagement.modules.system.domain.model.SystemParameter;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateDepositRuleService implements UpdateDepositRuleUseCase {

  private final SystemParameterQueryPort systemParameterQueryPort;
  private final SystemParameterCommandPort systemParameterCommandPort;

  public UpdateDepositRuleService(
      SystemParameterQueryPort systemParameterQueryPort,
      SystemParameterCommandPort systemParameterCommandPort) {
    this.systemParameterQueryPort = systemParameterQueryPort;
    this.systemParameterCommandPort = systemParameterCommandPort;
  }

  @Override
  @AuditAction(
      action = "SYSTEM_RULE_UPDATE_DEPOSIT",
      module = "SYSTEM",
      targetType = "SYSTEM_PARAMETER",
      targetIdExpression = "#result.id",
      successDescriptionExpression = "'Updated minimum deposit percentage rule'",
      failureDescriptionExpression = "'Failed to update minimum deposit percentage rule'",
      detailsExpression = "#command")
  public SystemSettingsResult updateDepositRule(UpdateDepositRuleCommand command) {
    SystemParameter updatedSystemParameter =
        loadSystemParameter().updateMinimumDepositPercentage(command.minimumDepositPercentage());

    return SystemSettingsResult.from(systemParameterCommandPort.saveSystemParameter(updatedSystemParameter));
  }

  private SystemParameter loadSystemParameter() {
    return systemParameterQueryPort
        .getSystemParameter()
        .orElseThrow(() -> new EntityNotFoundException("System settings have not been initialized."));
  }
}
