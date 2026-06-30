package com.uit.weddingmanagement.modules.catalog.application.port.in;

import com.uit.weddingmanagement.modules.catalog.application.model.result.ShiftResult;
import java.util.List;

public interface ListShiftsUseCase {

  List<ShiftResult> listShifts();
}
