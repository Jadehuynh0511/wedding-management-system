package com.uit.weddingmanagement.modules.catalog.application.port.in;

import com.uit.weddingmanagement.modules.catalog.application.model.result.ServiceItemResult;
import java.util.List;

public interface ListServiceItemsUseCase {

  List<ServiceItemResult> listServiceItems(Boolean active);
}
