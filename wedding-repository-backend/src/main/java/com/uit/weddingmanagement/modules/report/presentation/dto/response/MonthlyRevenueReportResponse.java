package com.uit.weddingmanagement.modules.report.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Schema(
    name = "MonthlyRevenueReportResponse",
    description = "Monthly revenue report grouped by daily paid revenue.")
public record MonthlyRevenueReportResponse(
    @Schema(description = "Requested month from 1 to 12.", example = "5") int reportMonth,
    @Schema(description = "Requested year.", example = "2026") int reportYear,
    @Schema(
            description = "UTC timestamp when the backend generated this report payload.",
            example = "2026-05-31T16:59:59Z")
        Instant generatedAt,
    @Schema(
            description = "Total revenue of fully paid wedding bookings within the month.",
            example = "27500000.00")
        BigDecimal totalRevenue,
    @Schema(
            description = "Total number of fully paid wedding bookings within the month.",
            example = "4")
        int totalWeddingBookings,
    @Schema(description = "Daily breakdown for the entire month.")
        List<MonthlyRevenueReportItemResponse> items) {}
