package com.uit.weddingmanagement.modules.catalog.application.usecase;

import com.uit.weddingmanagement.common.exception.ResourceInUseException;
import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.catalog.application.port.in.DeleteShiftUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ShiftBookingReferenceQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ShiftCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ShiftQueryPort;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteShiftService implements DeleteShiftUseCase {

  private final ShiftQueryPort shiftQueryPort;
  private final ShiftCommandPort shiftCommandPort;
  private final ShiftBookingReferenceQueryPort shiftBookingReferenceQueryPort;

  public DeleteShiftService(
      ShiftQueryPort shiftQueryPort,
      ShiftCommandPort shiftCommandPort,
      ShiftBookingReferenceQueryPort shiftBookingReferenceQueryPort) {
    this.shiftQueryPort = shiftQueryPort;
    this.shiftCommandPort = shiftCommandPort;
    this.shiftBookingReferenceQueryPort = shiftBookingReferenceQueryPort;
  }

  @Override
  @AuditAction(
      action = "SHIFT_DELETE",
      module = "CATALOG",
      targetType = "SHIFT",
      targetIdExpression = "#shiftId",
      successDescriptionExpression = "'Deleted shift ' + #shiftId",
      failureDescriptionExpression = "'Failed to delete shift ' + #shiftId")
  public void deleteShift(Long shiftId) {
    Long normalizedShiftId = requireShiftId(shiftId);

    shiftQueryPort
        .findShiftById(normalizedShiftId)
        .orElseThrow(
            () -> new EntityNotFoundException("Shift not found with id: " + normalizedShiftId));

    ensureShiftIsNotUsedByWeddingBookings(normalizedShiftId);

    try {
      shiftCommandPort.deleteShiftById(normalizedShiftId);
    } catch (DataIntegrityViolationException exception) {
      throw new ResourceInUseException(
          "Shift is currently used by existing wedding bookings and cannot be deleted.");
    }
  }

  private Long requireShiftId(Long shiftId) {
    if (shiftId == null) {
      throw new IllegalArgumentException("Shift id is required.");
    }

    return shiftId;
  }

  private void ensureShiftIsNotUsedByWeddingBookings(Long shiftId) {
    // Chủ động trả 409 ở tầng nghiệp vụ để frontend nhận được lỗi rõ nghĩa khi ca đã
    // được dùng trong dữ liệu đặt tiệc.
    if (shiftBookingReferenceQueryPort.existsWeddingBookingByShiftId(shiftId)) {
      throw new ResourceInUseException(
          "Shift is currently used by existing wedding bookings and cannot be deleted.");
    }
  }
}
