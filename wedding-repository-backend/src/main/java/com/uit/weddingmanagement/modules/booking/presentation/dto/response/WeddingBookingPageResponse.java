package com.uit.weddingmanagement.modules.booking.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(
    name = "WeddingBookingPageResponse",
    description = "Paged wedding booking search result for BM3.")
public record WeddingBookingPageResponse(
    @Schema(description = "Current page items.") List<WeddingBookingSummaryResponse> items,
    @Schema(description = "Total matching rows.", example = "37") long totalElements,
    @Schema(description = "Total pages.", example = "2") int totalPages,
    @Schema(description = "Current page index starting from 0.", example = "0") int page,
    @Schema(description = "Requested page size.", example = "20") int size) {}
