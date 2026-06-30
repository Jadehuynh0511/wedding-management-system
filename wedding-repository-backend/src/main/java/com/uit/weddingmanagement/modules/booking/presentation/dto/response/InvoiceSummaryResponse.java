package com.uit.weddingmanagement.modules.booking.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Schema(
    name = "InvoiceSummaryResponse",
    description = "Summary row used by invoice list results.")
public record InvoiceSummaryResponse(
    @Schema(description = "Invoice id.", example = "901") Long id,
    @Schema(description = "Wedding booking id.", example = "44") Long weddingBookingId,
    @Schema(description = "Hall id.", example = "7") Long hallId,
    @Schema(description = "Hall name.", example = "Sunrise Hall") String hallName,
    @Schema(description = "Shift id.", example = "2") Long shiftId,
    @Schema(description = "Shift name.", example = "Evening") String shiftName,
    @Schema(description = "Groom name.", example = "Minh") String groomName,
    @Schema(description = "Bride name.", example = "Lan") String brideName,
    @Schema(description = "Combined couple name.", example = "Minh & Lan") String coupleName,
    @Schema(description = "Celebration date.", example = "2026-08-15") LocalDate celebrationDate,
    @Schema(description = "Payment time.", example = "2026-08-16T15:00:00Z") Instant paidAt,
    @Schema(description = "Final invoice amount.", example = "60392500.00")
        BigDecimal finalAmount) {}
