package com.uit.weddingmanagement.modules.catalog.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record CreateShiftRequest(
    @NotBlank(message = "Shift name is required.") String shiftName,
    @NotNull(message = "Shift start time is required.")
        @JsonFormat(pattern = "HH:mm")
        LocalTime startTime,
    @NotNull(message = "Shift end time is required.")
        @JsonFormat(pattern = "HH:mm")
        LocalTime endTime,
    String description) {}
