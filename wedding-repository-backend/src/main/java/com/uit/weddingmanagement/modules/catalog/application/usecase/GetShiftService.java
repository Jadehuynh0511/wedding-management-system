package com.uit.weddingmanagement.modules.catalog.application.usecase;

import com.uit.weddingmanagement.modules.catalog.application.model.result.ShiftResult;
import com.uit.weddingmanagement.modules.catalog.application.port.in.GetShiftUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ShiftQueryPort;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetShiftService implements GetShiftUseCase {

  private final ShiftQueryPort shiftQueryPort;

  public GetShiftService(ShiftQueryPort shiftQueryPort) {
    this.shiftQueryPort = shiftQueryPort;
  }

  @Override
  public ShiftResult getShift(Long shiftId) {
    Long normalizedShiftId = requireShiftId(shiftId);

    return shiftQueryPort
        .findShiftById(normalizedShiftId)
        .map(ShiftResult::from)
        .orElseThrow(
            () -> new EntityNotFoundException("Shift not found with id: " + normalizedShiftId));
  }

  private Long requireShiftId(Long shiftId) {
    if (shiftId == null) {
      throw new IllegalArgumentException("Shift id is required.");
    }

    return shiftId;
  }
}
