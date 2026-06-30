package com.uit.weddingmanagement.modules.catalog.domain.model;

public enum ServiceItemStatus {
  HOAT_DONG,
  NGUNG_HOAT_DONG;

  public boolean isActive() {
    return this == HOAT_DONG;
  }
}
