package com.uit.weddingmanagement.modules.booking.application.model.command;

import java.time.LocalDate;
import java.util.List;

public record CreateWeddingBookingCommand(
    Long hallId,
    Long shiftId,
    String groomName,
    String brideName,
    String groomPhoneNumber,
    String bridePhoneNumber,
    LocalDate celebrationDate,
    Integer tableCount,
    Integer reservedTableCount,
    String notes,
    List<CreateWeddingBookingMenuItemCommand> menuItems,
    List<CreateWeddingBookingServiceCommand> services,
    CreateDepositReceiptCommand depositReceipt) {}
