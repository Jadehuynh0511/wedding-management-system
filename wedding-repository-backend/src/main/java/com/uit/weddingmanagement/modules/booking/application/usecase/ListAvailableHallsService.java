package com.uit.weddingmanagement.modules.booking.application.usecase;

import com.uit.weddingmanagement.modules.booking.application.model.result.AvailableHallResult;
import com.uit.weddingmanagement.modules.booking.application.port.in.ListAvailableHallsUseCase;
import com.uit.weddingmanagement.modules.booking.application.port.out.WeddingBookingQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ShiftQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.HallStatus;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListAvailableHallsService implements ListAvailableHallsUseCase {

  private final HallQueryPort hallQueryPort;
  private final ShiftQueryPort shiftQueryPort;
  private final WeddingBookingQueryPort weddingBookingQueryPort;

  public ListAvailableHallsService(
      HallQueryPort hallQueryPort,
      ShiftQueryPort shiftQueryPort,
      WeddingBookingQueryPort weddingBookingQueryPort) {
    this.hallQueryPort = hallQueryPort;
    this.shiftQueryPort = shiftQueryPort;
    this.weddingBookingQueryPort = weddingBookingQueryPort;
  }

  @Override
  public List<AvailableHallResult> listAvailableHalls(LocalDate celebrationDate, Long shiftId) {
    requireCelebrationDate(celebrationDate);
    requireShift(shiftId);

    Set<Long> bookedHallIds =
        weddingBookingQueryPort.findBookedHallIdsByCelebrationDateAndShiftId(celebrationDate, shiftId);

    return hallQueryPort.findAllHalls().stream()
        .filter(hall -> hall.status() != HallStatus.BAO_TRI)
        .filter(hall -> !bookedHallIds.contains(hall.id()))
        .map(AvailableHallResult::from)
        .toList();
  }

  private void requireCelebrationDate(LocalDate celebrationDate) {
    if (celebrationDate == null) {
      throw new IllegalArgumentException("Celebration date is required.");
    }

    if (celebrationDate.isBefore(LocalDate.now(ZoneOffset.UTC))) {
      throw new IllegalArgumentException("Celebration date must be today or later.");
    }
  }

  private void requireShift(Long shiftId) {
    if (shiftId == null) {
      throw new IllegalArgumentException("Shift id is required.");
    }

    if (shiftId <= 0) {
      throw new IllegalArgumentException("Shift id must be greater than 0.");
    }

    shiftQueryPort
        .findShiftById(shiftId)
        .orElseThrow(() -> new EntityNotFoundException("Shift not found with id: " + shiftId));
  }
}
