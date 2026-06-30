package com.uit.weddingmanagement.modules.catalog.application.port.out;

public interface ServiceItemReferenceQueryPort {

  boolean existsAnyServiceReferenceByServiceItemId(Long serviceItemId);
}
