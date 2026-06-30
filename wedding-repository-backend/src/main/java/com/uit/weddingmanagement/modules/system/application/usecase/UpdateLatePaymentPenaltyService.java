package com.uit.weddingmanagement.modules.system.application.usecase;

import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.system.application.model.result.SystemSettingsResult;
import com.uit.weddingmanagement.modules.system.application.model.command.UpdateLatePaymentPenaltyCommand;
import com.uit.weddingmanagement.modules.system.application.port.in.UpdateLatePaymentPenaltyUseCase;
import com.uit.weddingmanagement.modules.system.application.port.out.SystemParameterCommandPort;
import com.uit.weddingmanagement.modules.system.application.port.out.SystemParameterQueryPort;
import com.uit.weddingmanagement.modules.system.domain.model.SystemParameter;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateLatePaymentPenaltyService implements UpdateLatePaymentPenaltyUseCase {

  private final SystemParameterQueryPort systemParameterQueryPort;
  private final SystemParameterCommandPort systemParameterCommandPort;

  public UpdateLatePaymentPenaltyService(
      SystemParameterQueryPort systemParameterQueryPort,
      SystemParameterCommandPort systemParameterCommandPort) {
    this.systemParameterQueryPort = systemParameterQueryPort;
    this.systemParameterCommandPort = systemParameterCommandPort;
  }

  @Override
  @AuditAction(
      action = "SYSTEM_RULE_UPDATE_LATE_PAYMENT_PENALTY",
      module = "SYSTEM",
      targetType = "SYSTEM_PARAMETER",
      targetIdExpression = "#result.id",
      successDescriptionExpression = "'Updated late payment penalty rule'",
      failureDescriptionExpression = "'Failed to update late payment penalty rule'",
      detailsExpression = "#command")
  public SystemSettingsResult updateLatePaymentPenalty(UpdateLatePaymentPenaltyCommand command) {
    SystemParameter updatedSystemParameter =
        loadSystemParameter()
            .updateLatePaymentPenalty(
                requirePenaltyEnabled(command.latePaymentPenaltyEnabled()),
                command.latePaymentPenaltyRate());

    return SystemSettingsResult.from(systemParameterCommandPort.saveSystemParameter(updatedSystemParameter));
  }

  private SystemParameter loadSystemParameter() {
    return systemParameterQueryPort
        .getSystemParameter()
        .orElseThrow(() -> new EntityNotFoundException("System settings have not been initialized."));
  }

  private boolean requirePenaltyEnabled(Boolean latePaymentPenaltyEnabled) {
    if (latePaymentPenaltyEnabled == null) {
      throw new IllegalArgumentException("Late payment penalty enabled flag is required.");
    }

    return latePaymentPenaltyEnabled;
  }
}
