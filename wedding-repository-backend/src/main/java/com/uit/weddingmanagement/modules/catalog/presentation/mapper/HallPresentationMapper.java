package com.uit.weddingmanagement.modules.catalog.presentation.mapper;

import com.uit.weddingmanagement.modules.catalog.application.model.result.HallResult;
import com.uit.weddingmanagement.modules.catalog.presentation.dto.response.HallResponse;

import org.springframework.stereotype.Component;

/**
 * Mapper chuyển đổi HallResult sang HallResponse cho presentation layer.
 */
@Component
public class HallPresentationMapper {

    public HallResponse toResponse(HallResult hallResult) {
        return new HallResponse(
                hallResult.id(),
                hallResult.hallTypeId(),
                hallResult.hallTypeName(),
                hallResult.minimumTablePrice(),
                hallResult.hallName(),
                hallResult.maxCapacity(),
                hallResult.tablePrice(),
                hallResult.status(),
                hallResult.description());
    }
}
