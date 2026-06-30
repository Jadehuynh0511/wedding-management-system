package com.uit.weddingmanagement.modules.system.presentation.controller;

import com.uit.weddingmanagement.common.api.ApiResponse;
import com.uit.weddingmanagement.modules.system.application.model.command.UpdateCancellationRuleCommand;
import com.uit.weddingmanagement.modules.system.application.model.command.UpdateDepositRuleCommand;
import com.uit.weddingmanagement.modules.system.application.model.command.UpdateLatePaymentPenaltyCommand;
import com.uit.weddingmanagement.modules.system.application.port.in.GetSystemSettingsUseCase;
import com.uit.weddingmanagement.modules.system.application.port.in.UpdateCancellationRuleUseCase;
import com.uit.weddingmanagement.modules.system.application.port.in.UpdateDepositRuleUseCase;
import com.uit.weddingmanagement.modules.system.application.port.in.UpdateLatePaymentPenaltyUseCase;
import com.uit.weddingmanagement.modules.system.presentation.dto.request.UpdateCancellationRuleRequest;
import com.uit.weddingmanagement.modules.system.presentation.dto.request.UpdateDepositRuleRequest;
import com.uit.weddingmanagement.modules.system.presentation.dto.request.UpdateLatePaymentPenaltyRequest;
import com.uit.weddingmanagement.modules.system.presentation.dto.response.SystemSettingsResponse;
import com.uit.weddingmanagement.modules.system.presentation.mapper.SystemSettingsPresentationMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Controller này gom API thay đổi quy định hệ thống để frontend có thể render đúng 3 card QĐ2/QĐ4/QĐ12.
@Validated
@RestController
@RequestMapping("/api/settings")
@Tag(name = "System Settings", description = "APIs for reading and updating dynamic system rules.")
public class SystemSettingsController {

  private static final String SYSTEM_RULE_MANAGE_PERMISSION =
      "@authorizationService.hasPermission('SYSTEM_RULE_MANAGE')";

  private final GetSystemSettingsUseCase getSystemSettingsUseCase;
  private final UpdateDepositRuleUseCase updateDepositRuleUseCase;
  private final UpdateLatePaymentPenaltyUseCase updateLatePaymentPenaltyUseCase;
  private final UpdateCancellationRuleUseCase updateCancellationRuleUseCase;
  private final SystemSettingsPresentationMapper systemSettingsPresentationMapper;

  public SystemSettingsController(
      GetSystemSettingsUseCase getSystemSettingsUseCase,
      UpdateDepositRuleUseCase updateDepositRuleUseCase,
      UpdateLatePaymentPenaltyUseCase updateLatePaymentPenaltyUseCase,
      UpdateCancellationRuleUseCase updateCancellationRuleUseCase,
      SystemSettingsPresentationMapper systemSettingsPresentationMapper) {
    this.getSystemSettingsUseCase = getSystemSettingsUseCase;
    this.updateDepositRuleUseCase = updateDepositRuleUseCase;
    this.updateLatePaymentPenaltyUseCase = updateLatePaymentPenaltyUseCase;
    this.updateCancellationRuleUseCase = updateCancellationRuleUseCase;
    this.systemSettingsPresentationMapper = systemSettingsPresentationMapper;
  }

  @GetMapping
  @PreAuthorize(SYSTEM_RULE_MANAGE_PERMISSION)
  @Operation(
      summary = "Get current system settings",
      description = "Returns the active rules for deposit, late payment penalty, and cancellation.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "System settings loaded successfully."),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "Current user does not have SYSTEM_RULE_MANAGE permission.")
  })
  public ApiResponse<SystemSettingsResponse> getSystemSettings() {
    return ApiResponse.success(
        "System settings loaded successfully.",
        systemSettingsPresentationMapper.toResponse(getSystemSettingsUseCase.getSystemSettings()));
  }

  @PatchMapping("/deposit-rate")
  @PreAuthorize(SYSTEM_RULE_MANAGE_PERMISSION)
  @Operation(
      summary = "Update deposit rule",
      description = "Updates QĐ2 minimum deposit percentage with range validation.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<SystemSettingsResponse> updateDepositRule(
      @Valid @RequestBody UpdateDepositRuleRequest request) {
    return ApiResponse.success(
        "Minimum deposit percentage updated successfully.",
        systemSettingsPresentationMapper.toResponse(
            updateDepositRuleUseCase.updateDepositRule(
                new UpdateDepositRuleCommand(request.minimumDepositPercentage()))));
  }

  @PatchMapping("/penalty")
  @PreAuthorize(SYSTEM_RULE_MANAGE_PERMISSION)
  @Operation(
      summary = "Update late payment penalty rule",
      description = "Updates QĐ4 late payment penalty toggle and penalty rate.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<SystemSettingsResponse> updateLatePaymentPenaltyRule(
      @Valid @RequestBody UpdateLatePaymentPenaltyRequest request) {
    return ApiResponse.success(
        "Late payment penalty settings updated successfully.",
        systemSettingsPresentationMapper.toResponse(
            updateLatePaymentPenaltyUseCase.updateLatePaymentPenalty(
                new UpdateLatePaymentPenaltyCommand(
                    request.latePaymentPenaltyEnabled(), request.latePaymentPenaltyRate()))));
  }

  @PatchMapping("/cancellation")
  @PreAuthorize(SYSTEM_RULE_MANAGE_PERMISSION)
  @Operation(
      summary = "Update cancellation rule",
      description = "Updates QĐ12 cancellation deadline and deposit refund percentage.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<SystemSettingsResponse> updateCancellationRule(
      @Valid @RequestBody UpdateCancellationRuleRequest request) {
    return ApiResponse.success(
        "Cancellation settings updated successfully.",
        systemSettingsPresentationMapper.toResponse(
            updateCancellationRuleUseCase.updateCancellationRule(
                new UpdateCancellationRuleCommand(
                    request.cancellationDeadlineDays(), request.depositRefundPercentage()))));
  }
}
