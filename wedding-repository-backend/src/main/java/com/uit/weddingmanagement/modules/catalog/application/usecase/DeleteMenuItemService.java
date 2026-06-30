package com.uit.weddingmanagement.modules.catalog.application.usecase;

import com.uit.weddingmanagement.common.exception.ResourceInUseException;
import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.catalog.application.port.in.DeleteMenuItemUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.out.MenuItemBookingReferenceQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.MenuItemCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.MenuItemQueryPort;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteMenuItemService implements DeleteMenuItemUseCase {

  private final MenuItemQueryPort menuItemQueryPort;
  private final MenuItemCommandPort menuItemCommandPort;
  private final MenuItemBookingReferenceQueryPort menuItemBookingReferenceQueryPort;

  public DeleteMenuItemService(
      MenuItemQueryPort menuItemQueryPort,
      MenuItemCommandPort menuItemCommandPort,
      MenuItemBookingReferenceQueryPort menuItemBookingReferenceQueryPort) {
    this.menuItemQueryPort = menuItemQueryPort;
    this.menuItemCommandPort = menuItemCommandPort;
    this.menuItemBookingReferenceQueryPort = menuItemBookingReferenceQueryPort;
  }

  @Override
  @AuditAction(
      action = "MENU_ITEM_DELETE",
      module = "CATALOG",
      targetType = "MENU_ITEM",
      targetIdExpression = "#menuItemId",
      successDescriptionExpression = "'Deleted menu item ' + #menuItemId",
      failureDescriptionExpression = "'Failed to delete menu item ' + #menuItemId")
  public void deleteMenuItem(Long menuItemId) {
    Long normalizedMenuItemId = requireMenuItemId(menuItemId);

    menuItemQueryPort
        .findMenuItemById(normalizedMenuItemId)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "Menu item not found with id: " + normalizedMenuItemId));

    ensureMenuItemIsNotUsedByBookings(normalizedMenuItemId);

    try {
      menuItemCommandPort.deleteMenuItemById(normalizedMenuItemId);
    } catch (DataIntegrityViolationException exception) {
      throw new ResourceInUseException(
          "Menu item is currently used by existing wedding bookings and cannot be deleted.");
    }
  }

  private Long requireMenuItemId(Long menuItemId) {
    if (menuItemId == null) {
      throw new IllegalArgumentException("Menu item id is required.");
    }

    return menuItemId;
  }

  private void ensureMenuItemIsNotUsedByBookings(Long menuItemId) {
    // Chủ động trả 409 ở tầng nghiệp vụ để tránh frontend phải đọc lỗi FK khó hiểu từ DB.
    if (menuItemBookingReferenceQueryPort.existsBookingMenuItemByMenuItemId(menuItemId)) {
      throw new ResourceInUseException(
          "Menu item is currently used by existing wedding bookings and cannot be deleted.");
    }
  }
}
