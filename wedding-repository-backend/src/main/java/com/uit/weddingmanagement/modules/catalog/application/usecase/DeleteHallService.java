package com.uit.weddingmanagement.modules.catalog.application.usecase;

import com.uit.weddingmanagement.common.exception.ResourceInUseException;
import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.catalog.application.port.in.DeleteHallUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallBookingReferenceQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallQueryPort;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DeleteHallService implements DeleteHallUseCase {

  private final HallQueryPort hallQueryPort;
  private final HallCommandPort hallCommandPort;
  private final HallBookingReferenceQueryPort hallBookingReferenceQueryPort;

  public DeleteHallService(
      HallQueryPort hallQueryPort,
      HallCommandPort hallCommandPort,
      HallBookingReferenceQueryPort hallBookingReferenceQueryPort) {
    this.hallQueryPort = hallQueryPort;
    this.hallCommandPort = hallCommandPort;
    this.hallBookingReferenceQueryPort = hallBookingReferenceQueryPort;
  }

  @Override
  @AuditAction(
      action = "HALL_DELETE",
      module = "CATALOG",
      targetType = "HALL",
      targetIdExpression = "#hallId",
      successDescriptionExpression = "'Deleted hall ' + #hallId",
      failureDescriptionExpression = "'Failed to delete hall ' + #hallId")
  public void deleteHall(Long hallId) {
    Long normalizedHallId = requireHallId(hallId);

    hallQueryPort
        .findHallById(normalizedHallId)
        .orElseThrow(
            () -> new EntityNotFoundException("Hall not found with id: " + normalizedHallId));

    ensureHallIsNotUsedByWeddingBookings(normalizedHallId);

    try {
      hallCommandPort.deleteHallById(normalizedHallId);
    } catch (DataIntegrityViolationException exception) {
      throw new ResourceInUseException(
          "Hall is currently used by existing wedding bookings and cannot be deleted.");
    }
  }

  private Long requireHallId(Long hallId) {
    if (hallId == null) {
      throw new IllegalArgumentException("Hall id is required.");
    }

    return hallId;
  }

  private void ensureHallIsNotUsedByWeddingBookings(Long hallId) {
    // Chủ động trả 409 ở tầng nghiệp vụ để người dùng nhận được lỗi rõ nghĩa thay vì lỗi FK mơ hồ
    // từ DB.
    if (hallBookingReferenceQueryPort.existsWeddingBookingByHallId(hallId)) {
      throw new ResourceInUseException(
          "Hall is currently used by existing wedding bookings and cannot be deleted.");
    }
  }
}
