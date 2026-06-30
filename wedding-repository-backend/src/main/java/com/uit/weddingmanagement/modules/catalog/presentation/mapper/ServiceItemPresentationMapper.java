package com.uit.weddingmanagement.modules.catalog.presentation.mapper;

import com.uit.weddingmanagement.modules.catalog.application.model.result.ServiceItemDetailResult;
import com.uit.weddingmanagement.modules.catalog.application.model.result.ServiceItemResult;
import com.uit.weddingmanagement.modules.catalog.application.model.result.ServicePriceHistoryResult;
import com.uit.weddingmanagement.modules.catalog.presentation.dto.response.ServiceItemDetailResponse;
import com.uit.weddingmanagement.modules.catalog.presentation.dto.response.ServiceItemResponse;
import com.uit.weddingmanagement.modules.catalog.presentation.dto.response.ServicePriceHistoryResponse;

import org.springframework.stereotype.Component;

/**
 * Mapper chuyển đổi ServiceItem Result objects sang Response DTOs cho presentation layer.
 */
@Component
public class ServiceItemPresentationMapper {

    public ServiceItemResponse toResponse(ServiceItemResult serviceItemResult) {
        return new ServiceItemResponse(
                serviceItemResult.id(),
                serviceItemResult.serviceName(),
                serviceItemResult.serviceCategory(),
                serviceItemResult.unitName(),
                serviceItemResult.currentPrice(),
                serviceItemResult.priceEffectiveFrom(),
                serviceItemResult.status(),
                serviceItemResult.active(),
                serviceItemResult.description());
    }

    public ServiceItemDetailResponse toDetailResponse(ServiceItemDetailResult serviceItemDetailResult) {
        return new ServiceItemDetailResponse(
                serviceItemDetailResult.id(),
                serviceItemDetailResult.serviceName(),
                serviceItemDetailResult.serviceCategory(),
                serviceItemDetailResult.unitName(),
                serviceItemDetailResult.currentPrice(),
                serviceItemDetailResult.priceEffectiveFrom(),
                serviceItemDetailResult.status(),
                serviceItemDetailResult.active(),
                serviceItemDetailResult.description(),
                serviceItemDetailResult.priceHistory().stream()
                        .map(this::toPriceHistoryResponse)
                        .toList());
    }

    public ServicePriceHistoryResponse toPriceHistoryResponse(ServicePriceHistoryResult result) {
        return new ServicePriceHistoryResponse(
                result.id(),
                result.serviceItemId(),
                result.oldPrice(),
                result.effectiveFrom(),
                result.effectiveTo());
    }
}
