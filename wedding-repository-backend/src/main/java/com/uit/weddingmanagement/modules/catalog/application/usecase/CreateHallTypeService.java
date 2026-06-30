package com.uit.weddingmanagement.modules.catalog.application.usecase;

import com.uit.weddingmanagement.common.exception.DuplicateResourceException;
import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.catalog.application.model.command.CreateHallTypeCommand;
import com.uit.weddingmanagement.modules.catalog.application.model.result.HallTypeResult;
import com.uit.weddingmanagement.modules.catalog.application.port.in.CreateHallTypeUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallTypeCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallTypeQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.HallType;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateHallTypeService implements CreateHallTypeUseCase {

  private final HallTypeQueryPort hallTypeQueryPort;
  private final HallTypeCommandPort hallTypeCommandPort;

  // Service cần port để làm việc với data, nên ở đây dùng DI thông qua constructor để Spring tự
  // động inject các port cần thiết vào service này khi khởi tạo.
  public CreateHallTypeService(
      HallTypeQueryPort hallTypeQueryPort, HallTypeCommandPort hallTypeCommandPort) {
    this.hallTypeQueryPort = hallTypeQueryPort;
    this.hallTypeCommandPort = hallTypeCommandPort;
  }

  @Override
  @AuditAction(
      action = "HALL_TYPE_CREATE",
      module = "CATALOG",
      targetType = "HALL_TYPE",
      targetIdExpression = "#result.id",
      targetLabelExpression = "#result.hallTypeName",
      successDescriptionExpression = "'Created hall type ' + #result.hallTypeName",
      failureDescriptionExpression = "'Failed to create hall type ' + #command.hallTypeName",
      detailsExpression = "#command")
  public HallTypeResult createHallType(CreateHallTypeCommand command) {
    HallType hallType =
        HallType.create(
            command.hallTypeName(), command.minimumTablePrice(), command.description());

    ensureUniqueHallTypeName(hallType.hallTypeName());

    try {
      HallType savedHallType = hallTypeCommandPort.saveHallType(hallType);

      return HallTypeResult.from(savedHallType);
    } catch (DataIntegrityViolationException exception) {
      throw new DuplicateResourceException(
          "Hall type name already exists: " + hallType.hallTypeName());
    }
  }

  private void ensureUniqueHallTypeName(String hallTypeName) {
    if (hallTypeQueryPort.existsHallTypeByName(hallTypeName)) {
      throw new DuplicateResourceException("Hall type name already exists: " + hallTypeName);
    }
  }
}
