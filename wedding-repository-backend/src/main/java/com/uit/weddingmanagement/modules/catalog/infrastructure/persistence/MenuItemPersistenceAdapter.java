package com.uit.weddingmanagement.modules.catalog.infrastructure.persistence;

import com.uit.weddingmanagement.modules.catalog.application.port.out.MenuItemBookingReferenceQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.MenuItemCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.MenuItemQueryPort;
import com.uit.weddingmanagement.modules.booking.infrastructure.persistence.repository.BookingMenuItemJpaRepository;
import com.uit.weddingmanagement.modules.catalog.domain.model.MenuItem;
import com.uit.weddingmanagement.modules.catalog.domain.model.MenuItemStatus;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity.MenuItemJpaEntity;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.repository.MenuItemJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class MenuItemPersistenceAdapter
    implements MenuItemQueryPort, MenuItemCommandPort, MenuItemBookingReferenceQueryPort {

  private final MenuItemJpaRepository menuItemJpaRepository;
  private final BookingMenuItemJpaRepository bookingMenuItemJpaRepository;

  public MenuItemPersistenceAdapter(
      MenuItemJpaRepository menuItemJpaRepository,
      BookingMenuItemJpaRepository bookingMenuItemJpaRepository) {
    this.menuItemJpaRepository = menuItemJpaRepository;
    this.bookingMenuItemJpaRepository = bookingMenuItemJpaRepository;
  }

  @Override
  public List<MenuItem> findMenuItems(Boolean available) {
    MenuItemStatus status = mapAvailabilityToStatus(available);

    return menuItemJpaRepository.findAllByOptionalStatus(status).stream().map(this::toDomain).toList();
  }

  @Override
  public Optional<MenuItem> findMenuItemById(Long menuItemId) {
    return menuItemJpaRepository.findById(menuItemId).map(this::toDomain);
  }

  @Override
  public boolean existsMenuItemByName(String itemName) {
    return menuItemJpaRepository.existsByItemNameIgnoreCase(itemName);
  }

  @Override
  public boolean existsMenuItemByNameAndIdNot(String itemName, Long menuItemId) {
    return menuItemJpaRepository.existsByItemNameIgnoreCaseAndIdNot(itemName, menuItemId);
  }

  @Override
  public MenuItem saveMenuItem(MenuItem menuItem) {
    MenuItemJpaEntity menuItemJpaEntity = resolveEntityForSave(menuItem);
    menuItemJpaEntity.setItemName(menuItem.itemName());
    menuItemJpaEntity.setItemCategory(menuItem.itemCategory());
    menuItemJpaEntity.setCurrentPrice(menuItem.currentPrice());
    menuItemJpaEntity.setStatus(menuItem.status());
    menuItemJpaEntity.setDescription(menuItem.description());

    return toDomain(menuItemJpaRepository.save(menuItemJpaEntity));
  }

  @Override
  public void deleteMenuItemById(Long menuItemId) {
    menuItemJpaRepository.deleteById(menuItemId);
  }

  @Override
  public boolean existsBookingMenuItemByMenuItemId(Long menuItemId) {
    return bookingMenuItemJpaRepository.existsByMenuItem_Id(menuItemId);
  }

  private MenuItemStatus mapAvailabilityToStatus(Boolean available) {
    if (available == null) {
      return null;
    }

    return available ? MenuItemStatus.CON : MenuItemStatus.HET;
  }

  private MenuItemJpaEntity resolveEntityForSave(MenuItem menuItem) {
    if (menuItem.id() == null) {
      return new MenuItemJpaEntity();
    }

    return menuItemJpaRepository
        .findById(menuItem.id())
        .orElseThrow(
            () -> new EntityNotFoundException("Menu item not found with id: " + menuItem.id()));
  }

  private MenuItem toDomain(MenuItemJpaEntity menuItemJpaEntity) {
    return new MenuItem(
        menuItemJpaEntity.getId(),
        menuItemJpaEntity.getItemName(),
        menuItemJpaEntity.getItemCategory(),
        menuItemJpaEntity.getCurrentPrice(),
        menuItemJpaEntity.getStatus(),
        menuItemJpaEntity.getDescription());
  }
}
