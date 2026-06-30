package com.uit.weddingmanagement.modules.catalog.application.usecase;

import com.uit.weddingmanagement.common.exception.DuplicateResourceException;
import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.catalog.application.model.command.CreateShiftCommand;
import com.uit.weddingmanagement.modules.catalog.application.model.result.ShiftResult;
import com.uit.weddingmanagement.modules.catalog.application.port.in.CreateShiftUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ShiftCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ShiftQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.Shift;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateShiftService implements CreateShiftUseCase {

  private final ShiftQueryPort shiftQueryPort;
  private final ShiftCommandPort shiftCommandPort;

  public CreateShiftService(ShiftQueryPort shiftQueryPort, ShiftCommandPort shiftCommandPort) {
    this.shiftQueryPort = shiftQueryPort;
    this.shiftCommandPort = shiftCommandPort;
  }

  @Override
  @AuditAction(
      action = "SHIFT_CREATE",
      module = "CATALOG",
      targetType = "SHIFT",
      targetIdExpression = "#result.id",
      targetLabelExpression = "#result.shiftName",
      successDescriptionExpression = "'Created shift ' + #result.shiftName",
      failureDescriptionExpression = "'Failed to create shift ' + #command.shiftName",
      detailsExpression = "#command")
  public ShiftResult createShift(CreateShiftCommand command) {
    Shift shift =
        Shift.create(
            command.shiftName(), command.startTime(), command.endTime(), command.description());

    ensureUniqueShiftName(shift.shiftName());
    ensureShiftTimeDoesNotOverlap(shift);

    try {
      return ShiftResult.from(shiftCommandPort.saveShift(shift));
    } catch (DataIntegrityViolationException exception) {
      throw new DuplicateResourceException("Shift name already exists: " + shift.shiftName());
    }
  }

  private void ensureUniqueShiftName(String shiftName) {
    if (shiftQueryPort.existsShiftByName(shiftName)) {
      throw new DuplicateResourceException("Shift name already exists: " + shiftName);
    }
  }

  private void ensureShiftTimeDoesNotOverlap(Shift candidateShift) {
    // Vì số lượng ca trong bài toán này nhỏ, use case chủ động duyệt toàn bộ danh mục hiện có
    // để giữ cho luật overlap đọc rất rõ ở tầng nghiệp vụ.
    shiftQueryPort.findAllShifts().stream()
        .filter(existingShift -> candidateShift.overlapsWith(existingShift))
        .findFirst()
        .ifPresent(
            overlappingShift -> {
              throw new IllegalArgumentException(
                  "Shift time range overlaps with existing shift: "
                      + overlappingShift.shiftName());
            });
  }
}
