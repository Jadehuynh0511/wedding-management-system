package com.uit.weddingmanagement.modules.catalog.application.port.in;

import com.uit.weddingmanagement.modules.catalog.application.model.result.MenuItemResult;
import java.util.List;

public interface ListMenuItemsUseCase {

  List<MenuItemResult> listMenuItems(Boolean available);
}
