package com.uit.weddingmanagement.modules.catalog.application.port.in;

import com.uit.weddingmanagement.modules.catalog.application.model.result.HallTypeResult;
import java.util.List;

public interface ListHallTypesUseCase {

  List<HallTypeResult> listHallTypes();
}
