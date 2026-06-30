package com.uit.weddingmanagement.modules.booking.application.port.out;

import com.uit.weddingmanagement.modules.booking.domain.model.IncidentalReceipt;

public interface IncidentalReceiptCommandPort {

  IncidentalReceipt saveIncidentalReceipt(IncidentalReceipt incidentalReceipt);
}
