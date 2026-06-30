package com.uit.weddingmanagement.modules.catalog.application.port.out;

import com.uit.weddingmanagement.modules.catalog.domain.model.MenuItem;
import java.util.List;
import java.util.Optional;

public interface MenuItemQueryPort {

  List<MenuItem> findMenuItems(Boolean available);

  Optional<MenuItem> findMenuItemById(Long menuItemId);

  boolean existsMenuItemByName(String itemName);

  boolean existsMenuItemByNameAndIdNot(String itemName, Long menuItemId);
}
