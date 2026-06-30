package com.uit.weddingmanagement.modules.booking.presentation.dto.request;

import com.uit.weddingmanagement.modules.booking.domain.model.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateDepositReceiptRequest(
    @NotNull(message = "Deposit receipt amount is required.")
        @DecimalMin(value = "0.01", message = "Deposit receipt amount must be greater than 0.")
        BigDecimal amount,
    PaymentMethod paymentMethod,
    String notes) {}
