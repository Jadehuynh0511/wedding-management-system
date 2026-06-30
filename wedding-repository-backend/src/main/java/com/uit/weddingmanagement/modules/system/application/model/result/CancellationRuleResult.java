package com.uit.weddingmanagement.modules.system.application.model.result;

import java.math.BigDecimal;

public record CancellationRuleResult(
    Integer cancellationDeadlineDays, BigDecimal depositRefundPercentage) {}
