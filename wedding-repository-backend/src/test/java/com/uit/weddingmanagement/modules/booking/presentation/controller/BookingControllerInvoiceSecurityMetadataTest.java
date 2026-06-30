package com.uit.weddingmanagement.modules.booking.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class BookingControllerInvoiceSecurityMetadataTest {

  @Test
  void shouldRequireInvoicePermissionForInvoiceListAndDetail() throws NoSuchMethodException {
    Method searchInvoicesMethod =
        BookingController.class.getMethod(
            "searchInvoices", String.class, String.class, Long.class, LocalDate.class, int.class, int.class);
    Method getInvoiceDetailMethod =
        BookingController.class.getMethod("getInvoiceDetail", Long.class);

    PreAuthorize searchInvoicesAuthorize = searchInvoicesMethod.getAnnotation(PreAuthorize.class);
    PreAuthorize getInvoiceDetailAuthorize = getInvoiceDetailMethod.getAnnotation(PreAuthorize.class);

    assertThat(searchInvoicesAuthorize).isNotNull();
    assertThat(searchInvoicesAuthorize.value())
        .isEqualTo("@authorizationService.hasPermission('INVOICE_VIEW')");
    assertThat(getInvoiceDetailAuthorize).isNotNull();
    assertThat(getInvoiceDetailAuthorize.value())
        .isEqualTo("@authorizationService.hasPermission('INVOICE_VIEW')");
  }
}
