package com.uit.weddingmanagement.modules.catalog.presentation.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record ServicePriceHistoryResponse(
    Long id, Long serviceItemId, BigDecimal oldPrice, Instant effectiveFrom, Instant effectiveTo) {}
