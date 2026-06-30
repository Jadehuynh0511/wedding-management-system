package com.uit.weddingmanagement.modules.catalog.application.port.in;

import com.uit.weddingmanagement.modules.catalog.application.model.result.ServiceItemResult;
import com.uit.weddingmanagement.modules.catalog.application.model.command.UpdateServiceItemCommand;

public interface UpdateServiceItemUseCase {

  ServiceItemResult updateServiceItem(Long serviceItemId, UpdateServiceItemCommand command);
}
