package com.uit.weddingmanagement.modules.booking.application.model.command;

public record CreateWeddingBookingServiceCommand(Long serviceId, Integer quantity, String notes) {}
