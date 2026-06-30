package com.uit.weddingmanagement.modules.catalog.application.port.in;

import com.uit.weddingmanagement.modules.catalog.application.model.result.HallResult;
import java.util.List;

public interface ListHallsUseCase {

  List<HallResult> listHalls();
}
