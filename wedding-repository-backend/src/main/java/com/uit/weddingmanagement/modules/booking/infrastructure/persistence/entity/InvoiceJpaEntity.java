package com.uit.weddingmanagement.modules.booking.infrastructure.persistence.entity;

import com.uit.weddingmanagement.common.entity.BaseEntity;
import com.uit.weddingmanagement.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "invoices")
public class InvoiceJpaEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "wedding_booking_id", nullable = false, updatable = false)
  private WeddingBookingJpaEntity weddingBooking;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", updatable = false)
  private UserJpaEntity user;

  @Column(name = "paid_at", nullable = false, updatable = false)
  private Instant paidAt;

  @Column(name = "hall_total_amount", nullable = false, precision = 18, scale = 2, updatable = false)
  private BigDecimal hallTotalAmount;

  @Column(
      name = "menu_items_total_amount",
      nullable = false,
      precision = 18,
      scale = 2,
      updatable = false)
  private BigDecimal menuItemsTotalAmount;

  @Column(name = "services_total_amount", nullable = false, precision = 18, scale = 2, updatable = false)
  private BigDecimal servicesTotalAmount;

  @Column(
      name = "incidentals_total_amount",
      nullable = false,
      precision = 18,
      scale = 2,
      updatable = false)
  private BigDecimal incidentalsTotalAmount;

  @Column(name = "deposit_amount", nullable = false, precision = 18, scale = 2, updatable = false)
  private BigDecimal depositAmount;

  @Column(
      name = "late_payment_penalty_amount",
      nullable = false,
      precision = 18,
      scale = 2,
      updatable = false)
  private BigDecimal latePaymentPenaltyAmount;

  @Column(name = "final_amount", nullable = false, precision = 18, scale = 2, updatable = false)
  private BigDecimal finalAmount;

  @Column(name = "notes", updatable = false)
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

  public UserJpaEntity getUser() {
    return user;
  }

  public void setUser(UserJpaEntity user) {
    this.user = user;
  }

  public Instant getPaidAt() {
    return paidAt;
  }

  public void setPaidAt(Instant paidAt) {
    this.paidAt = paidAt;
  }

  public BigDecimal getHallTotalAmount() {
    return hallTotalAmount;
  }

  public void setHallTotalAmount(BigDecimal hallTotalAmount) {
    this.hallTotalAmount = hallTotalAmount;
  }

  public BigDecimal getMenuItemsTotalAmount() {
    return menuItemsTotalAmount;
  }

  public void setMenuItemsTotalAmount(BigDecimal menuItemsTotalAmount) {
    this.menuItemsTotalAmount = menuItemsTotalAmount;
  }

  public BigDecimal getServicesTotalAmount() {
    return servicesTotalAmount;
  }

  public void setServicesTotalAmount(BigDecimal servicesTotalAmount) {
    this.servicesTotalAmount = servicesTotalAmount;
  }

  public BigDecimal getIncidentalsTotalAmount() {
    return incidentalsTotalAmount;
  }

  public void setIncidentalsTotalAmount(BigDecimal incidentalsTotalAmount) {
    this.incidentalsTotalAmount = incidentalsTotalAmount;
  }

  public BigDecimal getDepositAmount() {
    return depositAmount;
  }

  public void setDepositAmount(BigDecimal depositAmount) {
    this.depositAmount = depositAmount;
  }

  public BigDecimal getLatePaymentPenaltyAmount() {
    return latePaymentPenaltyAmount;
  }

  public void setLatePaymentPenaltyAmount(BigDecimal latePaymentPenaltyAmount) {
    this.latePaymentPenaltyAmount = latePaymentPenaltyAmount;
  }

  public BigDecimal getFinalAmount() {
    return finalAmount;
  }

  public void setFinalAmount(BigDecimal finalAmount) {
    this.finalAmount = finalAmount;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }
}
