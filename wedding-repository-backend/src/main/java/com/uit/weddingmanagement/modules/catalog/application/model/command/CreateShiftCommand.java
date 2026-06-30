package com.uit.weddingmanagement.modules.catalog.application.model.command;

import java.time.LocalTime;

public record CreateShiftCommand(
    String shiftName, LocalTime startTime, LocalTime endTime, String description) {}
