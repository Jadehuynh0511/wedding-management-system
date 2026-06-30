package com.uit.weddingmanagement.modules.catalog.application.port.in;

import com.uit.weddingmanagement.modules.catalog.application.model.result.MenuItemResult;

public interface GetMenuItemUseCase {

  MenuItemResult getMenuItem(Long menuItemId);
}
