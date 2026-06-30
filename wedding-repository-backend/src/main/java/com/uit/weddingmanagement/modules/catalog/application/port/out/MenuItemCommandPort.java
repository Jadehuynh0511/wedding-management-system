package com.uit.weddingmanagement.modules.catalog.application.port.out;

import com.uit.weddingmanagement.modules.catalog.domain.model.MenuItem;

public interface MenuItemCommandPort {

  MenuItem saveMenuItem(MenuItem menuItem);

  void deleteMenuItemById(Long menuItemId);
}
