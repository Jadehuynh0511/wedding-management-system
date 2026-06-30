package com.uit.weddingmanagement.modules.catalog.application.port.in;

import com.uit.weddingmanagement.modules.catalog.application.model.result.ServiceItemDetailResult;

public interface GetServiceItemUseCase {

  ServiceItemDetailResult getServiceItem(Long serviceItemId);
}
