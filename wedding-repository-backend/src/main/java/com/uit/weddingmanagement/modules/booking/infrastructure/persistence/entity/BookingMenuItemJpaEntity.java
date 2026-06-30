package com.uit.weddingmanagement.modules.booking.infrastructure.persistence.entity;

import com.uit.weddingmanagement.common.entity.BaseEntity;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity.MenuItemJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "booking_menu_items")
public class BookingMenuItemJpaEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "wedding_booking_id", nullable = false)
  private WeddingBookingJpaEntity weddingBooking;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "menu_item_id", nullable = false)
  private MenuItemJpaEntity menuItem;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "price_snapshot", nullable = false, precision = 18, scale = 2)
  private BigDecimal priceSnapshot;

  @Column(name = "line_total", nullable = false, precision = 18, scale = 2)
  private BigDecimal lineTotal;

  @Column(name = "notes")
  private String notes;

  // Getters and setters
  public Long getId() {
    return id;
  }

  public WeddingBookingJpaEntity getWeddingBooking() {
    return weddingBooking;
  }

  public void setWeddingBooking(WeddingBookingJpaEntity weddingBooking) {
    this.weddingBooking = weddingBooking;
  }

  public MenuItemJpaEntity getMenuItem() {
    return menuItem;
  }

  public void setMenuItem(MenuItemJpaEntity menuItem) {
    this.menuItem = menuItem;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public BigDecimal getPriceSnapshot() {
    return priceSnapshot;
  }

  public void setPriceSnapshot(BigDecimal priceSnapshot) {
    this.priceSnapshot = priceSnapshot;
  }

  public BigDecimal getLineTotal() {
    return lineTotal;
  }

  public void setLineTotal(BigDecimal lineTotal) {
    this.lineTotal = lineTotal;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }
}
