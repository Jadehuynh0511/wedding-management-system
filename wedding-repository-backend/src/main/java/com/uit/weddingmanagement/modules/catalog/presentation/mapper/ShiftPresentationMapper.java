package com.uit.weddingmanagement.modules.catalog.presentation.mapper;

import com.uit.weddingmanagement.modules.catalog.application.model.result.ShiftResult;
import com.uit.weddingmanagement.modules.catalog.presentation.dto.response.ShiftResponse;

import org.springframework.stereotype.Component;

/**
 * Mapper chuyển đổi ShiftResult sang ShiftResponse cho presentation layer.
 */
@Component
public class ShiftPresentationMapper {

    public ShiftResponse toResponse(ShiftResult shiftResult) {
        return new ShiftResponse(
                shiftResult.id(),
                shiftResult.shiftName(),
                shiftResult.startTime(),
                shiftResult.endTime(),
                shiftResult.description());
    }
}
