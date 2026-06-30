package com.uit.weddingmanagement.modules.system.infrastructure.persistence;

import com.uit.weddingmanagement.modules.system.application.port.out.SystemParameterCommandPort;
import com.uit.weddingmanagement.modules.system.application.port.out.SystemParameterQueryPort;
import com.uit.weddingmanagement.modules.system.domain.model.SystemParameter;
import com.uit.weddingmanagement.modules.system.infrastructure.persistence.entity.SystemParameterJpaEntity;
import com.uit.weddingmanagement.modules.system.infrastructure.persistence.repository.SystemParameterJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class SystemParameterPersistenceAdapter
    implements SystemParameterQueryPort, SystemParameterCommandPort {

  private final SystemParameterJpaRepository systemParameterJpaRepository;

  public SystemParameterPersistenceAdapter(
      SystemParameterJpaRepository systemParameterJpaRepository) {
    this.systemParameterJpaRepository = systemParameterJpaRepository;
  }

  @Override
  public Optional<SystemParameter> getSystemParameter() {
    // Bảng system_parameters là singleton row id=1, nên adapter luôn đọc đúng bản ghi này
    // thay vì expose logic query linh tinh ra tầng use case.
    return systemParameterJpaRepository.findById(SystemParameter.SINGLETON_ID).map(this::toDomain);
  }

  @Override
  public SystemParameter saveSystemParameter(SystemParameter systemParameter) {
    SystemParameterJpaEntity systemParameterJpaEntity =
        systemParameterJpaRepository
            .findById(SystemParameter.SINGLETON_ID)
            .orElseThrow(
                () -> new EntityNotFoundException("System settings have not been initialized."));

    systemParameterJpaEntity.setMinimumDepositPercentage(
        systemParameter.minimumDepositPercentage());
    systemParameterJpaEntity.setLatePaymentPenaltyEnabled(
        systemParameter.latePaymentPenaltyEnabled());
    systemParameterJpaEntity.setLatePaymentPenaltyRate(systemParameter.latePaymentPenaltyRate());
    systemParameterJpaEntity.setCancellationDeadlineDays(
        systemParameter.cancellationDeadlineDays());
    systemParameterJpaEntity.setDepositRefundPercentage(systemParameter.depositRefundPercentage());

    return toDomain(systemParameterJpaRepository.save(systemParameterJpaEntity));
  }

  private SystemParameter toDomain(SystemParameterJpaEntity systemParameterJpaEntity) {
    return new SystemParameter(
        systemParameterJpaEntity.getId(),
        systemParameterJpaEntity.getMinimumDepositPercentage(),
        systemParameterJpaEntity.isLatePaymentPenaltyEnabled(),
        systemParameterJpaEntity.getLatePaymentPenaltyRate(),
        systemParameterJpaEntity.getCancellationDeadlineDays(),
        systemParameterJpaEntity.getDepositRefundPercentage());
  }
}
