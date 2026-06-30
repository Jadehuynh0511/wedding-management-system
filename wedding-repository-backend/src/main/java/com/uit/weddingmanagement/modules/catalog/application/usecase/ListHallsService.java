package com.uit.weddingmanagement.modules.catalog.application.usecase;

import com.uit.weddingmanagement.modules.catalog.application.model.result.HallResult;
import com.uit.weddingmanagement.modules.catalog.application.port.in.ListHallsUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallQueryPort;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListHallsService implements ListHallsUseCase {

  private final HallQueryPort hallQueryPort;

  public ListHallsService(HallQueryPort hallQueryPort) {
    this.hallQueryPort = hallQueryPort;
  }

  @Override
  public List<HallResult> listHalls() {
    return hallQueryPort.findAllHalls().stream().map(HallResult::from).toList();
  }
}
