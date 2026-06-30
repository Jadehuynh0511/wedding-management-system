package com.uit.weddingmanagement.modules.system.infrastructure.persistence.entity;

import com.uit.weddingmanagement.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "system_parameters")
public class SystemParameterJpaEntity extends BaseEntity {

  @Id
  private Short id;

  @Column(name = "minimum_deposit_percentage", nullable = false, precision = 5, scale = 2)
  private BigDecimal minimumDepositPercentage;

  @Column(name = "late_payment_penalty_enabled", nullable = false)
  private boolean latePaymentPenaltyEnabled;

  @Column(name = "late_payment_penalty_rate", nullable = false, precision = 5, scale = 2)
  private BigDecimal latePaymentPenaltyRate;

  @Column(name = "cancellation_deadline_days", nullable = false)
  private Integer cancellationDeadlineDays;

  @Column(name = "deposit_refund_percentage", nullable = false, precision = 5, scale = 2)
  private BigDecimal depositRefundPercentage;

  // Getters and setters
  public Short getId() {
    return id;
  }

  public void setId(Short id) {
    this.id = id;
  }

  public BigDecimal getMinimumDepositPercentage() {
    return minimumDepositPercentage;
  }

  public void setMinimumDepositPercentage(BigDecimal minimumDepositPercentage) {
    this.minimumDepositPercentage = minimumDepositPercentage;
  }

  public boolean isLatePaymentPenaltyEnabled() {
    return latePaymentPenaltyEnabled;
  }

  public void setLatePaymentPenaltyEnabled(boolean latePaymentPenaltyEnabled) {
    this.latePaymentPenaltyEnabled = latePaymentPenaltyEnabled;
  }

  public BigDecimal getLatePaymentPenaltyRate() {
    return latePaymentPenaltyRate;
  }

  public void setLatePaymentPenaltyRate(BigDecimal latePaymentPenaltyRate) {
    this.latePaymentPenaltyRate = latePaymentPenaltyRate;
  }

  public Integer getCancellationDeadlineDays() {
    return cancellationDeadlineDays;
  }

  public void setCancellationDeadlineDays(Integer cancellationDeadlineDays) {
    this.cancellationDeadlineDays = cancellationDeadlineDays;
  }

  public BigDecimal getDepositRefundPercentage() {
    return depositRefundPercentage;
  }

  public void setDepositRefundPercentage(BigDecimal depositRefundPercentage) {
    this.depositRefundPercentage = depositRefundPercentage;
  }
}
