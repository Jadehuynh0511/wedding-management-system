package com.uit.weddingmanagement.modules.system.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateDepositRuleRequest(
    @NotNull(message = "Minimum deposit percentage is required.")
        @DecimalMin(
            value = "0.01",
            message = "Minimum deposit percentage must be greater than 0.")
        BigDecimal minimumDepositPercentage) {}
