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
@Table(name = "cancellation_receipts")
public class CancellationReceiptJpaEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "wedding_booking_id", nullable = false, updatable = false)
  private WeddingBookingJpaEntity weddingBooking;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", updatable = false)
  private UserJpaEntity user;

  @Column(name = "cancelled_at", nullable = false, updatable = false)
  private Instant cancelledAt;

  @Column(name = "days_before_celebration", nullable = false, updatable = false)
  private Integer daysBeforeCelebration;

  @Column(
      name = "applied_deposit_refund_percentage",
      nullable = false,
      precision = 5,
      scale = 2,
      updatable = false)
  private BigDecimal appliedDepositRefundPercentage;

  @Column(name = "refund_amount", nullable = false, precision = 18, scale = 2, updatable = false)
  private BigDecimal refundAmount;

  @Column(name = "reason", nullable = false, updatable = false)
  private String reason;

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

  public Instant getCancelledAt() {
    return cancelledAt;
  }

  public void setCancelledAt(Instant cancelledAt) {
    this.cancelledAt = cancelledAt;
  }

  public Integer getDaysBeforeCelebration() {
    return daysBeforeCelebration;
  }

  public void setDaysBeforeCelebration(Integer daysBeforeCelebration) {
    this.daysBeforeCelebration = daysBeforeCelebration;
  }

  public BigDecimal getAppliedDepositRefundPercentage() {
    return appliedDepositRefundPercentage;
  }

  public void setAppliedDepositRefundPercentage(BigDecimal appliedDepositRefundPercentage) {
    this.appliedDepositRefundPercentage = appliedDepositRefundPercentage;
  }

  public BigDecimal getRefundAmount() {
    return refundAmount;
  }

  public void setRefundAmount(BigDecimal refundAmount) {
    this.refundAmount = refundAmount;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }
}
