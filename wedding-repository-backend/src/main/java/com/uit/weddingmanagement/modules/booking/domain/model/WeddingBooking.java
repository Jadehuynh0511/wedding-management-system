package com.uit.weddingmanagement.modules.booking.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Aggregate này gom toàn bộ snapshot cần chốt tại lúc đặt tiệc: giá bàn, món,
// dịch vụ và phiếu cọc. Nhờ vậy application service chỉ còn phối hợp input và
// transaction, còn invariant nghiệp vụ sống tập trung ở một chỗ.
public record WeddingBooking(
    Long id,
    Long hallId,
    String hallName,
    Long shiftId,
    String shiftName,
    String groomName,
    String brideName,
    String groomPhoneNumber,
    String bridePhoneNumber,
    LocalDate bookingDate,
    LocalDate celebrationDate,
    Integer tableCount,
    Integer reservedTableCount,
    BigDecimal tablePrice,
    WeddingBookingStatus status,
    String notes,
    List<BookingMenuItem> menuItems,
    List<BookingService> services,
    DepositReceipt depositReceipt) {

  public WeddingBooking {
    if (id != null && id <= 0) {
      throw new IllegalArgumentException("Wedding booking id must be greater than 0.");
    }

    hallId = requirePositiveId(hallId, "Hall id is required.", "Hall id must be greater than 0.");
    hallName = normalizeRequiredText(hallName, "Hall name is required.");
    shiftId =
        requirePositiveId(shiftId, "Shift id is required.", "Shift id must be greater than 0.");
    shiftName = normalizeRequiredText(shiftName, "Shift name is required.");
    groomName = normalizeRequiredText(groomName, "Groom name is required.");
    brideName = normalizeRequiredText(brideName, "Bride name is required.");
    groomPhoneNumber = normalizeOptionalPhoneNumber(groomPhoneNumber);
    bridePhoneNumber = normalizeRequiredText(bridePhoneNumber, "Bride phone number is required.");
    bookingDate = requireBookingDate(bookingDate);
    celebrationDate = requireCelebrationDate(celebrationDate, bookingDate);
    tableCount =
        requirePositiveInteger(
            tableCount, "Table count is required.", "Table count must be greater than 0.");
    reservedTableCount =
        requireNonNegativeInteger(reservedTableCount, "Reserved table count is required.");
    tablePrice = requirePositiveAmount(tablePrice, "Table price is required.");
    status = requireStatus(status);
    notes = normalizeOptionalText(notes);
    menuItems = requireMenuItems(menuItems);
    services = normalizeServices(services);
    depositReceipt = requireDepositReceipt(depositReceipt);

    ensureDistinctMenuItems(menuItems);
    ensureDistinctServices(services);
  }

  public static WeddingBooking create(
      Long hallId,
      String hallName,
      Long shiftId,
      String shiftName,
      String groomName,
      String brideName,
      String groomPhoneNumber,
      String bridePhoneNumber,
      LocalDate bookingDate,
      LocalDate celebrationDate,
      Integer tableCount,
      Integer reservedTableCount,
      BigDecimal tablePrice,
      String notes,
      List<BookingMenuItem> menuItems,
      List<BookingService> services,
      DepositReceipt depositReceipt) {
    // M3 chưa có trạng thái "CHỜ THANH TOÁN" riêng trong schema, nên booking
    // mới được chốt về DA_XAC_NHAN để thể hiện đây là tiệc đã nhận cọc thành công.
    return new WeddingBooking(
        null,
        hallId,
        hallName,
        shiftId,
        shiftName,
        groomName,
        brideName,
        groomPhoneNumber,
        bridePhoneNumber,
        bookingDate,
        celebrationDate,
        tableCount,
        reservedTableCount,
        tablePrice,
        WeddingBookingStatus.DA_XAC_NHAN,
        notes,
        menuItems,
        services,
        depositReceipt);
  }

  public BigDecimal calculateHallTotalAmount() {
    return tablePrice.multiply(BigDecimal.valueOf(tableCount.longValue()));
  }

  public BigDecimal calculateMenuItemsTotalAmount() {
    return menuItems.stream()
        .map(menuItem -> menuItem.calculateBookingLineTotal(tableCount))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public BigDecimal calculateServicesTotalAmount() {
    return services.stream()
        .map(BookingService::lineTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public WeddingBooking markAsPaid() {
    if (status == WeddingBookingStatus.DA_THANH_TOAN) {
      throw new IllegalArgumentException("Wedding booking is already fully paid.");
    }

    if (status == WeddingBookingStatus.DA_HUY) {
      throw new IllegalArgumentException("Cancelled wedding booking cannot be marked as paid.");
    }

    return new WeddingBooking(
        id,
        hallId,
        hallName,
        shiftId,
        shiftName,
        groomName,
        brideName,
        groomPhoneNumber,
        bridePhoneNumber,
        bookingDate,
        celebrationDate,
        tableCount,
        reservedTableCount,
        tablePrice,
        WeddingBookingStatus.DA_THANH_TOAN,
        notes,
        menuItems,
        services,
        depositReceipt);
  }

  public WeddingBooking markAsCancelled() {
    if (status == WeddingBookingStatus.DA_HUY) {
      throw new IllegalArgumentException("Wedding booking is already cancelled.");
    }

    if (status == WeddingBookingStatus.DA_THANH_TOAN) {
      throw new IllegalArgumentException("Fully paid wedding booking cannot be cancelled.");
    }

    return new WeddingBooking(
        id,
        hallId,
        hallName,
        shiftId,
        shiftName,
        groomName,
        brideName,
        groomPhoneNumber,
        bridePhoneNumber,
        bookingDate,
        celebrationDate,
        tableCount,
        reservedTableCount,
        tablePrice,
        WeddingBookingStatus.DA_HUY,
        notes,
        menuItems,
        services,
        depositReceipt);
  }

  private static Long requirePositiveId(
      Long value, String nullMessage, String negativeMessage) {
    if (value == null) {
      throw new IllegalArgumentException(nullMessage);
    }

    if (value <= 0) {
      throw new IllegalArgumentException(negativeMessage);
    }

    return value;
  }

  private static String normalizeRequiredText(String value, String message) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(message);
    }

    return value.trim().replaceAll("\\s+", " ");
  }

  private static String normalizeOptionalText(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return value.trim();
  }

  private static String normalizeOptionalPhoneNumber(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }

    return value.trim();
  }

  private static LocalDate requireBookingDate(LocalDate bookingDate) {
    if (bookingDate == null) {
      throw new IllegalArgumentException("Booking date is required.");
    }

    return bookingDate;
  }

  private static LocalDate requireCelebrationDate(LocalDate celebrationDate, LocalDate bookingDate) {
    if (celebrationDate == null) {
      throw new IllegalArgumentException("Celebration date is required.");
    }

    if (celebrationDate.isBefore(bookingDate)) {
      throw new IllegalArgumentException("Celebration date must be on or after booking date.");
    }

    return celebrationDate;
  }

  private static Integer requirePositiveInteger(
      Integer value, String nullMessage, String negativeMessage) {
    if (value == null) {
      throw new IllegalArgumentException(nullMessage);
    }

    if (value <= 0) {
      throw new IllegalArgumentException(negativeMessage);
    }

    return value;
  }

  private static Integer requireNonNegativeInteger(Integer value, String message) {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }

    if (value < 0) {
      throw new IllegalArgumentException("Reserved table count must be greater than or equal to 0.");
    }

    return value;
  }

  private static BigDecimal requirePositiveAmount(BigDecimal value, String message) {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }

    if (value.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Table price must be greater than 0.");
    }

    return value;
  }

  private static WeddingBookingStatus requireStatus(WeddingBookingStatus status) {
    if (status == null) {
      throw new IllegalArgumentException("Wedding booking status is required.");
    }

    return status;
  }

  private static List<BookingMenuItem> requireMenuItems(List<BookingMenuItem> menuItems) {
    if (menuItems == null || menuItems.isEmpty()) {
      throw new IllegalArgumentException("At least one menu item is required.");
    }

    return List.copyOf(menuItems);
  }

  private static List<BookingService> normalizeServices(List<BookingService> services) {
    if (services == null || services.isEmpty()) {
      return List.of();
    }

    return List.copyOf(services);
  }

  private static DepositReceipt requireDepositReceipt(DepositReceipt depositReceipt) {
    if (depositReceipt == null) {
      throw new IllegalArgumentException("Deposit receipt is required.");
    }

    return depositReceipt;
  }

  private static void ensureDistinctMenuItems(List<BookingMenuItem> menuItems) {
    Set<Long> menuItemIds = new LinkedHashSet<>();
    for (BookingMenuItem menuItem : menuItems) {
      if (!menuItemIds.add(menuItem.menuItemId())) {
        throw new IllegalArgumentException(
            "Duplicate menu item is not allowed in a single wedding booking.");
      }
    }
  }

  private static void ensureDistinctServices(List<BookingService> services) {
    Set<Long> serviceIds = new LinkedHashSet<>();
    for (BookingService service : services) {
      if (!serviceIds.add(service.serviceId())) {
        throw new IllegalArgumentException(
            "Duplicate service is not allowed in a single wedding booking.");
      }
    }
  }
}
