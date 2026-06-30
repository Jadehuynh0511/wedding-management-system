package com.uit.weddingmanagement.modules.catalog.application.usecase;

import com.uit.weddingmanagement.common.exception.DuplicateResourceException;
import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.catalog.application.model.result.ShiftResult;
import com.uit.weddingmanagement.modules.catalog.application.model.command.UpdateShiftCommand;
import com.uit.weddingmanagement.modules.catalog.application.port.in.UpdateShiftUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ShiftCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ShiftQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.Shift;
import jakarta.persistence.EntityNotFoundException;
import java.util.Objects;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UpdateShiftService implements UpdateShiftUseCase {

  private final ShiftQueryPort shiftQueryPort;
  private final ShiftCommandPort shiftCommandPort;

  public UpdateShiftService(ShiftQueryPort shiftQueryPort, ShiftCommandPort shiftCommandPort) {
    this.shiftQueryPort = shiftQueryPort;
    this.shiftCommandPort = shiftCommandPort;
  }

  @Override
  @AuditAction(
      action = "SHIFT_UPDATE",
      module = "CATALOG",
      targetType = "SHIFT",
      targetIdExpression = "#shiftId",
      targetLabelExpression = "#command.shiftName",
      successDescriptionExpression = "'Updated shift ' + #shiftId",
      failureDescriptionExpression = "'Failed to update shift ' + #shiftId",
      detailsExpression = "#command")
  public ShiftResult updateShift(Long shiftId, UpdateShiftCommand command) {
    Long normalizedShiftId = requireShiftId(shiftId);

    Shift existingShift =
        shiftQueryPort
            .findShiftById(normalizedShiftId)
            .orElseThrow(
                () -> new EntityNotFoundException("Shift not found with id: " + normalizedShiftId));

    Shift updatedShift =
        existingShift.update(
            command.shiftName(), command.startTime(), command.endTime(), command.description());

    ensureUniqueShiftName(updatedShift.shiftName(), normalizedShiftId);
    ensureShiftTimeDoesNotOverlap(updatedShift, normalizedShiftId);

    try {
      return ShiftResult.from(shiftCommandPort.saveShift(updatedShift));
    } catch (DataIntegrityViolationException exception) {
      throw new DuplicateResourceException(
          "Shift name already exists: " + updatedShift.shiftName());
    }
  }

  private Long requireShiftId(Long shiftId) {
    if (shiftId == null) {
      throw new IllegalArgumentException("Shift id is required.");
    }

    return shiftId;
  }

  private void ensureUniqueShiftName(String shiftName, Long shiftId) {
    if (shiftQueryPort.existsShiftByNameAndIdNot(shiftName, shiftId)) {
      throw new DuplicateResourceException("Shift name already exists: " + shiftName);
    }
  }

  private void ensureShiftTimeDoesNotOverlap(Shift candidateShift, Long shiftId) {
    shiftQueryPort.findAllShifts().stream()
        .filter(existingShift -> !Objects.equals(existingShift.id(), shiftId))
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
