package com.uit.weddingmanagement.modules.catalog.application.usecase;

import com.uit.weddingmanagement.modules.catalog.application.model.result.HallResult;
import com.uit.weddingmanagement.modules.catalog.application.port.in.GetHallUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallQueryPort;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class GetHallService implements GetHallUseCase {

  private final HallQueryPort hallQueryPort;

  public GetHallService(HallQueryPort hallQueryPort) {
    this.hallQueryPort = hallQueryPort;
  }

  @Override
  public HallResult getHall(Long hallId) {
    Long normalizedHallId = requireHallId(hallId);

    return hallQueryPort
        .findHallById(normalizedHallId)
        .map(HallResult::from)
        .orElseThrow(
            () -> new EntityNotFoundException("Hall not found with id: " + normalizedHallId));
  }

  private Long requireHallId(Long hallId) {
    if (hallId == null) {
      throw new IllegalArgumentException("Hall id is required.");
    }

    return hallId;
  }
}
