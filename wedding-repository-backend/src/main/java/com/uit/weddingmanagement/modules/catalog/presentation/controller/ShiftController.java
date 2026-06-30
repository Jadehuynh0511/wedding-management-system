package com.uit.weddingmanagement.modules.catalog.presentation.controller;

import com.uit.weddingmanagement.common.api.ApiResponse;
import com.uit.weddingmanagement.modules.catalog.application.model.command.CreateShiftCommand;
import com.uit.weddingmanagement.modules.catalog.application.model.command.UpdateShiftCommand;
import com.uit.weddingmanagement.modules.catalog.application.port.in.CreateShiftUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.in.DeleteShiftUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.in.GetShiftUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.in.ListShiftsUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.in.UpdateShiftUseCase;
import com.uit.weddingmanagement.modules.catalog.presentation.dto.request.CreateShiftRequest;
import com.uit.weddingmanagement.modules.catalog.presentation.dto.request.UpdateShiftRequest;
import com.uit.weddingmanagement.modules.catalog.presentation.dto.response.ShiftResponse;
import com.uit.weddingmanagement.modules.catalog.presentation.mapper.ShiftPresentationMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Controller này gom toàn bộ API CRUD cho danh mục ca theo naming `Shift` đang được dùng thống
// nhất trong source code hiện tại.
@Validated
@RestController
@RequestMapping("/api/shifts")
@Tag(name = "Shifts", description = "APIs for managing wedding booking shifts.")
public class ShiftController {

  private static final String SHIFT_MANAGE_PERMISSION =
      "@authorizationService.hasPermission('SHIFT_MANAGE')";

  private final ListShiftsUseCase listShiftsUseCase;
  private final GetShiftUseCase getShiftUseCase;
  private final CreateShiftUseCase createShiftUseCase;
  private final UpdateShiftUseCase updateShiftUseCase;
  private final DeleteShiftUseCase deleteShiftUseCase;
  private final ShiftPresentationMapper shiftPresentationMapper;

  public ShiftController(
      ListShiftsUseCase listShiftsUseCase,
      GetShiftUseCase getShiftUseCase,
      CreateShiftUseCase createShiftUseCase,
      UpdateShiftUseCase updateShiftUseCase,
      DeleteShiftUseCase deleteShiftUseCase,
      ShiftPresentationMapper shiftPresentationMapper) {
    this.listShiftsUseCase = listShiftsUseCase;
    this.getShiftUseCase = getShiftUseCase;
    this.createShiftUseCase = createShiftUseCase;
    this.updateShiftUseCase = updateShiftUseCase;
    this.deleteShiftUseCase = deleteShiftUseCase;
    this.shiftPresentationMapper = shiftPresentationMapper;
  }

  @GetMapping
  @PreAuthorize(SHIFT_MANAGE_PERMISSION)
  @Operation(
      summary = "List shifts",
      description = "Returns all configured wedding booking shifts for catalog management.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Shifts loaded successfully."),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "Current user does not have SHIFT_MANAGE permission.")
  })
  public ApiResponse<List<ShiftResponse>> listShifts() {
    List<ShiftResponse> shifts =
        listShiftsUseCase.listShifts().stream()
            .map(shiftPresentationMapper::toResponse)
            .toList();

    return ApiResponse.success("Shifts loaded successfully.", shifts);
  }

  @GetMapping("/{shiftId}")
  @PreAuthorize(SHIFT_MANAGE_PERMISSION)
  @Operation(summary = "Get one shift", description = "Returns details for the requested shift.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<ShiftResponse> getShift(
      @PathVariable @Positive(message = "Shift id must be greater than 0.") Long shiftId) {
    return ApiResponse.success(
        "Shift loaded successfully.", shiftPresentationMapper.toResponse(getShiftUseCase.getShift(shiftId)));
  }

  @PostMapping
  @PreAuthorize(SHIFT_MANAGE_PERMISSION)
  @Operation(
      summary = "Create shift",
      description =
          "Creates a new shift after validating unique shift name and closed-interval time overlap.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<ShiftResponse> createShift(@Valid @RequestBody CreateShiftRequest request) {
    return ApiResponse.success(
        "Shift created successfully.",
        shiftPresentationMapper.toResponse(
            createShiftUseCase.createShift(
                new CreateShiftCommand(
                    request.shiftName(),
                    request.startTime(),
                    request.endTime(),
                    request.description()))));
  }

  @PutMapping("/{shiftId}")
  @PreAuthorize(SHIFT_MANAGE_PERMISSION)
  @Operation(
      summary = "Update shift",
      description = "Updates shift information after validating unique name and time overlap.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<ShiftResponse> updateShift(
      @PathVariable @Positive(message = "Shift id must be greater than 0.") Long shiftId,
      @Valid @RequestBody UpdateShiftRequest request) {
    return ApiResponse.success(
        "Shift updated successfully.",
        shiftPresentationMapper.toResponse(
            updateShiftUseCase.updateShift(
                shiftId,
                new UpdateShiftCommand(
                    request.shiftName(),
                    request.startTime(),
                    request.endTime(),
                    request.description()))));
  }

  @DeleteMapping("/{shiftId}")
  @PreAuthorize(SHIFT_MANAGE_PERMISSION)
  @Operation(
      summary = "Delete shift",
      description = "Deletes a shift when there are no wedding bookings referencing it.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<Void> deleteShift(
      @PathVariable @Positive(message = "Shift id must be greater than 0.") Long shiftId) {
    deleteShiftUseCase.deleteShift(shiftId);
    return ApiResponse.success("Shift deleted successfully.");
  }
}
