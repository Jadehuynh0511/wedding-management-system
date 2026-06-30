package com.uit.weddingmanagement.modules.catalog.presentation.controller;

import com.uit.weddingmanagement.common.api.ApiResponse;
import com.uit.weddingmanagement.modules.catalog.application.model.command.CreateServiceItemCommand;
import com.uit.weddingmanagement.modules.catalog.application.model.command.UpdateServiceItemCommand;
import com.uit.weddingmanagement.modules.catalog.application.model.command.UpdateServiceItemPriceCommand;
import com.uit.weddingmanagement.modules.catalog.application.port.in.CreateServiceItemUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.in.DeleteServiceItemUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.in.GetServiceItemUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.in.ListServiceItemsUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.in.UpdateServiceItemPriceUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.in.UpdateServiceItemUseCase;
import com.uit.weddingmanagement.modules.catalog.presentation.dto.request.CreateServiceItemRequest;
import com.uit.weddingmanagement.modules.catalog.presentation.dto.request.UpdateServiceItemPriceRequest;
import com.uit.weddingmanagement.modules.catalog.presentation.dto.request.UpdateServiceItemRequest;
import com.uit.weddingmanagement.modules.catalog.presentation.dto.response.ServiceItemDetailResponse;
import com.uit.weddingmanagement.modules.catalog.presentation.dto.response.ServiceItemResponse;
import com.uit.weddingmanagement.modules.catalog.presentation.mapper.ServiceItemPresentationMapper;

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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/services")
@Tag(name = "Services", description = "APIs for managing wedding service catalog.")
public class ServiceItemController {

  private static final String SERVICE_MANAGE_PERMISSION =
      "@authorizationService.hasPermission('SERVICE_MANAGE')";

  private final ListServiceItemsUseCase listServiceItemsUseCase;
  private final GetServiceItemUseCase getServiceItemUseCase;
  private final CreateServiceItemUseCase createServiceItemUseCase;
  private final UpdateServiceItemUseCase updateServiceItemUseCase;
  private final UpdateServiceItemPriceUseCase updateServiceItemPriceUseCase;
  private final DeleteServiceItemUseCase deleteServiceItemUseCase;
  private final ServiceItemPresentationMapper serviceItemPresentationMapper;

  public ServiceItemController(
      ListServiceItemsUseCase listServiceItemsUseCase,
      GetServiceItemUseCase getServiceItemUseCase,
      CreateServiceItemUseCase createServiceItemUseCase,
      UpdateServiceItemUseCase updateServiceItemUseCase,
      UpdateServiceItemPriceUseCase updateServiceItemPriceUseCase,
      DeleteServiceItemUseCase deleteServiceItemUseCase,
      ServiceItemPresentationMapper serviceItemPresentationMapper) {
    this.listServiceItemsUseCase = listServiceItemsUseCase;
    this.getServiceItemUseCase = getServiceItemUseCase;
    this.createServiceItemUseCase = createServiceItemUseCase;
    this.updateServiceItemUseCase = updateServiceItemUseCase;
    this.updateServiceItemPriceUseCase = updateServiceItemPriceUseCase;
    this.deleteServiceItemUseCase = deleteServiceItemUseCase;
    this.serviceItemPresentationMapper = serviceItemPresentationMapper;
  }

  @GetMapping
  @PreAuthorize(SERVICE_MANAGE_PERMISSION)
  @Operation(
      summary = "List services",
      description =
          "Returns all services, or filters by active status when the active query parameter is provided.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Services loaded successfully."),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "Current user does not have SERVICE_MANAGE permission.")
  })
  public ApiResponse<List<ServiceItemResponse>> listServiceItems(
      @RequestParam(required = false) Boolean active) {
    List<ServiceItemResponse> services =
        listServiceItemsUseCase.listServiceItems(active).stream()
            .map(serviceItemPresentationMapper::toResponse)
            .toList();

    return ApiResponse.success("Services loaded successfully.", services);
  }

  @GetMapping("/{serviceItemId}")
  @PreAuthorize(SERVICE_MANAGE_PERMISSION)
  @Operation(
      summary = "Get one service",
      description = "Returns details for the requested service, including price history.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<ServiceItemDetailResponse> getServiceItem(
      @PathVariable @Positive(message = "Service item id must be greater than 0.")
          Long serviceItemId) {
    return ApiResponse.success(
        "Service loaded successfully.",
        serviceItemPresentationMapper.toDetailResponse(getServiceItemUseCase.getServiceItem(serviceItemId)));
  }

  @PostMapping
  @PreAuthorize(SERVICE_MANAGE_PERMISSION)
  @Operation(
      summary = "Create service",
      description = "Creates a new service after validating unique name and current price.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<ServiceItemResponse> createServiceItem(
      @Valid @RequestBody CreateServiceItemRequest request) {
    return ApiResponse.success(
        "Service created successfully.",
        serviceItemPresentationMapper.toResponse(
            createServiceItemUseCase.createServiceItem(
                new CreateServiceItemCommand(
                    request.serviceName(),
                    request.serviceCategory(),
                    request.unitName(),
                    request.currentPrice(),
                    request.status(),
                    request.description()))));
  }

  @PutMapping("/{serviceItemId}")
  @PreAuthorize(SERVICE_MANAGE_PERMISSION)
  @Operation(
      summary = "Update service metadata",
      description =
          "Updates service information except price. Price changes must use the dedicated PATCH endpoint.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<ServiceItemResponse> updateServiceItem(
      @PathVariable @Positive(message = "Service item id must be greater than 0.")
          Long serviceItemId,
      @Valid @RequestBody UpdateServiceItemRequest request) {
    return ApiResponse.success(
        "Service updated successfully.",
        serviceItemPresentationMapper.toResponse(
            updateServiceItemUseCase.updateServiceItem(
                serviceItemId,
                new UpdateServiceItemCommand(
                    request.serviceName(),
                    request.serviceCategory(),
                    request.unitName(),
                    request.status(),
                    request.description()))));
  }

  @PatchMapping("/{serviceItemId}/price")
  @PreAuthorize(SERVICE_MANAGE_PERMISSION)
  @Operation(
      summary = "Update service price",
      description =
          "Closes the current price period, inserts a history row, and applies the new price from now.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<ServiceItemDetailResponse> updateServiceItemPrice(
      @PathVariable @Positive(message = "Service item id must be greater than 0.")
          Long serviceItemId,
      @Valid @RequestBody UpdateServiceItemPriceRequest request) {
    return ApiResponse.success(
        "Service price updated successfully.",
        serviceItemPresentationMapper.toDetailResponse(
            updateServiceItemPriceUseCase.updateServiceItemPrice(
                serviceItemId, new UpdateServiceItemPriceCommand(request.newPrice()))));
  }

  @DeleteMapping("/{serviceItemId}")
  @PreAuthorize(SERVICE_MANAGE_PERMISSION)
  @Operation(
      summary = "Delete service",
      description =
          "Deletes a service when there are no wedding booking snapshots or incidental receipts referencing it.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<Void> deleteServiceItem(
      @PathVariable @Positive(message = "Service item id must be greater than 0.")
          Long serviceItemId) {
    deleteServiceItemUseCase.deleteServiceItem(serviceItemId);
    return ApiResponse.success("Service deleted successfully.");
  }
}
