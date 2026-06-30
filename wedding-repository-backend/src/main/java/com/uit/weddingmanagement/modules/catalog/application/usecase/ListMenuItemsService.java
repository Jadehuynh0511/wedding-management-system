package com.uit.weddingmanagement.modules.catalog.application.usecase;

import com.uit.weddingmanagement.modules.catalog.application.model.result.MenuItemResult;
import com.uit.weddingmanagement.modules.catalog.application.port.in.ListMenuItemsUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.out.MenuItemQueryPort;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListMenuItemsService implements ListMenuItemsUseCase {

  private final MenuItemQueryPort menuItemQueryPort;

  public ListMenuItemsService(MenuItemQueryPort menuItemQueryPort) {
    this.menuItemQueryPort = menuItemQueryPort;
  }

  @Override
  public List<MenuItemResult> listMenuItems(Boolean available) {
    return menuItemQueryPort.findMenuItems(available).stream().map(MenuItemResult::from).toList();
  }
}
