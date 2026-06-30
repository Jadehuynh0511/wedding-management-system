package com.uit.weddingmanagement.modules.catalog.application.usecase;

import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.catalog.application.model.result.ServiceItemDetailResult;
import com.uit.weddingmanagement.modules.catalog.application.model.result.ServicePriceHistoryResult;
import com.uit.weddingmanagement.modules.catalog.application.model.command.UpdateServiceItemPriceCommand;
import com.uit.weddingmanagement.modules.catalog.application.port.in.UpdateServiceItemPriceUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServicePriceHistoryCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServicePriceHistoryQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItem;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateServiceItemPriceService implements UpdateServiceItemPriceUseCase {

  private final ServiceItemQueryPort serviceItemQueryPort;
  private final ServiceItemCommandPort serviceItemCommandPort;
  private final ServicePriceHistoryCommandPort servicePriceHistoryCommandPort;
  private final ServicePriceHistoryQueryPort servicePriceHistoryQueryPort;

  public UpdateServiceItemPriceService(
      ServiceItemQueryPort serviceItemQueryPort,
      ServiceItemCommandPort serviceItemCommandPort,
      ServicePriceHistoryCommandPort servicePriceHistoryCommandPort,
      ServicePriceHistoryQueryPort servicePriceHistoryQueryPort) {
    this.serviceItemQueryPort = serviceItemQueryPort;
    this.serviceItemCommandPort = serviceItemCommandPort;
    this.servicePriceHistoryCommandPort = servicePriceHistoryCommandPort;
    this.servicePriceHistoryQueryPort = servicePriceHistoryQueryPort;
  }

  @Override
  @AuditAction(
      action = "SERVICE_PRICE_UPDATE",
      module = "CATALOG",
      targetType = "SERVICE",
      targetIdExpression = "#serviceItemId",
      successDescriptionExpression = "'Updated service price ' + #serviceItemId",
      failureDescriptionExpression = "'Failed to update service price ' + #serviceItemId",
      detailsExpression = "#command")
  public ServiceItemDetailResult updateServiceItemPrice(
      Long serviceItemId, UpdateServiceItemPriceCommand command) {
    Long normalizedServiceItemId = requireServiceItemId(serviceItemId);

    ServiceItem existingServiceItem =
        serviceItemQueryPort
            .findServiceItemById(normalizedServiceItemId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Service item not found with id: " + normalizedServiceItemId));

    Instant changedAt = Instant.now();

    // Giá chỉ được đổi qua luồng PATCH riêng để luôn đóng kỳ hiệu lực cũ và lưu lại history,
    // tránh việc update metadata bình thường vô tình làm mất dấu vết thay đổi giá.
    ServiceItem updatedServiceItem =
        existingServiceItem.changePrice(command.newPrice(), changedAt);
    servicePriceHistoryCommandPort.saveServicePriceHistory(
        existingServiceItem.closeCurrentPricePeriod(changedAt));
    updatedServiceItem = serviceItemCommandPort.saveServiceItem(updatedServiceItem);

    List<ServicePriceHistoryResult> priceHistory =
        servicePriceHistoryQueryPort
            .findServicePriceHistoriesByServiceItemId(normalizedServiceItemId)
            .stream()
            .map(ServicePriceHistoryResult::from)
            .toList();

    return ServiceItemDetailResult.from(updatedServiceItem, priceHistory);
  }

  private Long requireServiceItemId(Long serviceItemId) {
    if (serviceItemId == null) {
      throw new IllegalArgumentException("Service item id is required.");
    }

    return serviceItemId;
  }
}
