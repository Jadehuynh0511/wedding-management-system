package com.uit.weddingmanagement.modules.catalog.application.usecase;

import com.uit.weddingmanagement.modules.catalog.application.model.result.ShiftResult;
import com.uit.weddingmanagement.modules.catalog.application.port.in.ListShiftsUseCase;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ShiftQueryPort;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListShiftsService implements ListShiftsUseCase {

  private final ShiftQueryPort shiftQueryPort;

  public ListShiftsService(ShiftQueryPort shiftQueryPort) {
    this.shiftQueryPort = shiftQueryPort;
  }

  @Override
  public List<ShiftResult> listShifts() {
    return shiftQueryPort.findAllShifts().stream().map(ShiftResult::from).toList();
  }
}
