package com.uit.weddingmanagement.modules.booking.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.uit.weddingmanagement.modules.booking.application.port.in.SearchWeddingBookingsUseCase;
import com.uit.weddingmanagement.modules.booking.application.port.out.WeddingBookingQueryPort;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingStatus;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingSummary;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class SearchWeddingBookingsServiceTest {

  @Mock private WeddingBookingQueryPort weddingBookingQueryPort;

  @Test
  void shouldSearchWeddingBookingsWithNormalizedFilters() {
    when(weddingBookingQueryPort.searchWeddingBookings(
            "Minh",
            "Lan",
            7L,
            LocalDate.of(2026, 8, 15),
            WeddingBookingStatus.DA_XAC_NHAN,
            PageRequest.of(0, 20)))
        .thenReturn(
            new PageImpl<>(
                List.of(
                    new WeddingBookingSummary(
                        44L,
                        7L,
                        "Sunrise Hall",
                        2L,
                        "Evening",
                        "Minh",
                        "Lan",
                        LocalDate.of(2026, 8, 15),
                        20,
                        WeddingBookingStatus.DA_XAC_NHAN))));

    SearchWeddingBookingsService searchWeddingBookingsService =
        new SearchWeddingBookingsService(weddingBookingQueryPort);

    var resultPage =
        searchWeddingBookingsService.searchWeddingBookings(
            new SearchWeddingBookingsUseCase.SearchWeddingBookingsQuery(
                "  Minh   ",
                "  Lan  ",
                7L,
                LocalDate.of(2026, 8, 15),
                WeddingBookingStatus.DA_XAC_NHAN,
                PageRequest.of(0, 20)));

    assertThat(resultPage.getContent()).hasSize(1);
    assertThat(resultPage.getContent().getFirst().coupleName()).isEqualTo("Minh & Lan");
    assertThat(resultPage.getContent().getFirst().hallName()).isEqualTo("Sunrise Hall");
  }

  @Test
  void shouldRejectWhenHallIdIsNotPositive() {
    SearchWeddingBookingsService searchWeddingBookingsService =
        new SearchWeddingBookingsService(weddingBookingQueryPort);

    assertThatThrownBy(
            () ->
                searchWeddingBookingsService.searchWeddingBookings(
                    new SearchWeddingBookingsUseCase.SearchWeddingBookingsQuery(
                        null,
                        null,
                        0L,
                        null,
                        null,
                        PageRequest.of(0, 20))))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Hall id must be greater than 0.");
  }
}
