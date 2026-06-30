package com.uit.weddingmanagement.modules.catalog.application.port.out;

import com.uit.weddingmanagement.modules.catalog.domain.model.ServicePriceHistory;
import java.util.List;

public interface ServicePriceHistoryQueryPort {

  List<ServicePriceHistory> findServicePriceHistoriesByServiceItemId(Long serviceItemId);
}
