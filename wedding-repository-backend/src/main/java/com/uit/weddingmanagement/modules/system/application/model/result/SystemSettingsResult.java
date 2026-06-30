package com.uit.weddingmanagement.modules.system.application.model.result;

import com.uit.weddingmanagement.modules.system.domain.model.SystemParameter;

public record SystemSettingsResult(
    Short id,
    DepositRuleResult depositRule,
    LatePaymentPenaltyRuleResult latePaymentPenaltyRule,
    CancellationRuleResult cancellationRule) {

  public static SystemSettingsResult from(SystemParameter systemParameter) {
    return new SystemSettingsResult(
        systemParameter.id(),
        new DepositRuleResult(systemParameter.minimumDepositPercentage()),
        new LatePaymentPenaltyRuleResult(
            systemParameter.latePaymentPenaltyEnabled(),
            systemParameter.latePaymentPenaltyRate()),
        new CancellationRuleResult(
            systemParameter.cancellationDeadlineDays(),
            systemParameter.depositRefundPercentage()));
  }
}
