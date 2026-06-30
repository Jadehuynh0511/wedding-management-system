package com.uit.weddingmanagement.modules.catalog.application.usecase;

import com.uit.weddingmanagement.common.exception.DuplicateResourceException;
import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.catalog.application.model.result.HallTypeResult;
import com.uit.weddingmanagement.modules.catalog.application.model.command.UpdateHallTypeCommand;
import com.uit.weddingmanagement.modules.catalog.application.port.in.UpdateHallTypeUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallTypeCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallTypeQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.HallType;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateHallTypeService implements UpdateHallTypeUseCase {

  private final HallTypeQueryPort hallTypeQueryPort;
  private final HallTypeCommandPort hallTypeCommandPort;

  public UpdateHallTypeService(
      HallTypeQueryPort hallTypeQueryPort, HallTypeCommandPort hallTypeCommandPort) {
    this.hallTypeQueryPort = hallTypeQueryPort;
    this.hallTypeCommandPort = hallTypeCommandPort;
  }

  @Override
  @AuditAction(
      action = "HALL_TYPE_UPDATE",
      module = "CATALOG",
      targetType = "HALL_TYPE",
      targetIdExpression = "#hallTypeId",
      targetLabelExpression = "#command.hallTypeName",
      successDescriptionExpression = "'Updated hall type ' + #hallTypeId",
      failureDescriptionExpression = "'Failed to update hall type ' + #hallTypeId",
      detailsExpression = "#command")
  public HallTypeResult updateHallType(Long hallTypeId, UpdateHallTypeCommand command) {
    Long normalizedHallTypeId = requireHallTypeId(hallTypeId);

    HallType existingHallType =
        hallTypeQueryPort
            .findHallTypeById(normalizedHallTypeId)
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Hall type not found with id: " + normalizedHallTypeId));

    HallType updatedHallType =
        existingHallType.update(
            command.hallTypeName(), command.minimumTablePrice(), command.description());

    ensureUniqueHallTypeName(updatedHallType.hallTypeName(), normalizedHallTypeId);

    try {
      HallType savedHallType = hallTypeCommandPort.saveHallType(updatedHallType);

      return HallTypeResult.from(savedHallType);
    } catch (DataIntegrityViolationException exception) {
      throw new DuplicateResourceException(
          "Hall type name already exists: " + updatedHallType.hallTypeName());
    }
  }

  private Long requireHallTypeId(Long hallTypeId) {
    if (hallTypeId == null) {
      throw new IllegalArgumentException("Hall type id is required.");
    }

    return hallTypeId;
  }

  private void ensureUniqueHallTypeName(String hallTypeName, Long hallTypeId) {
    if (hallTypeQueryPort.existsHallTypeByNameAndIdNot(hallTypeName, hallTypeId)) {
      throw new DuplicateResourceException("Hall type name already exists: " + hallTypeName);
    }
  }
}
