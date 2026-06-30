package com.uit.weddingmanagement.modules.booking.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.time.LocalDate;
import java.util.List;

public record CreateWeddingBookingRequest(
    @NotNull(message = "Hall id is required.")
        @Positive(message = "Hall id must be greater than 0.")
        Long hallId,
    @NotNull(message = "Shift id is required.")
        @Positive(message = "Shift id must be greater than 0.")
        Long shiftId,
    @NotBlank(message = "Groom name is required.") String groomName,
    @NotBlank(message = "Bride name is required.") String brideName,
    String groomPhoneNumber,
    @NotBlank(message = "Bride phone number is required.") String bridePhoneNumber,
    @NotNull(message = "Celebration date is required.") LocalDate celebrationDate,
    @NotNull(message = "Table count is required.")
        @Positive(message = "Table count must be greater than 0.")
        Integer tableCount,
    @PositiveOrZero(message = "Reserved table count must be greater than or equal to 0.")
        Integer reservedTableCount,
    String notes,
    @NotEmpty(message = "At least one menu item is required.")
        List<@Valid CreateWeddingBookingMenuItemRequest> menuItems,
    List<@Valid CreateWeddingBookingServiceRequest> services,
    @NotNull(message = "Deposit receipt is required.") @Valid CreateDepositReceiptRequest depositReceipt) {}
