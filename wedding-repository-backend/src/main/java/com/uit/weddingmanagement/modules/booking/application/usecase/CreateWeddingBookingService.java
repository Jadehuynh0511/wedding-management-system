package com.uit.weddingmanagement.modules.booking.application.usecase;

import com.uit.weddingmanagement.common.exception.DuplicateResourceException;
import com.uit.weddingmanagement.modules.audit.infrastructure.aspect.AuditAction;
import com.uit.weddingmanagement.modules.auth.application.port.out.CurrentUserPort;
import com.uit.weddingmanagement.modules.auth.domain.model.AuthenticatedUser;
import com.uit.weddingmanagement.modules.booking.application.model.command.CreateDepositReceiptCommand;
import com.uit.weddingmanagement.modules.booking.application.model.command.CreateWeddingBookingCommand;
import com.uit.weddingmanagement.modules.booking.application.model.command.CreateWeddingBookingMenuItemCommand;
import com.uit.weddingmanagement.modules.booking.application.model.command.CreateWeddingBookingServiceCommand;
import com.uit.weddingmanagement.modules.booking.application.model.result.WeddingBookingDetailResult;
import com.uit.weddingmanagement.modules.booking.application.port.in.CreateWeddingBookingUseCase;
import com.uit.weddingmanagement.modules.booking.application.port.out.WeddingBookingCommandPort;
import com.uit.weddingmanagement.modules.booking.application.port.out.WeddingBookingQueryPort;
import com.uit.weddingmanagement.modules.booking.domain.model.BookingMenuItem;
import com.uit.weddingmanagement.modules.booking.domain.model.BookingService;
import com.uit.weddingmanagement.modules.booking.domain.model.DepositReceipt;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBooking;
import com.uit.weddingmanagement.modules.catalog.application.port.out.HallQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.MenuItemQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ServiceItemQueryPort;
import com.uit.weddingmanagement.modules.catalog.application.port.out.ShiftQueryPort;
import com.uit.weddingmanagement.modules.catalog.domain.model.Hall;
import com.uit.weddingmanagement.modules.catalog.domain.model.HallStatus;
import com.uit.weddingmanagement.modules.catalog.domain.model.MenuItem;
import com.uit.weddingmanagement.modules.catalog.domain.model.ServiceItem;
import com.uit.weddingmanagement.modules.catalog.domain.model.Shift;
import com.uit.weddingmanagement.modules.system.application.port.out.SystemParameterQueryPort;
import com.uit.weddingmanagement.modules.system.domain.model.SystemParameter;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
// Transactional read committed để đảm bảo đọc được booking vừa insert trong cùng transaction khi kiểm tra slot có bị trùng hay không
@Transactional(isolation = Isolation.READ_COMMITTED)
public class CreateWeddingBookingService implements CreateWeddingBookingUseCase {

  private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

  private final WeddingBookingQueryPort weddingBookingQueryPort;
  private final WeddingBookingCommandPort weddingBookingCommandPort;
  private final HallQueryPort hallQueryPort;
  private final ShiftQueryPort shiftQueryPort;
  private final MenuItemQueryPort menuItemQueryPort;
  private final ServiceItemQueryPort serviceItemQueryPort;
  private final SystemParameterQueryPort systemParameterQueryPort;
  private final CurrentUserPort currentUserPort;

  public CreateWeddingBookingService(
      WeddingBookingQueryPort weddingBookingQueryPort,
      WeddingBookingCommandPort weddingBookingCommandPort,
      HallQueryPort hallQueryPort,
      ShiftQueryPort shiftQueryPort,
      MenuItemQueryPort menuItemQueryPort,
      ServiceItemQueryPort serviceItemQueryPort,
      SystemParameterQueryPort systemParameterQueryPort,
      CurrentUserPort currentUserPort) {
    this.weddingBookingQueryPort = weddingBookingQueryPort;
    this.weddingBookingCommandPort = weddingBookingCommandPort;
    this.hallQueryPort = hallQueryPort;
    this.shiftQueryPort = shiftQueryPort;
    this.menuItemQueryPort = menuItemQueryPort;
    this.serviceItemQueryPort = serviceItemQueryPort;
    this.systemParameterQueryPort = systemParameterQueryPort;
    this.currentUserPort = currentUserPort;
  }

  // Check trùng hall + shift + date
  // Đọc tham số mỗi lần đặt
  // Validate cọc tối thiểu
  // Snapshot giá món/dịch vụ tại thời điểm đặt
  // Tạo booking + chi tiết + phiếu cọc trong cùng transaction, nếu trùng sẽ rollback và trả lỗi thân thiện
  @Override
  @AuditAction(
      action = "WEDDING_BOOKING_CREATE",
      module = "BOOKING",
      targetType = "WEDDING_BOOKING",
      targetIdExpression = "#result.id",
      targetLabelExpression = "#result.groomName + ' & ' + #result.brideName",
      successDescriptionExpression =
          "'Created wedding booking ' + #result.id + ' for ' + #result.groomName + ' & ' + #result.brideName",
      failureDescriptionExpression =
          "'Failed to create wedding booking for ' + #command.groomName + ' & ' + #command.brideName",
      detailsExpression = "#command")
  public WeddingBookingDetailResult createWeddingBooking(CreateWeddingBookingCommand command) {
    Hall hall = loadHall(command.hallId());
    Shift shift = loadShift(command.shiftId());
    LocalDate bookingDate = LocalDate.now(ZoneOffset.UTC);

    ensureCelebrationDateIsNotInPast(command.celebrationDate(), bookingDate);
    ensureHallCanReceiveBooking(hall);
    ensureSlotIsAvailable(hall.id(), shift.id(), command.celebrationDate());

    // Rule QĐ2 yêu cầu đọc THAMSO tại đúng thời điểm đặt tiệc để thay đổi về sau
    // có hiệu lực ngay mà không cần deploy lại code.
    SystemParameter systemParameter = loadSystemParameter();
    List<BookingMenuItem> menuItems = buildBookingMenuItems(command.menuItems());
    List<BookingService> services = buildBookingServices(command.services());
    AuthenticatedUser currentUser = currentUserPort.getCurrentUser();
    Instant receivedAt = Instant.now();

    DepositReceipt depositReceipt =
        createDepositReceipt(command.depositReceipt(), currentUser.id(), receivedAt);

    WeddingBooking weddingBooking =
        WeddingBooking.create(
            hall.id(),
            hall.hallName(),
            shift.id(),
            shift.shiftName(),
            command.groomName(),
            command.brideName(),
            command.groomPhoneNumber(),
            command.bridePhoneNumber(),
            bookingDate,
            command.celebrationDate(),
            command.tableCount(),
            defaultReservedTableCount(command.reservedTableCount()),
            hall.tablePrice(),
            command.notes(),
            menuItems,
            services,
            depositReceipt);

    ensureDepositAmountMeetsMinimumRule(
        weddingBooking, depositReceipt.amount(), systemParameter.minimumDepositPercentage());

    try {
      return WeddingBookingDetailResult.from(
          weddingBookingCommandPort.saveWeddingBooking(weddingBooking));
    } catch (DataIntegrityViolationException exception) {
      // Pre-check giúp trả lỗi thân thiện, còn unique index ở DB mới là lớp khóa
      // cuối cùng để chống race condition khi hai request cùng giành một slot.
      throw new DuplicateResourceException(
          "Hall is already booked for the selected celebration date and shift.");
    }
  }

  private Hall loadHall(Long hallId) {
    requirePositiveId(hallId, "Hall id is required.", "Hall id must be greater than 0.");

    return hallQueryPort
        .findHallById(hallId)
        .orElseThrow(() -> new EntityNotFoundException("Hall not found with id: " + hallId));
  }

  private Shift loadShift(Long shiftId) {
    requirePositiveId(shiftId, "Shift id is required.", "Shift id must be greater than 0.");

    return shiftQueryPort
        .findShiftById(shiftId)
        .orElseThrow(() -> new EntityNotFoundException("Shift not found with id: " + shiftId));
  }

  private void ensureCelebrationDateIsNotInPast(LocalDate celebrationDate, LocalDate bookingDate) {
    if (celebrationDate == null) {
      throw new IllegalArgumentException("Celebration date is required.");
    }

    if (celebrationDate.isBefore(bookingDate)) {
      throw new IllegalArgumentException("Celebration date must be on or after booking date.");
    }
  }

  private void ensureHallCanReceiveBooking(Hall hall) {
    // Hall status hiện tại là trạng thái catalog toàn cục, không mang ngữ nghĩa
    // theo từng ngày+ca. Vì vậy chỉ chặn sảnh bảo trì, còn khả dụng theo slot sẽ
    // được quyết định bởi wedding_bookings + unique index.
    if (hall.status() == HallStatus.BAO_TRI) {
      throw new IllegalArgumentException("Hall is under maintenance and cannot be booked.");
    }
  }

  private void ensureSlotIsAvailable(Long hallId, Long shiftId, LocalDate celebrationDate) {
    if (weddingBookingQueryPort.existsActiveBookingByHallIdAndShiftIdAndCelebrationDate(
        hallId, shiftId, celebrationDate)) {
      throw new DuplicateResourceException(
          "Hall is already booked for the selected celebration date and shift.");
    }
  }

  private SystemParameter loadSystemParameter() {
    return systemParameterQueryPort
        .getSystemParameter()
        .orElseThrow(() -> new EntityNotFoundException("System parameter row was not found."));
  }

  private List<BookingMenuItem> buildBookingMenuItems(
      List<CreateWeddingBookingMenuItemCommand> menuItemCommands) {
    if (menuItemCommands == null || menuItemCommands.isEmpty()) {
      throw new IllegalArgumentException("At least one menu item is required.");
    }

    return menuItemCommands.stream().map(this::toBookingMenuItem).toList();
  }

  private BookingMenuItem toBookingMenuItem(CreateWeddingBookingMenuItemCommand command) {
    requirePositiveId(
        command.menuItemId(), "Menu item id is required.", "Menu item id must be greater than 0.");

    MenuItem menuItem =
        menuItemQueryPort
            .findMenuItemById(command.menuItemId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Menu item not found with id: " + command.menuItemId()));

    if (!menuItem.isAvailable()) {
      throw new IllegalArgumentException(
          "Menu item is not available for booking: " + menuItem.itemName());
    }

    return BookingMenuItem.create(
        menuItem.id(),
        menuItem.itemName(),
        command.quantity(),
        menuItem.currentPrice(),
        command.notes());
  }

  private List<BookingService> buildBookingServices(
      List<CreateWeddingBookingServiceCommand> serviceCommands) {
    if (serviceCommands == null || serviceCommands.isEmpty()) {
      return List.of();
    }

    return serviceCommands.stream().map(this::toBookingService).toList();
  }

  private BookingService toBookingService(CreateWeddingBookingServiceCommand command) {
    requirePositiveId(command.serviceId(), "Service id is required.", "Service id must be greater than 0.");

    ServiceItem serviceItem =
        serviceItemQueryPort
            .findServiceItemById(command.serviceId())
            .orElseThrow(
                () ->
                    new EntityNotFoundException(
                        "Service item not found with id: " + command.serviceId()));

    if (!serviceItem.isActive()) {
      throw new IllegalArgumentException(
          "Service item is not active for booking: " + serviceItem.serviceName());
    }

    return BookingService.create(
        serviceItem.id(),
        serviceItem.serviceName(),
        serviceItem.unitName(),
        command.quantity(),
        serviceItem.currentPrice(),
        command.notes());
  }

  private DepositReceipt createDepositReceipt(
      CreateDepositReceiptCommand command, Long userId, Instant receivedAt) {
    if (command == null) {
      throw new IllegalArgumentException("Deposit receipt is required.");
    }

    return DepositReceipt.create(userId, receivedAt, command.amount(), command.paymentMethod(), command.notes());
  }

  private Integer defaultReservedTableCount(Integer reservedTableCount) {
    return reservedTableCount == null ? 0 : reservedTableCount;
  }

  private void ensureDepositAmountMeetsMinimumRule(
      WeddingBooking weddingBooking, BigDecimal depositAmount, BigDecimal minimumDepositPercentage) {
    BigDecimal minimumRequiredDeposit =
        weddingBooking
            .calculateHallTotalAmount()
            .multiply(minimumDepositPercentage)
            .divide(ONE_HUNDRED, 2, RoundingMode.HALF_UP);

    if (depositAmount.compareTo(minimumRequiredDeposit) < 0) {
      throw new IllegalArgumentException(
          "Deposit amount must be greater than or equal to minimum required amount of "
              + minimumRequiredDeposit.toPlainString()
              + ".");
    }
  }

  private void requirePositiveId(Long value, String nullMessage, String negativeMessage) {
    if (value == null) {
      throw new IllegalArgumentException(nullMessage);
    }

    if (value <= 0) {
      throw new IllegalArgumentException(negativeMessage);
    }
  }
}
