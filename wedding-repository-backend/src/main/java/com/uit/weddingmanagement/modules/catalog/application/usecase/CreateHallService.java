package com.uit.weddingmanagement.modules.catalog.application.usecase;

import com.uit.weddingmanagement.common.exception.DuplicateResourceException;
import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.catalog.application.model.command.CreateHallCommand;
import com.uit.weddingmanagement.modules.catalog.application.model.result.HallResult;
import com.uit.weddingmanagement.modules.catalog.application.port.in.CreateHallUseCase;
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
public class CreateHallService implements CreateHallUseCase {

  private final HallQueryPort hallQueryPort;
  private final HallTypeQueryPort hallTypeQueryPort;
  private final HallCommandPort hallCommandPort;

  public CreateHallService(
      HallQueryPort hallQueryPort,
      HallTypeQueryPort hallTypeQueryPort,
      HallCommandPort hallCommandPort) {
    this.hallQueryPort = hallQueryPort;
    this.hallTypeQueryPort = hallTypeQueryPort;
    this.hallCommandPort = hallCommandPort;
  }

  @Override
  @AuditAction(
      action = "HALL_CREATE",
      module = "CATALOG",
      targetType = "HALL",
      targetIdExpression = "#result.id",
      targetLabelExpression = "#result.hallName",
      successDescriptionExpression = "'Created hall ' + #result.hallName",
      failureDescriptionExpression = "'Failed to create hall ' + #command.hallName",
      detailsExpression = "#command")
  public HallResult createHall(CreateHallCommand command) {
    Long hallTypeId = requireHallTypeId(command.hallTypeId());
    String hallName = normalizeHallName(command.hallName());

    HallType hallType =
        hallTypeQueryPort
            .findHallTypeById(hallTypeId)
            .orElseThrow(
                () -> new EntityNotFoundException("Hall type not found with id: " + hallTypeId));

    // Unique name là rule phụ thuộc persistence, nên use case vẫn là nơi điều phối bước kiểm tra
    // này trước khi chuyển sang domain để tạo object hợp lệ.
    ensureUniqueHallName(hallName);

    try {
      Hall savedHall =
          hallCommandPort.saveHall(
              Hall.create(
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

  private Long requireHallTypeId(Long hallTypeId) {
    if (hallTypeId == null) {
      throw new IllegalArgumentException("Hall type id is required.");
    }

    if (hallTypeId <= 0) {
      throw new IllegalArgumentException("Hall type id must be greater than 0.");
    }

    return hallTypeId;
  }

  private void ensureUniqueHallName(String hallName) {
    if (hallQueryPort.existsHallByName(hallName)) {
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
