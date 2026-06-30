package com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity;

import com.uit.weddingmanagement.common.entity.BaseEntity;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItemStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "services")
public class ServiceItemJpaEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "service_name", nullable = false, unique = true, length = 150)
  private String serviceName;

  @Column(name = "service_category", nullable = false, length = 100)
  private String serviceCategory;

  @Column(name = "unit_name", nullable = false, length = 50)
  private String unitName;

  @Column(name = "current_price", nullable = false, precision = 18, scale = 2)
  private BigDecimal currentPrice;

  @Column(name = "price_effective_from", nullable = false)
  private Instant priceEffectiveFrom;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private ServiceItemStatus status;

  @Column(name = "description")
  private String description;

  public Long getId() {
    return id;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getServiceCategory() {
    return serviceCategory;
  }

  public void setServiceCategory(String serviceCategory) {
    this.serviceCategory = serviceCategory;
  }

  public String getUnitName() {
    return unitName;
  }

  public void setUnitName(String unitName) {
    this.unitName = unitName;
  }

  public BigDecimal getCurrentPrice() {
    return currentPrice;
  }

  public void setCurrentPrice(BigDecimal currentPrice) {
    this.currentPrice = currentPrice;
  }

  public Instant getPriceEffectiveFrom() {
    return priceEffectiveFrom;
  }

  public void setPriceEffectiveFrom(Instant priceEffectiveFrom) {
    this.priceEffectiveFrom = priceEffectiveFrom;
  }

  public ServiceItemStatus getStatus() {
    return status;
  }

  public void setStatus(ServiceItemStatus status) {
    this.status = status;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
