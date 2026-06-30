package com.uit.weddingmanagement.modules.catalog.application.usecase;

import com.uit.weddingmanagement.common.exception.DuplicateResourceException;
import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.catalog.application.model.result.ServiceItemResult;
import com.uit.weddingmanagement.modules.catalog.application.model.command.UpdateServiceItemCommand;
import com.uit.weddingmanagement.modules.catalog.application.port.in.UpdateServiceItemUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItem;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateServiceItemService implements UpdateServiceItemUseCase {

  private final ServiceItemQueryPort serviceItemQueryPort;
  private final ServiceItemCommandPort serviceItemCommandPort;

  public UpdateServiceItemService(
      ServiceItemQueryPort serviceItemQueryPort, ServiceItemCommandPort serviceItemCommandPort) {
    this.serviceItemQueryPort = serviceItemQueryPort;
    this.serviceItemCommandPort = serviceItemCommandPort;
  }

  @Override
  @AuditAction(
      action = "SERVICE_UPDATE",
      module = "CATALOG",
      targetType = "SERVICE",
      targetIdExpression = "#serviceItemId",
      targetLabelExpression = "#command.serviceName",
      successDescriptionExpression = "'Updated service ' + #serviceItemId",
      failureDescriptionExpression = "'Failed to update service ' + #serviceItemId",
      detailsExpression = "#command")
  public ServiceItemResult updateServiceItem(Long serviceItemId, UpdateServiceItemCommand command) {
    Long normalizedServiceItemId = requireServiceItemId(serviceItemId);

    ServiceItem existingServiceItem =
        serviceItemQueryPort
            .findServiceItemById(normalizedServiceItemId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Service item not found with id: " + normalizedServiceItemId));

    ServiceItem updatedServiceItem =
        existingServiceItem.updateInfo(
            command.serviceName(),
            command.serviceCategory(),
            command.unitName(),
            command.status(),
            command.description());

    ensureUniqueServiceName(updatedServiceItem.serviceName(), normalizedServiceItemId);

    try {
      return ServiceItemResult.from(serviceItemCommandPort.saveServiceItem(updatedServiceItem));
    } catch (DataIntegrityViolationException exception) {
      throw new DuplicateResourceException(
          "Service item name already exists: " + updatedServiceItem.serviceName());
    }
  }

  private Long requireServiceItemId(Long serviceItemId) {
    if (serviceItemId == null) {
      throw new IllegalArgumentException("Service item id is required.");
    }

    return serviceItemId;
  }

  private void ensureUniqueServiceName(String serviceName, Long serviceItemId) {
    if (serviceItemQueryPort.existsServiceItemByNameAndIdNot(serviceName, serviceItemId)) {
      throw new DuplicateResourceException("Service item name already exists: " + serviceName);
    }
  }
}
