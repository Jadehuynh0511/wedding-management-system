package com.uit.weddingmanagement.modules.catalog.application.usecase;

import com.uit.weddingmanagement.modules.catalog.application.model.result.HallTypeResult;
import com.uit.weddingmanagement.modules.catalog.application.port.in.ListHallTypesUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallTypeQueryPort;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListHallTypesService implements ListHallTypesUseCase {

  private final HallTypeQueryPort hallTypeQueryPort;

  public ListHallTypesService(HallTypeQueryPort hallTypeQueryPort) {
    this.hallTypeQueryPort = hallTypeQueryPort;
  }

  @Override
  public List<HallTypeResult> listHallTypes() {
    return hallTypeQueryPort.findAllHallTypes().stream().map(HallTypeResult::from).toList();
  }
}
