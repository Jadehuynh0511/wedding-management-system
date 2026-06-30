package com.uit.weddingmanagement.modules.booking.domain.model;

// Giữ đúng tập trạng thái đang được schema hỗ trợ để M3 dùng ngay, đồng thời
// không khóa cửa cho các luồng M4/M5 dùng lại sau này.
public enum WeddingBookingStatus {
  CHO_XAC_NHAN,
  DA_XAC_NHAN,
  DANG_DIEN_RA,
  DA_THANH_TOAN,
  DA_HUY
}
