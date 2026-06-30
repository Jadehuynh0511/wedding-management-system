package com.uit.weddingmanagement.modules.catalog.application.port.out;

import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItem;

public interface ServiceItemCommandPort {

  ServiceItem saveServiceItem(ServiceItem serviceItem);

  void deleteServiceItemById(Long serviceItemId);
}
