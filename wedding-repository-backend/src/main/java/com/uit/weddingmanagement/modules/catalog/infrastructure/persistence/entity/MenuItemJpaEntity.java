package com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity;

import com.uit.weddingmanagement.common.entity.BaseEntity;
import com.uit.weddingmanagement.modules.catalog.domain.model.MenuItemStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "menu_items")
public class MenuItemJpaEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "item_name", nullable = false, unique = true, length = 150)
  private String itemName;

  @Column(name = "item_category", nullable = false, length = 100)
  private String itemCategory;

  @Column(name = "current_price", nullable = false, precision = 18, scale = 2)
  private BigDecimal currentPrice;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 10)
  private MenuItemStatus status;

  @Column(name = "description")
  private String description;

  public Long getId() {
    return id;
  }

  public String getItemName() {
    return itemName;
  }

  public void setItemName(String itemName) {
    this.itemName = itemName;
  }

  public String getItemCategory() {
    return itemCategory;
  }

  public void setItemCategory(String itemCategory) {
    this.itemCategory = itemCategory;
  }

  public BigDecimal getCurrentPrice() {
    return currentPrice;
  }

  public void setCurrentPrice(BigDecimal currentPrice) {
    this.currentPrice = currentPrice;
  }

  public MenuItemStatus getStatus() {
    return status;
  }

  public void setStatus(MenuItemStatus status) {
    this.status = status;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
