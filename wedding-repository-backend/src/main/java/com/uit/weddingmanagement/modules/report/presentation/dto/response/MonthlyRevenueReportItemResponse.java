package com.uit.weddingmanagement.modules.report.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(
    name = "MonthlyRevenueReportItemResponse",
    description = "Daily revenue row within a monthly revenue report.")
public record MonthlyRevenueReportItemResponse(
    @Schema(description = "Business date of the paid invoices.", example = "2026-05-03")
        LocalDate reportDate,
    @Schema(description = "Number of fully paid wedding bookings on that date.", example = "2")
        int weddingBookingCount,
    @Schema(description = "Revenue collected on that date.", example = "12500000.00")
        BigDecimal revenue,
    @Schema(description = "Revenue percentage contribution within the month.", example = "45.4545")
        BigDecimal revenueRatio) {}
