package com.uit.weddingmanagement.modules.catalog.application.usecase;

import com.uit.weddingmanagement.modules.catalog.application.model.result.HallTypeResult;
import com.uit.weddingmanagement.modules.catalog.application.port.in.GetHallTypeUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallTypeQueryPort;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetHallTypeService implements GetHallTypeUseCase {

  private final HallTypeQueryPort hallTypeQueryPort;

  public GetHallTypeService(HallTypeQueryPort hallTypeQueryPort) {
    this.hallTypeQueryPort = hallTypeQueryPort;
  }

  @Override
  public HallTypeResult getHallType(Long hallTypeId) {
    Long normalizedHallTypeId = requireHallTypeId(hallTypeId);

    return hallTypeQueryPort
        .findHallTypeById(normalizedHallTypeId)
        .map(HallTypeResult::from)
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "Hall type not found with id: " + normalizedHallTypeId));
  }

  private Long requireHallTypeId(Long hallTypeId) {
    if (hallTypeId == null) {
      throw new IllegalArgumentException("Hall type id is required.");
    }

    return hallTypeId;
  }
}
