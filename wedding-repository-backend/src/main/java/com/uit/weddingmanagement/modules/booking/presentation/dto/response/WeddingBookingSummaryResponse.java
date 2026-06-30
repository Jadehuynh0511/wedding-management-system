package com.uit.weddingmanagement.modules.booking.presentation.dto.response;

import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(
    name = "WeddingBookingSummaryResponse",
    description = "Summary row used by BM3 wedding booking search results.")
public record WeddingBookingSummaryResponse(
    @Schema(description = "Wedding booking id.", example = "44") Long id,
    @Schema(description = "Hall id.", example = "7") Long hallId,
    @Schema(description = "Hall name.", example = "Sunrise Hall") String hallName,
    @Schema(description = "Shift id.", example = "2") Long shiftId,
    @Schema(description = "Shift name.", example = "Evening") String shiftName,
    @Schema(description = "Groom name.", example = "Minh") String groomName,
    @Schema(description = "Bride name.", example = "Lan") String brideName,
    @Schema(description = "Combined couple name.", example = "Minh & Lan") String coupleName,
    @Schema(description = "Celebration date.", example = "2026-08-15") LocalDate celebrationDate,
    @Schema(description = "Confirmed table count.", example = "20") Integer tableCount,
    @Schema(description = "Wedding booking status.", example = "DA_XAC_NHAN")
        WeddingBookingStatus status) {}
