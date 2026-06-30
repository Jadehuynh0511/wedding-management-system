package com.uit.weddingmanagement.modules.catalog.presentation.mapper;

import com.uit.weddingmanagement.modules.catalog.application.model.result.HallTypeResult;
import com.uit.weddingmanagement.modules.catalog.presentation.dto.response.HallTypeResponse;

import org.springframework.stereotype.Component;

/**
 * Mapper chuyển đổi HallTypeResult sang HallTypeResponse cho presentation layer.
 */
@Component
public class HallTypePresentationMapper {

    public HallTypeResponse toResponse(HallTypeResult hallTypeResult) {
        return new HallTypeResponse(
                hallTypeResult.id(),
                hallTypeResult.hallTypeName(),
                hallTypeResult.minimumTablePrice(),
                hallTypeResult.description());
    }
}
