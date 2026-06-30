package com.uit.weddingmanagement.modules.system.presentation.dto.response;

import java.math.BigDecimal;

public record CancellationRuleResponse(
    Integer cancellationDeadlineDays, BigDecimal depositRefundPercentage) {}
