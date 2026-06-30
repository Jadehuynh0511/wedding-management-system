package com.uit.weddingmanagement.modules.report.presentation.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class MonthlyRevenueReportControllerSecurityMetadataTest {

  @Test
  void shouldRequireMonthlyRevenueReportPermissionFromCurrentRbacCatalog()
      throws NoSuchMethodException {
    Method method =
        MonthlyRevenueReportController.class.getMethod(
            "getMonthlyRevenueReport", int.class, int.class);

    PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

    assertThat(preAuthorize).isNotNull();
    assertThat(preAuthorize.value())
        .isEqualTo(
            "@authorizationService.hasPermission('MONTHLY_REVENUE_REPORT_GENERATE')");
  }
}
