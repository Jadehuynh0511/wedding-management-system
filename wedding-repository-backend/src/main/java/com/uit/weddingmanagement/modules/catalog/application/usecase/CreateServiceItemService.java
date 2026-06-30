package com.uit.weddingmanagement.modules.catalog.application.usecase;

import com.uit.weddingmanagement.common.exception.DuplicateResourceException;
import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.catalog.application.model.command.CreateServiceItemCommand;
import com.uit.weddingmanagement.modules.catalog.application.model.result.ServiceItemResult;
import com.uit.weddingmanagement.modules.catalog.application.port.in.CreateServiceItemUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItem;
import java.time.Instant;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateServiceItemService implements CreateServiceItemUseCase {

  private final ServiceItemQueryPort serviceItemQueryPort;
  private final ServiceItemCommandPort serviceItemCommandPort;

  public CreateServiceItemService(
      ServiceItemQueryPort serviceItemQueryPort, ServiceItemCommandPort serviceItemCommandPort) {
    this.serviceItemQueryPort = serviceItemQueryPort;
    this.serviceItemCommandPort = serviceItemCommandPort;
  }

  @Override
  @AuditAction(
      action = "SERVICE_CREATE",
      module = "CATALOG",
      targetType = "SERVICE",
      targetIdExpression = "#result.id",
      targetLabelExpression = "#result.serviceName",
      successDescriptionExpression = "'Created service ' + #result.serviceName",
      failureDescriptionExpression = "'Failed to create service ' + #command.serviceName",
      detailsExpression = "#command")
  public ServiceItemResult createServiceItem(CreateServiceItemCommand command) {
    ServiceItem serviceItem =
        ServiceItem.create(
            command.serviceName(),
            command.serviceCategory(),
            command.unitName(),
            command.currentPrice(),
            command.status(),
            Instant.now(),
            command.description());

    ensureUniqueServiceName(serviceItem.serviceName());

    try {
      return ServiceItemResult.from(serviceItemCommandPort.saveServiceItem(serviceItem));
    } catch (DataIntegrityViolationException exception) {
      throw new DuplicateResourceException(
          "Service item name already exists: " + serviceItem.serviceName());
    }
  }

  private void ensureUniqueServiceName(String serviceName) {
    if (serviceItemQueryPort.existsServiceItemByName(serviceName)) {
      throw new DuplicateResourceException("Service item name already exists: " + serviceName);
    }
  }
}
