package com.uit.weddingmanagement.modules.catalog.domain.model;

public enum MenuItemStatus {
  CON,
  HET;

  public boolean isAvailable() {
    return this == CON;
  }
}
