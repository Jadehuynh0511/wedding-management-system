package com.uit.weddingmanagement.modules.catalog.presentation.controller;

import com.uit.weddingmanagement.common.api.ApiResponse;
import com.uit.weddingmanagement.modules.catalog.application.model.command.CreateHallCommand;
import com.uit.weddingmanagement.modules.catalog.application.model.command.UpdateHallCommand;
import com.uit.weddingmanagement.modules.catalog.application.port.in.CreateHallUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.in.DeleteHallUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.in.GetHallUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.in.ListHallsUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.in.UpdateHallUseCase;
import com.uit.weddingmanagement.modules.catalog.presentation.dto.request.CreateHallRequest;
import com.uit.weddingmanagement.modules.catalog.presentation.dto.request.UpdateHallRequest;
import com.uit.weddingmanagement.modules.catalog.presentation.dto.response.HallResponse;
import com.uit.weddingmanagement.modules.catalog.presentation.mapper.HallPresentationMapper;

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

// Controller này gom toàn bộ API CRUD cho sảnh để frontend BM1 chỉ cần làm việc với một resource
// `/api/halls`.
@Validated
@RestController
@RequestMapping("/api/halls")
@Tag(name = "Halls", description = "APIs for managing wedding halls.")
public class HallController {

  private static final String HALL_MANAGE_PERMISSION =
      "@authorizationService.hasPermission('HALL_MANAGE')";

  private final ListHallsUseCase listHallsUseCase;
  private final GetHallUseCase getHallUseCase;
  private final CreateHallUseCase createHallUseCase;
  private final UpdateHallUseCase updateHallUseCase;
  private final DeleteHallUseCase deleteHallUseCase;
  private final HallPresentationMapper hallPresentationMapper;

  public HallController(
      ListHallsUseCase listHallsUseCase,
      GetHallUseCase getHallUseCase,
      CreateHallUseCase createHallUseCase,
      UpdateHallUseCase updateHallUseCase,
      DeleteHallUseCase deleteHallUseCase,
      HallPresentationMapper hallPresentationMapper) {
    this.listHallsUseCase = listHallsUseCase;
    this.getHallUseCase = getHallUseCase;
    this.createHallUseCase = createHallUseCase;
    this.updateHallUseCase = updateHallUseCase;
    this.deleteHallUseCase = deleteHallUseCase;
    this.hallPresentationMapper = hallPresentationMapper;
  }

  @GetMapping
  @PreAuthorize(HALL_MANAGE_PERMISSION)
  @Operation(
      summary = "List halls",
      description = "Returns all configured halls for catalog management.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Halls loaded successfully."),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "Current user does not have HALL_MANAGE permission.")
  })
  public ApiResponse<List<HallResponse>> listHalls() {
    List<HallResponse> halls = listHallsUseCase.listHalls().stream()
        .map(hallPresentationMapper::toResponse)
        .toList();

    return ApiResponse.success("Halls loaded successfully.", halls);
  }

  @GetMapping("/{hallId}")
  @PreAuthorize(HALL_MANAGE_PERMISSION)
  @Operation(summary = "Get one hall", description = "Returns details for the requested hall.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<HallResponse> getHall(
      @PathVariable @Positive(message = "Hall id must be greater than 0.") Long hallId) {
    return ApiResponse.success(
        "Hall loaded successfully.", hallPresentationMapper.toResponse(getHallUseCase.getHall(hallId)));
  }

  @PostMapping
  @PreAuthorize(HALL_MANAGE_PERMISSION)
  @Operation(
      summary = "Create hall",
      description =
          "Creates a new hall after validating unique hall name and minimum table price by hall type.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<HallResponse> createHall(@Valid @RequestBody CreateHallRequest request) {
    return ApiResponse.success(
        "Hall created successfully.",
        hallPresentationMapper.toResponse(
            createHallUseCase.createHall(
                new CreateHallCommand(
                    request.hallTypeId(),
                    request.hallName(),
                    request.maxCapacity(),
                    request.tablePrice(),
                    request.status(),
                    request.description()))));
  }

  @PutMapping("/{hallId}")
  @PreAuthorize(HALL_MANAGE_PERMISSION)
  @Operation(
      summary = "Update hall",
      description =
          "Updates hall information after validating hall type pricing and hall name uniqueness.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<HallResponse> updateHall(
      @PathVariable @Positive(message = "Hall id must be greater than 0.") Long hallId,
      @Valid @RequestBody UpdateHallRequest request) {
    return ApiResponse.success(
        "Hall updated successfully.",
        hallPresentationMapper.toResponse(
            updateHallUseCase.updateHall(
                hallId,
                new UpdateHallCommand(
                    request.hallTypeId(),
                    request.hallName(),
                    request.maxCapacity(),
                    request.tablePrice(),
                    request.status(),
                    request.description()))));
  }

  @DeleteMapping("/{hallId}")
  @PreAuthorize(HALL_MANAGE_PERMISSION)
  @Operation(
      summary = "Delete hall",
      description = "Deletes a hall when there are no wedding bookings referencing it.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<Void> deleteHall(
      @PathVariable @Positive(message = "Hall id must be greater than 0.") Long hallId) {
    deleteHallUseCase.deleteHall(hallId);
    return ApiResponse.success("Hall deleted successfully.");
  }
}
