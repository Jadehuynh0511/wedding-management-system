package com.uit.weddingmanagement.modules.booking.application.port.in;

import com.uit.weddingmanagement.modules.booking.application.model.result.AvailableHallResult;
import java.time.LocalDate;
import java.util.List;

public interface ListAvailableHallsUseCase {

  List<AvailableHallResult> listAvailableHalls(LocalDate celebrationDate, Long shiftId);
}
