package com.uit.weddingmanagement.modules.booking.application.model.command;

public record CreateIncidentalReceiptItemCommand(Long serviceId, Integer quantity, String notes) {}
