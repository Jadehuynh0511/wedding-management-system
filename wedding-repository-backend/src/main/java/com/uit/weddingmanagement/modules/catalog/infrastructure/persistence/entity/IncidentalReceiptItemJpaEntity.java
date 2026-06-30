package com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity;

import com.uit.weddingmanagement.common.entity.BaseEntity;
import com.uit.weddingmanagement.modules.booking.infrastructure.persistence.entity.IncidentalReceiptJpaEntity;
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
@Table(name = "incidental_receipt_items")
public class IncidentalReceiptItemJpaEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "incidental_receipt_id", nullable = false, updatable = false)
  private IncidentalReceiptJpaEntity incidentalReceipt;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "service_id", nullable = false, updatable = false)
  private ServiceItemJpaEntity serviceItem;

  @Column(name = "quantity", nullable = false, updatable = false)
  private Integer quantity;

  @Column(name = "applied_unit_price", nullable = false, precision = 18, scale = 2, updatable = false)
  private BigDecimal appliedUnitPrice;

  @Column(name = "line_total", nullable = false, precision = 18, scale = 2, updatable = false)
  private BigDecimal lineTotal;

  @Column(name = "notes", updatable = false)
  private String notes;

  public Long getId() {
    return id;
  }

  public IncidentalReceiptJpaEntity getIncidentalReceipt() {
    return incidentalReceipt;
  }

  public void setIncidentalReceipt(IncidentalReceiptJpaEntity incidentalReceipt) {
    this.incidentalReceipt = incidentalReceipt;
  }

  public ServiceItemJpaEntity getServiceItem() {
    return serviceItem;
  }

  public void setServiceItem(ServiceItemJpaEntity serviceItem) {
    this.serviceItem = serviceItem;
  }

  public Integer getQuantity() {
    return quantity;
  }

  public void setQuantity(Integer quantity) {
    this.quantity = quantity;
  }

  public BigDecimal getAppliedUnitPrice() {
    return appliedUnitPrice;
  }

  public void setAppliedUnitPrice(BigDecimal appliedUnitPrice) {
    this.appliedUnitPrice = appliedUnitPrice;
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
