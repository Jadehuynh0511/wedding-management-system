package com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity;

import com.uit.weddingmanagement.common.entity.BaseEntity;
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
import java.time.Instant;

@Entity
@Table(name = "service_price_history")
public class ServicePriceHistoryJpaEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "service_id", nullable = false)
  private ServiceItemJpaEntity serviceItem;

  @Column(name = "old_price", nullable = false, precision = 18, scale = 2)
  private BigDecimal oldPrice;

  @Column(name = "effective_from", nullable = false)
  private Instant effectiveFrom;

  @Column(name = "effective_to", nullable = false)
  private Instant effectiveTo;

  public Long getId() {
    return id;
  }

  public ServiceItemJpaEntity getServiceItem() {
    return serviceItem;
  }

  public void setServiceItem(ServiceItemJpaEntity serviceItem) {
    this.serviceItem = serviceItem;
  }

  public BigDecimal getOldPrice() {
    return oldPrice;
  }

  public void setOldPrice(BigDecimal oldPrice) {
    this.oldPrice = oldPrice;
  }

  public Instant getEffectiveFrom() {
    return effectiveFrom;
  }

  public void setEffectiveFrom(Instant effectiveFrom) {
    this.effectiveFrom = effectiveFrom;
  }

  public Instant getEffectiveTo() {
    return effectiveTo;
  }

  public void setEffectiveTo(Instant effectiveTo) {
    this.effectiveTo = effectiveTo;
  }
}
