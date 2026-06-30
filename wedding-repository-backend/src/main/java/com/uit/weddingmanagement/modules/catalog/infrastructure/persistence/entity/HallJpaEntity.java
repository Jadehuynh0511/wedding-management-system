package com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity;

import com.uit.weddingmanagement.common.entity.BaseEntity;
import com.uit.weddingmanagement.modules.catalog.domain.model.HallStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "halls")
public class HallJpaEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "hall_type_id", nullable = false)
  private HallTypeJpaEntity hallType;

  @Column(name = "hall_name", nullable = false, unique = true, length = 150)
  private String hallName;

  @Column(name = "max_capacity", nullable = false)
  private Integer maxCapacity;

  @Column(name = "table_price", nullable = false, precision = 18, scale = 2)
  private BigDecimal tablePrice;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private HallStatus status;

  @Column(name = "description")
  private String description;

  // Getters and setters
  public Long getId() {
    return id;
  }

  public HallTypeJpaEntity getHallType() {
    return hallType;
  }

  public void setHallType(HallTypeJpaEntity hallType) {
    this.hallType = hallType;
  }

  public String getHallName() {
    return hallName;
  }

  public void setHallName(String hallName) {
    this.hallName = hallName;
  }

  public Integer getMaxCapacity() {
    return maxCapacity;
  }

  public void setMaxCapacity(Integer maxCapacity) {
    this.maxCapacity = maxCapacity;
  }

  public BigDecimal getTablePrice() {
    return tablePrice;
  }

  public void setTablePrice(BigDecimal tablePrice) {
    this.tablePrice = tablePrice;
  }

  public HallStatus getStatus() {
    return status;
  }

  public void setStatus(HallStatus status) {
    this.status = status;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
