package com.uit.weddingmanagement.modules.catalog.application.port.in;

import com.uit.weddingmanagement.modules.catalog.application.model.command.CreateServiceItemCommand;
import com.uit.weddingmanagement.modules.catalog.application.model.result.ServiceItemResult;

public interface CreateServiceItemUseCase {

  ServiceItemResult createServiceItem(CreateServiceItemCommand command);
}
