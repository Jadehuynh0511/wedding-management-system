package com.uit.weddingmanagement.modules.report.presentation.controller;

import com.uit.weddingmanagement.common.api.ApiResponse;
import com.uit.weddingmanagement.modules.report.application.port.in.GetMonthlyRevenueReportUseCase;
import com.uit.weddingmanagement.modules.report.presentation.dto.response.MonthlyRevenueReportResponse;
import com.uit.weddingmanagement.modules.report.presentation.mapper.ReportPresentationMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/reports")
@Tag(name = "Reports", description = "APIs for generating monthly revenue reports.")
public class MonthlyRevenueReportController {

  private static final String MONTHLY_REVENUE_REPORT_PERMISSION =
      "@authorizationService.hasPermission('MONTHLY_REVENUE_REPORT_GENERATE')";

  private final GetMonthlyRevenueReportUseCase getMonthlyRevenueReportUseCase;
  private final ReportPresentationMapper reportPresentationMapper;

  public MonthlyRevenueReportController(
      GetMonthlyRevenueReportUseCase getMonthlyRevenueReportUseCase,
      ReportPresentationMapper reportPresentationMapper) {
    this.getMonthlyRevenueReportUseCase = getMonthlyRevenueReportUseCase;
    this.reportPresentationMapper = reportPresentationMapper;
  }

  @GetMapping("/monthly")
  @PreAuthorize(MONTHLY_REVENUE_REPORT_PERMISSION)
  @Operation(
      summary = "Get monthly revenue report",
      description =
          "Returns BM5 monthly revenue totals and a full day-by-day breakdown using only fully paid wedding bookings.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Monthly revenue report loaded successfully."),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description =
            "Current user does not have MONTHLY_REVENUE_REPORT_GENERATE permission.")
  })
  public ApiResponse<MonthlyRevenueReportResponse> getMonthlyRevenueReport(
      @RequestParam("month")
          @Min(value = 1, message = "Month must be between 1 and 12.")
          @Max(value = 12, message = "Month must be between 1 and 12.")
          int month,
      @RequestParam("year")
          @Min(value = 2000, message = "Year must be between 2000 and 2100.")
          @Max(value = 2100, message = "Year must be between 2000 and 2100.")
          int year) {
    return ApiResponse.success(
        "Monthly revenue report loaded successfully.",
        reportPresentationMapper.toResponse(getMonthlyRevenueReportUseCase.getMonthlyRevenueReport(month, year)));
  }
}
