package com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity;

import com.uit.weddingmanagement.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "hall_types")
public class HallTypeJpaEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "hall_type_name", nullable = false, unique = true, length = 100)
  private String hallTypeName;

  @Column(name = "minimum_table_price", nullable = false, precision = 18, scale = 2)
  private BigDecimal minimumTablePrice;

  @Column(name = "description")
  private String description;

  // Getters and setters
  public Long getId() {
    return id;
  }

  public String getHallTypeName() {
    return hallTypeName;
  }

  public void setHallTypeName(String hallTypeName) {
    this.hallTypeName = hallTypeName;
  }

  public BigDecimal getMinimumTablePrice() {
    return minimumTablePrice;
  }

  public void setMinimumTablePrice(BigDecimal minimumTablePrice) {
    this.minimumTablePrice = minimumTablePrice;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
