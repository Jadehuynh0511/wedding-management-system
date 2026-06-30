package com.uit.weddingmanagement.modules.catalog.application.usecase;

import com.uit.weddingmanagement.modules.catalog.application.model.result.ServiceItemResult;
import com.uit.weddingmanagement.modules.catalog.application.port.in.ListServiceItemsUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemQueryPort;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListServiceItemsService implements ListServiceItemsUseCase {

  private final ServiceItemQueryPort serviceItemQueryPort;

  public ListServiceItemsService(ServiceItemQueryPort serviceItemQueryPort) {
    this.serviceItemQueryPort = serviceItemQueryPort;
  }

  @Override
  public List<ServiceItemResult> listServiceItems(Boolean active) {
    return serviceItemQueryPort.findServiceItems(active).stream()
        .map(ServiceItemResult::from)
        .toList();
  }
}
