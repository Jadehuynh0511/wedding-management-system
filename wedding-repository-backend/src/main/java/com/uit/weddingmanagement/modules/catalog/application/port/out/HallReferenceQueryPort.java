package com.uit.weddingmanagement.modules.catalog.application.port.out;

public interface HallReferenceQueryPort {

  boolean existsHallByHallTypeId(Long hallTypeId);
}
