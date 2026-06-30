package com.uit.weddingmanagement.modules.system.application.model.command;

import java.math.BigDecimal;

public record UpdateCancellationRuleCommand(
    Integer cancellationDeadlineDays, BigDecimal depositRefundPercentage) {}
