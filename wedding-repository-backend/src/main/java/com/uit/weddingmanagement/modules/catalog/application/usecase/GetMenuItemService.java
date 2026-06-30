package com.uit.weddingmanagement.modules.catalog.application.usecase;

import com.uit.weddingmanagement.modules.catalog.application.model.result.MenuItemResult;
import com.uit.weddingmanagement.modules.catalog.application.port.in.GetMenuItemUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.out.MenuItemQueryPort;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetMenuItemService implements GetMenuItemUseCase {

  private final MenuItemQueryPort menuItemQueryPort;

  public GetMenuItemService(MenuItemQueryPort menuItemQueryPort) {
    this.menuItemQueryPort = menuItemQueryPort;
  }

  @Override
  public MenuItemResult getMenuItem(Long menuItemId) {
    Long normalizedMenuItemId = requireMenuItemId(menuItemId);

    return menuItemQueryPort
        .findMenuItemById(normalizedMenuItemId)
        .map(MenuItemResult::from)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "Menu item not found with id: " + normalizedMenuItemId));
  }

  private Long requireMenuItemId(Long menuItemId) {
    if (menuItemId == null) {
      throw new IllegalArgumentException("Menu item id is required.");
    }

    return menuItemId;
  }
}
