package com.uit.weddingmanagement.modules.catalog.presentation.dto.response;

import java.math.BigDecimal;

public record HallTypeResponse(
    Long id, String hallTypeName, BigDecimal minimumTablePrice, String description) {}
