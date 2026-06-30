package com.uit.weddingmanagement.modules.catalog.application.port.in;

import com.uit.weddingmanagement.modules.catalog.application.model.result.ServiceItemDetailResult;
import com.uit.weddingmanagement.modules.catalog.application.model.command.UpdateServiceItemPriceCommand;

public interface UpdateServiceItemPriceUseCase {

  ServiceItemDetailResult updateServiceItemPrice(
      Long serviceItemId, UpdateServiceItemPriceCommand command);
}
