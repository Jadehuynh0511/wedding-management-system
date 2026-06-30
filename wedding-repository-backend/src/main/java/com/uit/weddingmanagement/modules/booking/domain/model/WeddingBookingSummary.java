package com.uit.weddingmanagement.modules.booking.domain.model;

import java.time.LocalDate;

// Read model gọn cho BM3 để trang tra cứu chỉ lấy đúng dữ liệu cần hiển thị,
// tránh kéo cả aggregate booking detail mỗi lần lọc/phân trang.
public record WeddingBookingSummary(
    Long id,
    Long hallId,
    String hallName,
    Long shiftId,
    String shiftName,
    String groomName,
    String brideName,
    LocalDate celebrationDate,
    Integer tableCount,
    WeddingBookingStatus status) {

  public String coupleName() {
    return groomName + " & " + brideName;
  }
}
