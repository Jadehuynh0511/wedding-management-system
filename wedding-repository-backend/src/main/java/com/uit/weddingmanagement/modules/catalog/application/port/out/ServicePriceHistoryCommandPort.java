package com.uit.weddingmanagement.modules.catalog.application.port.out;

import com.uit.weddingmanagement.modules.catalog.domain.model.ServicePriceHistory;

public interface ServicePriceHistoryCommandPort {

  ServicePriceHistory saveServicePriceHistory(ServicePriceHistory servicePriceHistory);
}
