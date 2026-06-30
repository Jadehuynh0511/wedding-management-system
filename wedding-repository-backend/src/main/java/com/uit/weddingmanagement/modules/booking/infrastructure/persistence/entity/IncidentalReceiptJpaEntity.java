package com.uit.weddingmanagement.modules.booking.infrastructure.persistence.entity;

import com.uit.weddingmanagement.common.entity.BaseEntity;
import com.uit.weddingmanagement.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity.IncidentalReceiptItemJpaEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "incidental_receipts")
public class IncidentalReceiptJpaEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "wedding_booking_id", nullable = false, updatable = false)
  private WeddingBookingJpaEntity weddingBooking;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", updatable = false)
  private UserJpaEntity user;

  @Column(name = "recorded_at", nullable = false, updatable = false)
  private Instant recordedAt;

  @Column(name = "total_amount", nullable = false, precision = 18, scale = 2, updatable = false)
  private BigDecimal totalAmount;

  @Column(name = "notes", updatable = false)
  private String notes;

  @OneToMany(mappedBy = "incidentalReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<IncidentalReceiptItemJpaEntity> items = new ArrayList<>();

  public Long getId() {
    return id;
  }

  public WeddingBookingJpaEntity getWeddingBooking() {
    return weddingBooking;
  }

  public void setWeddingBooking(WeddingBookingJpaEntity weddingBooking) {
    this.weddingBooking = weddingBooking;
  }

  public UserJpaEntity getUser() {
    return user;
  }

  public void setUser(UserJpaEntity user) {
    this.user = user;
  }

  public Instant getRecordedAt() {
    return recordedAt;
  }

  public void setRecordedAt(Instant recordedAt) {
    this.recordedAt = recordedAt;
  }

  public BigDecimal getTotalAmount() {
    return totalAmount;
  }

  public void setTotalAmount(BigDecimal totalAmount) {
    this.totalAmount = totalAmount;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public List<IncidentalReceiptItemJpaEntity> getItems() {
    return items;
  }

  public void setItems(List<IncidentalReceiptItemJpaEntity> items) {
    this.items = items;
  }
}
