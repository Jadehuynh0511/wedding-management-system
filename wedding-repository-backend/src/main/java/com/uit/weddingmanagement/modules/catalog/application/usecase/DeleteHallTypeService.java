package com.uit.weddingmanagement.modules.catalog.application.usecase;

import com.uit.weddingmanagement.common.exception.ResourceInUseException;
import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.catalog.application.port.in.DeleteHallTypeUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallReferenceQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallTypeCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallTypeQueryPort;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteHallTypeService implements DeleteHallTypeUseCase {

  private final HallTypeQueryPort hallTypeQueryPort;
  private final HallTypeCommandPort hallTypeCommandPort;
  private final HallReferenceQueryPort hallReferenceQueryPort;

  public DeleteHallTypeService(
      HallTypeQueryPort hallTypeQueryPort,
      HallTypeCommandPort hallTypeCommandPort,
      HallReferenceQueryPort hallReferenceQueryPort) {
    this.hallTypeQueryPort = hallTypeQueryPort;
    this.hallTypeCommandPort = hallTypeCommandPort;
    this.hallReferenceQueryPort = hallReferenceQueryPort;
  }

  @Override
  @AuditAction(
      action = "HALL_TYPE_DELETE",
      module = "CATALOG",
      targetType = "HALL_TYPE",
      targetIdExpression = "#hallTypeId",
      successDescriptionExpression = "'Deleted hall type ' + #hallTypeId",
      failureDescriptionExpression = "'Failed to delete hall type ' + #hallTypeId")
  public void deleteHallType(Long hallTypeId) {
    Long normalizedHallTypeId = requireHallTypeId(hallTypeId);

    hallTypeQueryPort
        .findHallTypeById(normalizedHallTypeId)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "Hall type not found with id: " + normalizedHallTypeId));

    ensureHallTypeIsNotInUse(normalizedHallTypeId);

    try {
      hallTypeCommandPort.deleteHallTypeById(normalizedHallTypeId);
    } catch (DataIntegrityViolationException exception) {
      throw new ResourceInUseException(
          "Hall type is currently used by existing halls and cannot be deleted.");
    }
  }

  private Long requireHallTypeId(Long hallTypeId) {
    if (hallTypeId == null) {
      throw new IllegalArgumentException("Hall type id is required.");
    }

    return hallTypeId;
  }

  private void ensureHallTypeIsNotInUse(Long hallTypeId) {
    if (hallReferenceQueryPort.existsHallByHallTypeId(hallTypeId)) {
      throw new ResourceInUseException(
          "Hall type is currently used by existing halls and cannot be deleted.");
    }
  }
}
