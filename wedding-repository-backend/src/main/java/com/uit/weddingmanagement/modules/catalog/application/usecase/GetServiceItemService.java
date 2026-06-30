package com.uit.weddingmanagement.modules.catalog.application.usecase;

import com.uit.weddingmanagement.modules.catalog.application.model.result.ServiceItemDetailResult;
import com.uit.weddingmanagement.modules.catalog.application.model.result.ServicePriceHistoryResult;
import com.uit.weddingmanagement.modules.catalog.application.port.in.GetServiceItemUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServicePriceHistoryQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItem;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetServiceItemService implements GetServiceItemUseCase {

  private final ServiceItemQueryPort serviceItemQueryPort;
  private final ServicePriceHistoryQueryPort servicePriceHistoryQueryPort;

  public GetServiceItemService(
      ServiceItemQueryPort serviceItemQueryPort,
      ServicePriceHistoryQueryPort servicePriceHistoryQueryPort) {
    this.serviceItemQueryPort = serviceItemQueryPort;
    this.servicePriceHistoryQueryPort = servicePriceHistoryQueryPort;
  }

  @Override
  public ServiceItemDetailResult getServiceItem(Long serviceItemId) {
    Long normalizedServiceItemId = requireServiceItemId(serviceItemId);

    ServiceItem serviceItem =
        serviceItemQueryPort
            .findServiceItemById(normalizedServiceItemId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Service item not found with id: " + normalizedServiceItemId));

    List<ServicePriceHistoryResult> priceHistory =
        servicePriceHistoryQueryPort
            .findServicePriceHistoriesByServiceItemId(normalizedServiceItemId)
            .stream()
            .map(ServicePriceHistoryResult::from)
            .toList();

    return ServiceItemDetailResult.from(serviceItem, priceHistory);
  }

  private Long requireServiceItemId(Long serviceItemId) {
    if (serviceItemId == null) {
      throw new IllegalArgumentException("Service item id is required.");
    }

    return serviceItemId;
  }
}
