package com.uit.weddingmanagement.modules.catalog.application.usecase;

import com.uit.weddingmanagement.common.exception.ResourceInUseException;
import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.catalog.application.port.in.DeleteServiceItemUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemReferenceQueryPort;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteServiceItemService implements DeleteServiceItemUseCase {

  private final ServiceItemQueryPort serviceItemQueryPort;
  private final ServiceItemCommandPort serviceItemCommandPort;
  private final ServiceItemReferenceQueryPort serviceItemReferenceQueryPort;

  public DeleteServiceItemService(
      ServiceItemQueryPort serviceItemQueryPort,
      ServiceItemCommandPort serviceItemCommandPort,
      ServiceItemReferenceQueryPort serviceItemReferenceQueryPort) {
    this.serviceItemQueryPort = serviceItemQueryPort;
    this.serviceItemCommandPort = serviceItemCommandPort;
    this.serviceItemReferenceQueryPort = serviceItemReferenceQueryPort;
  }

  @Override
  @AuditAction(
      action = "SERVICE_DELETE",
      module = "CATALOG",
      targetType = "SERVICE",
      targetIdExpression = "#serviceItemId",
      successDescriptionExpression = "'Deleted service ' + #serviceItemId",
      failureDescriptionExpression = "'Failed to delete service ' + #serviceItemId")
  public void deleteServiceItem(Long serviceItemId) {
    Long normalizedServiceItemId = requireServiceItemId(serviceItemId);

    serviceItemQueryPort
        .findServiceItemById(normalizedServiceItemId)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "Service item not found with id: " + normalizedServiceItemId));

    ensureServiceItemIsNotUsed(normalizedServiceItemId);

    try {
      serviceItemCommandPort.deleteServiceItemById(normalizedServiceItemId);
    } catch (DataIntegrityViolationException exception) {
      throw new ResourceInUseException(
          "Service item is currently used by existing wedding bookings or incidental receipts and cannot be deleted.");
    }
  }

  private Long requireServiceItemId(Long serviceItemId) {
    if (serviceItemId == null) {
      throw new IllegalArgumentException("Service item id is required.");
    }

    return serviceItemId;
  }

  private void ensureServiceItemIsNotUsed(Long serviceItemId) {
    // Chủ động trả 409 trước để frontend nhận đúng ngữ cảnh nghiệp vụ thay vì đọc lỗi FK khó hiểu.
    if (serviceItemReferenceQueryPort.existsAnyServiceReferenceByServiceItemId(serviceItemId)) {
      throw new ResourceInUseException(
          "Service item is currently used by existing wedding bookings or incidental receipts and cannot be deleted.");
    }
  }
}
