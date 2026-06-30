package com.uit.weddingmanagement.modules.booking.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.modules.booking.application.port.out.WeddingBookingQueryPort;
import com.uit.weddingmanagement.modules.booking.domain.model.BookingMenuItem;
import com.uit.weddingmanagement.modules.booking.domain.model.DepositReceipt;
import com.uit.weddingmanagement.modules.booking.domain.model.PaymentMethod;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBooking;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingStatus;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetWeddingBookingDetailServiceTest {

  @Mock private WeddingBookingQueryPort weddingBookingQueryPort;

  @Test
  void shouldReturnWeddingBookingDetailWhenBookingExists() {
    when(weddingBookingQueryPort.findWeddingBookingById(44L))
        .thenReturn(
            Optional.of(
                new WeddingBooking(
                    44L,
                    7L,
                    "Sunrise Hall",
                    2L,
                    "Evening",
                    "Minh",
                    "Lan",
                    "0909000111",
                    "0909000222",
                    LocalDate.of(2026, 6, 1),
                    LocalDate.of(2026, 8, 15),
                    20,
                    2,
                    new BigDecimal("5000000.00"),
                    WeddingBookingStatus.DA_XAC_NHAN,
                    "Ban gan san khau",
                    List.of(
                        BookingMenuItem.create(
                            11L, "Soup cua", 1, new BigDecimal("150000.00"), null)),
                    List.of(),
                    new DepositReceipt(
                        55L,
                        44L,
                        99L,
                        Instant.parse("2026-06-01T08:00:00Z"),
                        new BigDecimal("50000000.00"),
                        PaymentMethod.CHUYEN_KHOAN,
                        null))));

    GetWeddingBookingDetailService getWeddingBookingDetailService =
        new GetWeddingBookingDetailService(weddingBookingQueryPort);

    var result = getWeddingBookingDetailService.getWeddingBookingDetail(44L);

    assertThat(result.id()).isEqualTo(44L);
    assertThat(result.hallName()).isEqualTo("Sunrise Hall");
    assertThat(result.depositReceipt().amount()).isEqualByComparingTo("50000000.00");
  }

  @Test
  void shouldThrowNotFoundWhenBookingDoesNotExist() {
    when(weddingBookingQueryPort.findWeddingBookingById(44L)).thenReturn(Optional.empty());

    GetWeddingBookingDetailService getWeddingBookingDetailService =
        new GetWeddingBookingDetailService(weddingBookingQueryPort);

    assertThatThrownBy(() -> getWeddingBookingDetailService.getWeddingBookingDetail(44L))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Wedding booking not found with id: 44");
  }
}
