package com.uit.weddingmanagement.modules.catalog.application.port.out;

import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItem;
import java.util.List;
import java.util.Optional;

public interface ServiceItemQueryPort {

  List<ServiceItem> findServiceItems(Boolean active);

  Optional<ServiceItem> findServiceItemById(Long serviceItemId);

  boolean existsServiceItemByName(String serviceName);

  boolean existsServiceItemByNameAndIdNot(String serviceName, Long serviceItemId);
}
