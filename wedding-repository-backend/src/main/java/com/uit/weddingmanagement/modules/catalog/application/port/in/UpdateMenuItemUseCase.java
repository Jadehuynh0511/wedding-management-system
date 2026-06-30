package com.uit.weddingmanagement.modules.catalog.application.port.in;

import com.uit.weddingmanagement.modules.catalog.application.model.result.MenuItemResult;
import com.uit.weddingmanagement.modules.catalog.application.model.command.UpdateMenuItemCommand;

public interface UpdateMenuItemUseCase {

  MenuItemResult updateMenuItem(Long menuItemId, UpdateMenuItemCommand command);
}
