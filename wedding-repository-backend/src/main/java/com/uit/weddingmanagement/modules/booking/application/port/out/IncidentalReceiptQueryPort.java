package com.uit.weddingmanagement.modules.booking.application.port.out;

import com.uit.weddingmanagement.modules.booking.domain.model.IncidentalReceipt;
import java.math.BigDecimal;
import java.util.List;

public interface IncidentalReceiptQueryPort {

  BigDecimal sumIncidentalReceiptTotalAmountByWeddingBookingId(Long bookingId);

  List<IncidentalReceipt> findIncidentalReceiptsByWeddingBookingId(Long bookingId);
}
