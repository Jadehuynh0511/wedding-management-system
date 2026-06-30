package com.uit.weddingmanagement.modules.catalog.application.usecase;

import com.uit.weddingmanagement.common.exception.DuplicateResourceException;
import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.catalog.application.model.result.MenuItemResult;
import com.uit.weddingmanagement.modules.catalog.application.model.command.UpdateMenuItemCommand;
import com.uit.weddingmanagement.modules.catalog.application.port.in.UpdateMenuItemUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.out.MenuItemCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.MenuItemQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.MenuItem;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateMenuItemService implements UpdateMenuItemUseCase {

  private final MenuItemQueryPort menuItemQueryPort;
  private final MenuItemCommandPort menuItemCommandPort;

  public UpdateMenuItemService(
      MenuItemQueryPort menuItemQueryPort, MenuItemCommandPort menuItemCommandPort) {
    this.menuItemQueryPort = menuItemQueryPort;
    this.menuItemCommandPort = menuItemCommandPort;
  }

  @Override
  @AuditAction(
      action = "MENU_ITEM_UPDATE",
      module = "CATALOG",
      targetType = "MENU_ITEM",
      targetIdExpression = "#menuItemId",
      targetLabelExpression = "#command.itemName",
      successDescriptionExpression = "'Updated menu item ' + #menuItemId",
      failureDescriptionExpression = "'Failed to update menu item ' + #menuItemId",
      detailsExpression = "#command")
  public MenuItemResult updateMenuItem(Long menuItemId, UpdateMenuItemCommand command) {
    Long normalizedMenuItemId = requireMenuItemId(menuItemId);

    MenuItem existingMenuItem =
        menuItemQueryPort
            .findMenuItemById(normalizedMenuItemId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Menu item not found with id: " + normalizedMenuItemId));

    MenuItem updatedMenuItem =
        existingMenuItem.update(
            command.itemName(),
            command.itemCategory(),
            command.currentPrice(),
            command.status(),
            command.description());

    ensureUniqueMenuItemName(updatedMenuItem.itemName(), normalizedMenuItemId);

    try {
      return MenuItemResult.from(menuItemCommandPort.saveMenuItem(updatedMenuItem));
    } catch (DataIntegrityViolationException exception) {
      throw new DuplicateResourceException(
          "Menu item name already exists: " + updatedMenuItem.itemName());
    }
  }

  private Long requireMenuItemId(Long menuItemId) {
    if (menuItemId == null) {
      throw new IllegalArgumentException("Menu item id is required.");
    }

    return menuItemId;
  }

  private void ensureUniqueMenuItemName(String itemName, Long menuItemId) {
    if (menuItemQueryPort.existsMenuItemByNameAndIdNot(itemName, menuItemId)) {
      throw new DuplicateResourceException("Menu item name already exists: " + itemName);
    }
  }
}
