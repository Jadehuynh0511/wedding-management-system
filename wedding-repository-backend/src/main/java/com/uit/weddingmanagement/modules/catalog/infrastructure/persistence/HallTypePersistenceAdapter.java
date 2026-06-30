package com.uit.weddingmanagement.modules.catalog.infrastructure.persistence;

import com.uit.weddingmanagement.modules.catalog.application.port.out.HallReferenceQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallTypeCommandPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallTypeQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.HallType;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity.HallTypeJpaEntity;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.repository.HallJpaRepository;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.repository.HallTypeJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

// @Component này đánh dấu class này là một Spring Bean,
// cho phép Spring tự động phát hiện và quản lý vòng đời của nó.
@Component
public class HallTypePersistenceAdapter
    implements HallTypeQueryPort, HallTypeCommandPort, HallReferenceQueryPort {

  private final HallTypeJpaRepository hallTypeJpaRepository;
  private final HallJpaRepository hallJpaRepository;

  public HallTypePersistenceAdapter(
      HallTypeJpaRepository hallTypeJpaRepository, HallJpaRepository hallJpaRepository) {
    this.hallTypeJpaRepository = hallTypeJpaRepository;
    this.hallJpaRepository = hallJpaRepository;
  }

  @Override
  public List<HallType> findAllHallTypes() {
    return hallTypeJpaRepository.findAllByOrderByIdAsc().stream().map(this::toDomain).toList();
  }

  @Override
  public Optional<HallType> findHallTypeById(Long hallTypeId) {
    return hallTypeJpaRepository.findById(hallTypeId).map(this::toDomain);
  }

  @Override
  public boolean existsHallTypeByName(String hallTypeName) {
    return hallTypeJpaRepository.existsByHallTypeNameIgnoreCase(hallTypeName);
  }

  @Override
  public boolean existsHallTypeByNameAndIdNot(String hallTypeName, Long hallTypeId) {
    return hallTypeJpaRepository.existsByHallTypeNameIgnoreCaseAndIdNot(hallTypeName, hallTypeId);
  }

  @Override
  public HallType saveHallType(HallType hallType) {
    HallTypeJpaEntity hallTypeJpaEntity = resolveEntityForSave(hallType);
    hallTypeJpaEntity.setHallTypeName(hallType.hallTypeName());
    hallTypeJpaEntity.setMinimumTablePrice(hallType.minimumTablePrice());
    hallTypeJpaEntity.setDescription(hallType.description());

    return toDomain(hallTypeJpaRepository.save(hallTypeJpaEntity));
  }

  @Override
  public void deleteHallTypeById(Long hallTypeId) {
    hallTypeJpaRepository.deleteById(hallTypeId);
  }

  @Override
  public boolean existsHallByHallTypeId(Long hallTypeId) {
    return hallJpaRepository.existsByHallType_Id(hallTypeId);
  }

  private HallTypeJpaEntity resolveEntityForSave(HallType hallType) {
    if (hallType.id() == null) {
      return new HallTypeJpaEntity();
    }

    return hallTypeJpaRepository
        .findById(hallType.id())
        .orElseThrow(
            () -> new EntityNotFoundException("Hall type not found with id: " + hallType.id()));
  }

  private HallType toDomain(HallTypeJpaEntity hallTypeJpaEntity) {
    return new HallType(
        hallTypeJpaEntity.getId(),
        hallTypeJpaEntity.getHallTypeName(),
        hallTypeJpaEntity.getMinimumTablePrice(),
        hallTypeJpaEntity.getDescription());
  }
}
