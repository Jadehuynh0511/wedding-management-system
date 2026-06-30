package com.uit.weddingmanagement.modules.booking.application.model.command;

import java.util.List;

public record CreateIncidentalReceiptCommand(
    String notes, List<CreateIncidentalReceiptItemCommand> items) {}
