package com.uit.weddingmanagement.modules.catalog.infrastructure.persistence;

import com.uit.weddingmanagement.modules.catalog.application.port.out.ShiftBookingReferenceQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ShiftCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ShiftQueryPort;
import com.uit.weddingmanagement.modules.booking.infrastructure.persistence.repository.WeddingBookingJpaRepository;
import com.uit.weddingmanagement.modules.catalog.domain.model.Shift;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity.ShiftJpaEntity;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.repository.ShiftJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class ShiftPersistenceAdapter
    implements ShiftQueryPort, ShiftCommandPort, ShiftBookingReferenceQueryPort {

  private final ShiftJpaRepository shiftJpaRepository;
  private final WeddingBookingJpaRepository weddingBookingJpaRepository;

  public ShiftPersistenceAdapter(
      ShiftJpaRepository shiftJpaRepository,
      WeddingBookingJpaRepository weddingBookingJpaRepository) {
    this.shiftJpaRepository = shiftJpaRepository;
    this.weddingBookingJpaRepository = weddingBookingJpaRepository;
  }

  @Override
  public List<Shift> findAllShifts() {
    return shiftJpaRepository.findAllByOrderByIdAsc().stream().map(this::toDomain).toList();
  }

  @Override
  public Optional<Shift> findShiftById(Long shiftId) {
    return shiftJpaRepository.findById(shiftId).map(this::toDomain);
  }

  @Override
  public boolean existsShiftByName(String shiftName) {
    return shiftJpaRepository.existsByShiftNameIgnoreCase(shiftName);
  }

  @Override
  public boolean existsShiftByNameAndIdNot(String shiftName, Long shiftId) {
    return shiftJpaRepository.existsByShiftNameIgnoreCaseAndIdNot(shiftName, shiftId);
  }

  @Override
  public Shift saveShift(Shift shift) {
    ShiftJpaEntity shiftJpaEntity = resolveEntityForSave(shift);
    shiftJpaEntity.setShiftName(shift.shiftName());
    shiftJpaEntity.setStartTime(shift.startTime());
    shiftJpaEntity.setEndTime(shift.endTime());
    shiftJpaEntity.setDescription(shift.description());

    return toDomain(shiftJpaRepository.save(shiftJpaEntity));
  }

  @Override
  public void deleteShiftById(Long shiftId) {
    shiftJpaRepository.deleteById(shiftId);
  }

  @Override
  public boolean existsWeddingBookingByShiftId(Long shiftId) {
    return weddingBookingJpaRepository.existsByShift_Id(shiftId);
  }

  private ShiftJpaEntity resolveEntityForSave(Shift shift) {
    if (shift.id() == null) {
      return new ShiftJpaEntity();
    }

    return shiftJpaRepository
        .findById(shift.id())
        .orElseThrow(() -> new EntityNotFoundException("Shift not found with id: " + shift.id()));
  }

  private Shift toDomain(ShiftJpaEntity shiftJpaEntity) {
    return new Shift(
        shiftJpaEntity.getId(),
        shiftJpaEntity.getShiftName(),
        shiftJpaEntity.getStartTime(),
        shiftJpaEntity.getEndTime(),
        shiftJpaEntity.getDescription());
  }
}
