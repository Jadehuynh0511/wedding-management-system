package com.uit.weddingmanagement.modules.catalog.presentation.controller;

import com.uit.weddingmanagement.common.api.ApiResponse;
import com.uit.weddingmanagement.modules.catalog.application.model.command.CreateMenuItemCommand;
import com.uit.weddingmanagement.modules.catalog.application.model.command.UpdateMenuItemCommand;
import com.uit.weddingmanagement.modules.catalog.application.port.in.CreateMenuItemUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.in.DeleteMenuItemUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.in.GetMenuItemUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.in.ListMenuItemsUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.in.UpdateMenuItemUseCase;
import com.uit.weddingmanagement.modules.catalog.presentation.dto.request.CreateMenuItemRequest;
import com.uit.weddingmanagement.modules.catalog.presentation.dto.request.UpdateMenuItemRequest;
import com.uit.weddingmanagement.modules.catalog.presentation.dto.response.MenuItemResponse;
import com.uit.weddingmanagement.modules.catalog.presentation.mapper.MenuItemPresentationMapper;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// Controller này gom toàn bộ API CRUD cho danh mục món ăn
@Validated
@RestController
@RequestMapping("/api/menu-items")
@Tag(name = "Menu Items", description = "APIs for managing wedding menu items.")
public class MenuItemController {

  private static final String MENU_ITEM_MANAGE_PERMISSION =
      "@authorizationService.hasPermission('MENU_ITEM_MANAGE')";

  private final ListMenuItemsUseCase listMenuItemsUseCase;
  private final GetMenuItemUseCase getMenuItemUseCase;
  private final CreateMenuItemUseCase createMenuItemUseCase;
  private final UpdateMenuItemUseCase updateMenuItemUseCase;
  private final DeleteMenuItemUseCase deleteMenuItemUseCase;
  private final MenuItemPresentationMapper menuItemPresentationMapper;

  public MenuItemController(
      ListMenuItemsUseCase listMenuItemsUseCase,
      GetMenuItemUseCase getMenuItemUseCase,
      CreateMenuItemUseCase createMenuItemUseCase,
      UpdateMenuItemUseCase updateMenuItemUseCase,
      DeleteMenuItemUseCase deleteMenuItemUseCase,
      MenuItemPresentationMapper menuItemPresentationMapper) {
    this.listMenuItemsUseCase = listMenuItemsUseCase;
    this.getMenuItemUseCase = getMenuItemUseCase;
    this.createMenuItemUseCase = createMenuItemUseCase;
    this.updateMenuItemUseCase = updateMenuItemUseCase;
    this.deleteMenuItemUseCase = deleteMenuItemUseCase;
    this.menuItemPresentationMapper = menuItemPresentationMapper;
  }

  @GetMapping
  @PreAuthorize(MENU_ITEM_MANAGE_PERMISSION)
  @Operation(
      summary = "List menu items",
      description = "Returns all menu items, or filters by available status when the available query parameter is provided.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Menu items loaded successfully."),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "Current user does not have MENU_ITEM_MANAGE permission.")
  })
  public ApiResponse<List<MenuItemResponse>> listMenuItems(
      @RequestParam(required = false) Boolean available) {
    List<MenuItemResponse> menuItems =
        listMenuItemsUseCase.listMenuItems(available).stream()
            .map(menuItemPresentationMapper::toResponse)
            .toList();

    return ApiResponse.success("Menu items loaded successfully.", menuItems);
  }

  @GetMapping("/{menuItemId}")
  @PreAuthorize(MENU_ITEM_MANAGE_PERMISSION)
  @Operation(summary = "Get one menu item", description = "Returns details for the requested menu item.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<MenuItemResponse> getMenuItem(
      @PathVariable @Positive(message = "Menu item id must be greater than 0.") Long menuItemId) {
    return ApiResponse.success(
        "Menu item loaded successfully.",
        menuItemPresentationMapper.toResponse(getMenuItemUseCase.getMenuItem(menuItemId)));
  }

  @PostMapping
  @PreAuthorize(MENU_ITEM_MANAGE_PERMISSION)
  @Operation(
      summary = "Create menu item",
      description = "Creates a new menu item after validating unique name and current price.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<MenuItemResponse> createMenuItem(@Valid @RequestBody CreateMenuItemRequest request) {
    return ApiResponse.success(
        "Menu item created successfully.",
        menuItemPresentationMapper.toResponse(
            createMenuItemUseCase.createMenuItem(
                new CreateMenuItemCommand(
                    request.itemName(),
                    request.itemCategory(),
                    request.currentPrice(),
                    request.status(),
                    request.description()))));
  }

  @PutMapping("/{menuItemId}")
  @PreAuthorize(MENU_ITEM_MANAGE_PERMISSION)
  @Operation(
      summary = "Update menu item",
      description = "Updates menu item information after validating unique name.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<MenuItemResponse> updateMenuItem(
      @PathVariable @Positive(message = "Menu item id must be greater than 0.") Long menuItemId,
      @Valid @RequestBody UpdateMenuItemRequest request) {
    return ApiResponse.success(
        "Menu item updated successfully.",
        menuItemPresentationMapper.toResponse(
            updateMenuItemUseCase.updateMenuItem(
                menuItemId,
                new UpdateMenuItemCommand(
                    request.itemName(),
                    request.itemCategory(),
                    request.currentPrice(),
                    request.status(),
                    request.description()))));
  }

  @DeleteMapping("/{menuItemId}")
  @PreAuthorize(MENU_ITEM_MANAGE_PERMISSION)
  @Operation(
      summary = "Delete menu item",
      description = "Deletes a menu item when there are no wedding bookings referencing it.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<Void> deleteMenuItem(
      @PathVariable @Positive(message = "Menu item id must be greater than 0.") Long menuItemId) {
    deleteMenuItemUseCase.deleteMenuItem(menuItemId);
    return ApiResponse.success("Menu item deleted successfully.");
  }
}
