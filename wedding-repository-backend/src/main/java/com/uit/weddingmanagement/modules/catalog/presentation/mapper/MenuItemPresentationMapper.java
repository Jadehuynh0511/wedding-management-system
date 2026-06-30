package com.uit.weddingmanagement.modules.catalog.presentation.mapper;

import com.uit.weddingmanagement.modules.catalog.application.model.result.MenuItemResult;
import com.uit.weddingmanagement.modules.catalog.presentation.dto.response.MenuItemResponse;

import org.springframework.stereotype.Component;

/**
 * Mapper chuyển đổi MenuItemResult sang MenuItemResponse cho presentation layer.
 */
@Component
public class MenuItemPresentationMapper {

    public MenuItemResponse toResponse(MenuItemResult menuItemResult) {
        return new MenuItemResponse(
                menuItemResult.id(),
                menuItemResult.itemName(),
                menuItemResult.itemCategory(),
                menuItemResult.currentPrice(),
                menuItemResult.status(),
                menuItemResult.available(),
                menuItemResult.description());
    }
}
