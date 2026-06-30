package com.uit.weddingmanagement.modules.catalog.application.port.in;

import com.uit.weddingmanagement.modules.catalog.application.model.command.CreateMenuItemCommand;
import com.uit.weddingmanagement.modules.catalog.application.model.result.MenuItemResult;

public interface CreateMenuItemUseCase {

  MenuItemResult createMenuItem(CreateMenuItemCommand command);
}
