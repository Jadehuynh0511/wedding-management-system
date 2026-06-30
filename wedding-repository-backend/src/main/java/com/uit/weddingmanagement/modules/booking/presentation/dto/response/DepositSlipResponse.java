package com.uit.weddingmanagement.modules.booking.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(
    name = "DepositSlipResponse",
    description = "Read-only deposit slip response for BM10.")
public record DepositSlipResponse(
    @Schema(description = "Wedding booking id.", example = "44") Long weddingBookingId,
    @Schema(description = "Hall id.", example = "7") Long hallId,
    @Schema(description = "Hall name.", example = "Sunrise Hall") String hallName,
    @Schema(description = "Shift id.", example = "2") Long shiftId,
    @Schema(description = "Shift name.", example = "Evening") String shiftName,
    @Schema(description = "Groom name.", example = "Minh") String groomName,
    @Schema(description = "Bride name.", example = "Lan") String brideName,
    @Schema(description = "Combined couple name.", example = "Minh & Lan") String coupleName,
    @Schema(description = "Booking date.", example = "2026-06-01") LocalDate bookingDate,
    @Schema(description = "Celebration date.", example = "2026-08-15") LocalDate celebrationDate,
    @Schema(description = "Confirmed table count.", example = "20") Integer tableCount,
    @Schema(description = "Reserved table count.", example = "2") Integer reservedTableCount,
    @Schema(description = "Hall-only total amount.", example = "100000000.00")
        BigDecimal hallTotalAmount,
    @Schema(description = "Immutable deposit receipt snapshot.") DepositReceiptResponse depositReceipt) {}
