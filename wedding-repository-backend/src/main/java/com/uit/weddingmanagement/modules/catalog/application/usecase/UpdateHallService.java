package com.uit.weddingmanagement.modules.catalog.application.usecase;

import com.uit.weddingmanagement.common.exception.DuplicateResourceException;
import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.catalog.application.model.result.HallResult;
import com.uit.weddingmanagement.modules.catalog.application.model.command.UpdateHallCommand;
import com.uit.weddingmanagement.modules.catalog.application.port.in.UpdateHallUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallTypeQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.Hall;
import com.uit.weddingmanagement.modules.catalog.domain.model.HallType;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateHallService implements UpdateHallUseCase {

  private final HallQueryPort hallQueryPort;
  private final HallTypeQueryPort hallTypeQueryPort;
  private final HallCommandPort hallCommandPort;

  public UpdateHallService(
      HallQueryPort hallQueryPort,
      HallTypeQueryPort hallTypeQueryPort,
      HallCommandPort hallCommandPort) {
    this.hallQueryPort = hallQueryPort;
    this.hallTypeQueryPort = hallTypeQueryPort;
    this.hallCommandPort = hallCommandPort;
  }

  @Override
  @AuditAction(
      action = "HALL_UPDATE",
      module = "CATALOG",
      targetType = "HALL",
      targetIdExpression = "#hallId",
      targetLabelExpression = "#command.hallName",
      successDescriptionExpression = "'Updated hall ' + #hallId",
      failureDescriptionExpression = "'Failed to update hall ' + #hallId",
      detailsExpression = "#command")
  public HallResult updateHall(Long hallId, UpdateHallCommand command) {
    Long normalizedHallId = requireHallId(hallId);
    Long hallTypeId = requireHallTypeId(command.hallTypeId());
    String hallName = normalizeHallName(command.hallName());

    Hall existingHall =
        hallQueryPort
            .findHallById(normalizedHallId)
            .orElseThrow(
                () -> new EntityNotFoundException("Hall not found with id: " + normalizedHallId));

    HallType hallType =
        hallTypeQueryPort
            .findHallTypeById(hallTypeId)
            .orElseThrow(
                () -> new EntityNotFoundException("Hall type not found with id: " + hallTypeId));

    ensureUniqueHallName(hallName, normalizedHallId);

    try {
      Hall savedHall =
          hallCommandPort.saveHall(
              existingHall.update(
                  hallType,
                  command.hallName(),
                  command.maxCapacity(),
                  command.tablePrice(),
                  command.status(),
                  command.description()));

      return HallResult.from(savedHall);
    } catch (DataIntegrityViolationException exception) {
      throw new DuplicateResourceException("Hall name already exists: " + hallName);
    }
  }

  private Long requireHallId(Long hallId) {
    if (hallId == null) {
      throw new IllegalArgumentException("Hall id is required.");
    }

    return hallId;
  }

  private Long requireHallTypeId(Long hallTypeId) {
    if (hallTypeId == null) {
      throw new IllegalArgumentException("Hall type id is required.");
    }

    if (hallTypeId <= 0) {
      throw new IllegalArgumentException("Hall type id must be greater than 0.");
    }

    return hallTypeId;
  }

  private void ensureUniqueHallName(String hallName, Long hallId) {
    if (hallQueryPort.existsHallByNameAndIdNot(hallName, hallId)) {
      throw new DuplicateResourceException("Hall name already exists: " + hallName);
    }
  }

  private String normalizeHallName(String hallName) {
    if (hallName == null || hallName.isBlank()) {
      throw new IllegalArgumentException("Hall name is required.");
    }

    return hallName.trim().replaceAll("\\s+", " ");
  }
}
