package com.uit.weddingmanagement.modules.catalog.domain.model;

import java.time.LocalTime;

// Domain model của ca tự giữ invariant cốt lõi để mọi luồng tạo/cập nhật đều dùng chung
// một bộ luật nghiệp vụ thống nhất.
public record Shift(
    Long id, String shiftName, LocalTime startTime, LocalTime endTime, String description) {

  public Shift {
    if (id != null && id <= 0) {
      throw new IllegalArgumentException("Shift id must be greater than 0.");
    }

    shiftName = normalizeShiftName(shiftName);
    startTime = requireStartTime(startTime);
    endTime = requireEndTime(endTime);
    ensureTimeRange(startTime, endTime);
    description = normalizeDescription(description);
  }

  public static Shift create(
      String shiftName, LocalTime startTime, LocalTime endTime, String description) {
    return new Shift(null, shiftName, startTime, endTime, description);
  }

  public Shift update(
      String shiftName, LocalTime startTime, LocalTime endTime, String description) {
    if (id == null) {
      throw new IllegalStateException("Cannot update a shift without id.");
    }

    return new Shift(id, shiftName, startTime, endTime, description);
  }

  public boolean overlapsWith(Shift otherShift) {
    if (otherShift == null) {
      throw new IllegalArgumentException("Other shift is required.");
    }

    return overlapsWith(otherShift.startTime(), otherShift.endTime());
  }

  // Kiểm tra xem khoảng thời gian của ca hiện tại có chồng lấn với khoảng thời gian được cung cấp hay không.
  // Logic kiểm tra overlap được xây dựng theo khoảng đóng [start, end], nghĩa là nếu ca A kết thúc đúng vào thời điểm ca B bắt đầu,
  // thì vẫn bị xem là chồng lấn, nhằm đảm bảo không có hai ca nào có thể xếp chồng lên nhau dù chỉ một chút thời gian.
  public boolean overlapsWith(LocalTime otherStartTime, LocalTime otherEndTime) {
    LocalTime normalizedOtherStartTime = requireStartTime(otherStartTime);
    LocalTime normalizedOtherEndTime = requireEndTime(otherEndTime);
    ensureTimeRange(normalizedOtherStartTime, normalizedOtherEndTime);

    // Plan đang chốt kiểm tra overlap theo khoảng đóng [start, end], vì vậy hai ca chạm nhau
    // đúng tại mốc giao biên vẫn bị xem là chồng giờ.
    return !normalizedOtherStartTime.isAfter(endTime)
        && !normalizedOtherEndTime.isBefore(startTime);
  }

  private static String normalizeShiftName(String shiftName) {
    if (shiftName == null || shiftName.isBlank()) {
      throw new IllegalArgumentException("Shift name is required.");
    }

    return shiftName.trim().replaceAll("\\s+", " ");
  }

  private static LocalTime requireStartTime(LocalTime startTime) {
    if (startTime == null) {
      throw new IllegalArgumentException("Shift start time is required.");
    }

    return startTime;
  }

  private static LocalTime requireEndTime(LocalTime endTime) {
    if (endTime == null) {
      throw new IllegalArgumentException("Shift end time is required.");
    }

    return endTime;
  }

  private static void ensureTimeRange(LocalTime startTime, LocalTime endTime) {
    if (!endTime.isAfter(startTime)) {
      throw new IllegalArgumentException("Shift end time must be after start time.");
    }
  }

  private static String normalizeDescription(String description) {
    if (description == null || description.isBlank()) {
      return null;
    }

    return description.trim();
  }
}
