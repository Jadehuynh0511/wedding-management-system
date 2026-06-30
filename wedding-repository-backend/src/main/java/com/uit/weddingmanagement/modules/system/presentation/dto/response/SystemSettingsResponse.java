package com.uit.weddingmanagement.modules.system.presentation.dto.response;

public record SystemSettingsResponse(
    Short id,
    DepositRuleResponse depositRule,
    LatePaymentPenaltyRuleResponse latePaymentPenaltyRule,
    CancellationRuleResponse cancellationRule) {}
