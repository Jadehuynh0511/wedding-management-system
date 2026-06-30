package com.uit.weddingmanagement.modules.system.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateCancellationRuleRequest(
    @NotNull(message = "Cancellation deadline days is required.")
        @Min(value = 1, message = "Cancellation deadline days must be greater than 0.")
        Integer cancellationDeadlineDays,
    @NotNull(message = "Deposit refund percentage is required.")
        @DecimalMin(
            value = "0.00",
            inclusive = true,
            message = "Deposit refund percentage must be greater than or equal to 0.")
        BigDecimal depositRefundPercentage) {}
