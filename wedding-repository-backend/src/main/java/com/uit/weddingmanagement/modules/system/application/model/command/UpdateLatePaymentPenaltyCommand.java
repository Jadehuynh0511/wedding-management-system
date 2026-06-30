package com.uit.weddingmanagement.modules.system.application.model.command;

import java.math.BigDecimal;

public record UpdateLatePaymentPenaltyCommand(
    Boolean latePaymentPenaltyEnabled, BigDecimal latePaymentPenaltyRate) {}
