package com.uit.weddingmanagement.modules.catalog.infrastructure.persistence;

import com.uit.weddingmanagement.modules.catalog.application.port.out.HallBookingReferenceQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallQueryPort;
import com.uit.weddingmanagement.modules.booking.infrastructure.persistence.repository.WeddingBookingJpaRepository;
import com.uit.weddingmanagement.modules.catalog.domain.model.Hall;
import com.uit.weddingmanagement.modules.catalog.domain.model.HallType;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity.HallJpaEntity;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity.HallTypeJpaEntity;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.repository.HallJpaRepository;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.repository.HallTypeJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

// Tầng persistence adapter là nơi TRIỂN KHAI các phương thức truy cập dữ liệu từ DB, chuyển đổi giữa entity và domain model
@Component
public class HallPersistenceAdapter
    implements HallQueryPort, HallCommandPort, HallBookingReferenceQueryPort {

  private final HallJpaRepository hallJpaRepository;
  private final HallTypeJpaRepository hallTypeJpaRepository;
  private final WeddingBookingJpaRepository weddingBookingJpaRepository;

  public HallPersistenceAdapter(
      HallJpaRepository hallJpaRepository,
      HallTypeJpaRepository hallTypeJpaRepository,
      WeddingBookingJpaRepository weddingBookingJpaRepository) {
    this.hallJpaRepository = hallJpaRepository;
    this.hallTypeJpaRepository = hallTypeJpaRepository;
    this.weddingBookingJpaRepository = weddingBookingJpaRepository;
  }

  @Override
  public List<Hall> findAllHalls() {
    return hallJpaRepository.findAllByOrderByIdAsc().stream().map(this::toDomain).toList();
  }

  @Override
  public Optional<Hall> findHallById(Long hallId) {
    return hallJpaRepository.findById(hallId).map(this::toDomain);
  }

  @Override
  public boolean existsHallByName(String hallName) {
    return hallJpaRepository.existsByHallNameIgnoreCase(hallName);
  }

  @Override
  public boolean existsHallByNameAndIdNot(String hallName, Long hallId) {
    return hallJpaRepository.existsByHallNameIgnoreCaseAndIdNot(hallName, hallId);
  }

  @Override
  public Hall saveHall(Hall hall) {
    HallJpaEntity hallJpaEntity = resolveEntityForSave(hall);
    hallJpaEntity.setHallType(resolveHallType(hall.hallType().id()));
    hallJpaEntity.setHallName(hall.hallName());
    hallJpaEntity.setMaxCapacity(hall.maxCapacity());
    hallJpaEntity.setTablePrice(hall.tablePrice());
    hallJpaEntity.setStatus(hall.status());
    hallJpaEntity.setDescription(hall.description());

    return toDomain(hallJpaRepository.save(hallJpaEntity));
  }

  @Override
  public void deleteHallById(Long hallId) {
    hallJpaRepository.deleteById(hallId);
  }

  @Override
  public boolean existsWeddingBookingByHallId(Long hallId) {
    return weddingBookingJpaRepository.existsByHall_Id(hallId);
  }

  private HallJpaEntity resolveEntityForSave(Hall hall) {
    if (hall.id() == null) {
      return new HallJpaEntity();
    }

    return hallJpaRepository
        .findById(hall.id())
        .orElseThrow(() -> new EntityNotFoundException("Hall not found with id: " + hall.id()));
  }

  private HallTypeJpaEntity resolveHallType(Long hallTypeId) {
    return hallTypeJpaRepository
        .findById(hallTypeId)
        .orElseThrow(
            () -> new EntityNotFoundException("Hall type not found with id: " + hallTypeId));
  }

  private Hall toDomain(HallJpaEntity hallJpaEntity) {
    return new Hall(
        hallJpaEntity.getId(),
        toHallTypeDomain(hallJpaEntity.getHallType()),
        hallJpaEntity.getHallName(),
        hallJpaEntity.getMaxCapacity(),
        hallJpaEntity.getTablePrice(),
        hallJpaEntity.getStatus(),
        hallJpaEntity.getDescription());
  }

  private HallType toHallTypeDomain(HallTypeJpaEntity hallTypeJpaEntity) {
    return new HallType(
        hallTypeJpaEntity.getId(),
        hallTypeJpaEntity.getHallTypeName(),
        hallTypeJpaEntity.getMinimumTablePrice(),
        hallTypeJpaEntity.getDescription());
  }
}
