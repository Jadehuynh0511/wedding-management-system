package com.uit.weddingmanagement.modules.booking.application.model.command;

public record CreateWeddingBookingMenuItemCommand(Long menuItemId, Integer quantity, String notes) {}
