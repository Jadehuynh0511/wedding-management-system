package com.uit.weddingmanagement.modules.booking.application.model.command;

import com.uit.weddingmanagement.modules.booking.domain.model.PaymentMethod;
import java.math.BigDecimal;

public record CreateDepositReceiptCommand(BigDecimal amount, PaymentMethod paymentMethod, String notes) {}
