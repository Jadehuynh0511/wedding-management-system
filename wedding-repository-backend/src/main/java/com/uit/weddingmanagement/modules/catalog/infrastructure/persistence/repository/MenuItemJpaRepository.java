package com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.repository;

import com.uit.weddingmanagement.modules.catalog.domain.model.MenuItemStatus;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity.MenuItemJpaEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MenuItemJpaRepository extends JpaRepository<MenuItemJpaEntity, Long> {

  @Query(
      """
      select menuItem
      from MenuItemJpaEntity menuItem
      where (:status is null or menuItem.status = :status)
      order by menuItem.id asc
      """)
  List<MenuItemJpaEntity> findAllByOptionalStatus(@Param("status") MenuItemStatus status);

  boolean existsByItemNameIgnoreCase(String itemName);

  boolean existsByItemNameIgnoreCaseAndIdNot(String itemName, Long menuItemId);
}
