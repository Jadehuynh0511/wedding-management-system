package com.uit.weddingmanagement.modules.catalog.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public record ShiftResponse(
    Long id,
    String shiftName,
    @JsonFormat(pattern = "HH:mm") LocalTime startTime,
    @JsonFormat(pattern = "HH:mm") LocalTime endTime,
    String description) {}
