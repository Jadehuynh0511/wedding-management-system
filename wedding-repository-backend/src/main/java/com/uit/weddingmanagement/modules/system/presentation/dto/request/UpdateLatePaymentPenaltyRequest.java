package com.uit.weddingmanagement.modules.system.presentation.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record UpdateLatePaymentPenaltyRequest(
    @NotNull(message = "Late payment penalty enabled flag is required.")
        Boolean latePaymentPenaltyEnabled,
    @NotNull(message = "Late payment penalty rate is required.")
        @DecimalMin(
            value = "0.01",
            message = "Late payment penalty rate must be greater than 0.")
        BigDecimal latePaymentPenaltyRate) {}
