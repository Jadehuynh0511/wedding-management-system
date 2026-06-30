package com.uit.weddingmanagement.modules.catalog.application.usecase;

import com.uit.weddingmanagement.common.exception.DuplicateResourceException;
import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.catalog.application.model.command.CreateMenuItemCommand;
import com.uit.weddingmanagement.modules.catalog.application.model.result.MenuItemResult;
import com.uit.weddingmanagement.modules.catalog.application.port.in.CreateMenuItemUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.out.MenuItemCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.MenuItemQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.MenuItem;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateMenuItemService implements CreateMenuItemUseCase {

  private final MenuItemQueryPort menuItemQueryPort;
  private final MenuItemCommandPort menuItemCommandPort;

  public CreateMenuItemService(
      MenuItemQueryPort menuItemQueryPort, MenuItemCommandPort menuItemCommandPort) {
    this.menuItemQueryPort = menuItemQueryPort;
    this.menuItemCommandPort = menuItemCommandPort;
  }

  @Override
  @AuditAction(
      action = "MENU_ITEM_CREATE",
      module = "CATALOG",
      targetType = "MENU_ITEM",
      targetIdExpression = "#result.id",
      targetLabelExpression = "#result.itemName",
      successDescriptionExpression = "'Created menu item ' + #result.itemName",
      failureDescriptionExpression = "'Failed to create menu item ' + #command.itemName",
      detailsExpression = "#command")
  public MenuItemResult createMenuItem(CreateMenuItemCommand command) {
    MenuItem menuItem =
        MenuItem.create(
            command.itemName(),
            command.itemCategory(),
            command.currentPrice(),
            command.status(),
            command.description());

    ensureUniqueMenuItemName(menuItem.itemName());

    try {
      return MenuItemResult.from(menuItemCommandPort.saveMenuItem(menuItem));
    } catch (DataIntegrityViolationException exception) {
      throw new DuplicateResourceException("Menu item name already exists: " + menuItem.itemName());
    }
  }

  private void ensureUniqueMenuItemName(String itemName) {
    if (menuItemQueryPort.existsMenuItemByName(itemName)) {
      throw new DuplicateResourceException("Menu item name already exists: " + itemName);
    }
  }
}
