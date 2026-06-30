package com.uit.weddingmanagement.modules.booking.infrastructure.persistence;

import com.uit.weddingmanagement.modules.auth.infrastructure.persistence.entity.UserJpaEntity;
import com.uit.weddingmanagement.modules.auth.infrastructure.persistence.repository.UserJpaRepository;
import com.uit.weddingmanagement.modules.booking.application.port.out.CancellationReceiptCommandPort;
import com.uit.weddingmanagement.modules.booking.application.port.out.IncidentalReceiptCommandPort;
import com.uit.weddingmanagement.modules.booking.application.port.out.IncidentalReceiptQueryPort;
import com.uit.weddingmanagement.modules.booking.application.port.out.InvoiceCommandPort;
import com.uit.weddingmanagement.modules.booking.application.port.out.InvoiceQueryPort;
import com.uit.weddingmanagement.modules.booking.application.port.out.WeddingBookingCommandPort;
import com.uit.weddingmanagement.modules.booking.application.port.out.WeddingBookingQueryPort;
import com.uit.weddingmanagement.modules.booking.domain.model.CancellationReceipt;
import com.uit.weddingmanagement.modules.booking.domain.model.BookingMenuItem;
import com.uit.weddingmanagement.modules.booking.domain.model.BookingService;
import com.uit.weddingmanagement.modules.booking.domain.model.DepositReceipt;
import com.uit.weddingmanagement.modules.booking.domain.model.IncidentalReceipt;
import com.uit.weddingmanagement.modules.booking.domain.model.IncidentalReceiptItem;
import com.uit.weddingmanagement.modules.booking.domain.model.Invoice;
import com.uit.weddingmanagement.modules.booking.domain.model.InvoiceSummary;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBooking;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingStatus;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingSummary;
import com.uit.weddingmanagement.modules.booking.infrastructure.persistence.entity.CancellationReceiptJpaEntity;
import com.uit.weddingmanagement.modules.booking.infrastructure.persistence.entity.BookingMenuItemJpaEntity;
import com.uit.weddingmanagement.modules.booking.infrastructure.persistence.entity.BookingServiceJpaEntity;
import com.uit.weddingmanagement.modules.booking.infrastructure.persistence.entity.DepositReceiptJpaEntity;
import com.uit.weddingmanagement.modules.booking.infrastructure.persistence.entity.IncidentalReceiptJpaEntity;
import com.uit.weddingmanagement.modules.booking.infrastructure.persistence.entity.InvoiceJpaEntity;
import com.uit.weddingmanagement.modules.booking.infrastructure.persistence.entity.WeddingBookingJpaEntity;
import com.uit.weddingmanagement.modules.booking.infrastructure.persistence.repository.CancellationReceiptJpaRepository;
import com.uit.weddingmanagement.modules.booking.infrastructure.persistence.repository.IncidentalReceiptJpaRepository;
import com.uit.weddingmanagement.modules.booking.infrastructure.persistence.repository.InvoiceJpaRepository;
import com.uit.weddingmanagement.modules.booking.infrastructure.persistence.repository.WeddingBookingJpaRepository;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity.HallJpaEntity;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity.IncidentalReceiptItemJpaEntity;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity.MenuItemJpaEntity;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity.ServiceItemJpaEntity;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity.ShiftJpaEntity;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.repository.HallJpaRepository;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.repository.MenuItemJpaRepository;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.repository.ServiceItemJpaRepository;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.repository.ShiftJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class BookingPersistenceAdapter
    implements
        WeddingBookingQueryPort,
        WeddingBookingCommandPort,
        CancellationReceiptCommandPort,
        IncidentalReceiptCommandPort,
        IncidentalReceiptQueryPort,
        InvoiceCommandPort,
        InvoiceQueryPort {

  private final WeddingBookingJpaRepository weddingBookingJpaRepository;
  private final CancellationReceiptJpaRepository cancellationReceiptJpaRepository;
  private final IncidentalReceiptJpaRepository incidentalReceiptJpaRepository;
  private final InvoiceJpaRepository invoiceJpaRepository;
  private final HallJpaRepository hallJpaRepository;
  private final ShiftJpaRepository shiftJpaRepository;
  private final MenuItemJpaRepository menuItemJpaRepository;
  private final ServiceItemJpaRepository serviceItemJpaRepository;
  private final UserJpaRepository userJpaRepository;

  public BookingPersistenceAdapter(
      WeddingBookingJpaRepository weddingBookingJpaRepository,
      CancellationReceiptJpaRepository cancellationReceiptJpaRepository,
      IncidentalReceiptJpaRepository incidentalReceiptJpaRepository,
      InvoiceJpaRepository invoiceJpaRepository,
      HallJpaRepository hallJpaRepository,
      ShiftJpaRepository shiftJpaRepository,
      MenuItemJpaRepository menuItemJpaRepository,
      ServiceItemJpaRepository serviceItemJpaRepository,
      UserJpaRepository userJpaRepository) {
    this.weddingBookingJpaRepository = weddingBookingJpaRepository;
    this.cancellationReceiptJpaRepository = cancellationReceiptJpaRepository;
    this.incidentalReceiptJpaRepository = incidentalReceiptJpaRepository;
    this.invoiceJpaRepository = invoiceJpaRepository;
    this.hallJpaRepository = hallJpaRepository;
    this.shiftJpaRepository = shiftJpaRepository;
    this.menuItemJpaRepository = menuItemJpaRepository;
    this.serviceItemJpaRepository = serviceItemJpaRepository;
    this.userJpaRepository = userJpaRepository;
  }

  @Override
  public boolean existsActiveBookingByHallIdAndShiftIdAndCelebrationDate(
      Long hallId, Long shiftId, LocalDate celebrationDate) {
    return weddingBookingJpaRepository.existsByHall_IdAndShift_IdAndCelebrationDateAndStatusNot(
        hallId, shiftId, celebrationDate, WeddingBookingStatus.DA_HUY);
  }

  @Override
  public Set<Long> findBookedHallIdsByCelebrationDateAndShiftId(LocalDate celebrationDate, Long shiftId) {
    return new LinkedHashSet<>(
        weddingBookingJpaRepository.findBookedHallIdsByCelebrationDateAndShiftId(
            celebrationDate, shiftId, WeddingBookingStatus.DA_HUY));
  }

  @Override
  public Page<WeddingBookingSummary> searchWeddingBookings(
      String groomName,
      String brideName,
      Long hallId,
      LocalDate celebrationDate,
      WeddingBookingStatus status,
      Pageable pageable) {
    return weddingBookingJpaRepository
        .findAll(
            buildSearchSpecification(groomName, brideName, hallId, celebrationDate, status),
            pageable)
        .map(this::toSummaryDomain);
  }

  @Override
  public Optional<WeddingBooking> findWeddingBookingById(Long bookingId) {
    return weddingBookingJpaRepository.findById(bookingId).map(this::toDomain);
  }

  @Override
  public Optional<WeddingBooking> findWeddingBookingByIdForUpdate(Long bookingId) {
    return weddingBookingJpaRepository.findByIdForUpdate(bookingId).map(this::toDomain);
  }

  @Override
  public Page<InvoiceSummary> searchInvoices(
      String groomName,
      String brideName,
      Long hallId,
      LocalDate celebrationDate,
      Pageable pageable) {
    return invoiceJpaRepository
        .findAll(
            buildInvoiceSearchSpecification(groomName, brideName, hallId, celebrationDate),
            pageable)
        .map(this::toSummaryDomain);
  }

  @Override
  public Optional<Invoice> findInvoiceById(Long invoiceId) {
    return invoiceJpaRepository.findById(invoiceId).map(this::toDomain);
  }

  @Override
  public BigDecimal sumIncidentalReceiptTotalAmountByWeddingBookingId(Long bookingId) {
    return incidentalReceiptJpaRepository.sumTotalAmountByWeddingBookingId(bookingId);
  }

  @Override
  public List<IncidentalReceipt> findIncidentalReceiptsByWeddingBookingId(Long bookingId) {
    return incidentalReceiptJpaRepository.findDetailedByWeddingBookingId(bookingId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public WeddingBooking saveWeddingBooking(WeddingBooking weddingBooking) {
    WeddingBookingJpaEntity weddingBookingJpaEntity = resolveEntityForSave(weddingBooking);
    weddingBookingJpaEntity.setHall(resolveHall(weddingBooking.hallId()));
    weddingBookingJpaEntity.setShift(resolveShift(weddingBooking.shiftId()));
    weddingBookingJpaEntity.setGroomName(weddingBooking.groomName());
    weddingBookingJpaEntity.setBrideName(weddingBooking.brideName());
    weddingBookingJpaEntity.setGroomPhoneNumber(weddingBooking.groomPhoneNumber());
    weddingBookingJpaEntity.setBridePhoneNumber(weddingBooking.bridePhoneNumber());
    weddingBookingJpaEntity.setBookingDate(weddingBooking.bookingDate());
    weddingBookingJpaEntity.setCelebrationDate(weddingBooking.celebrationDate());
    weddingBookingJpaEntity.setTableCount(weddingBooking.tableCount());
    weddingBookingJpaEntity.setReservedTableCount(weddingBooking.reservedTableCount());
    weddingBookingJpaEntity.setTablePrice(weddingBooking.tablePrice());
    weddingBookingJpaEntity.setStatus(weddingBooking.status());
    weddingBookingJpaEntity.setNotes(weddingBooking.notes());
    weddingBookingJpaEntity.setBookingMenuItems(
        mapBookingMenuItems(weddingBooking.menuItems(), weddingBookingJpaEntity));
    weddingBookingJpaEntity.setBookingServices(
        mapBookingServices(weddingBooking.services(), weddingBookingJpaEntity));
    weddingBookingJpaEntity.setDepositReceipt(
        mapDepositReceipt(weddingBooking.depositReceipt(), weddingBookingJpaEntity));

    return toDomain(weddingBookingJpaRepository.save(weddingBookingJpaEntity));
  }

  @Override
  public WeddingBooking updateWeddingBookingStatus(Long bookingId, WeddingBookingStatus status) {
    if (bookingId == null || bookingId <= 0) {
      throw new IllegalArgumentException("Wedding booking id must be greater than 0.");
    }

    if (status == null) {
      throw new IllegalArgumentException("Wedding booking status is required.");
    }

    WeddingBookingJpaEntity weddingBookingJpaEntity =
        weddingBookingJpaRepository
            .findById(bookingId)
            .orElseThrow(
                () -> new EntityNotFoundException("Wedding booking not found with id: " + bookingId));
    weddingBookingJpaEntity.setStatus(status);

    return toDomain(weddingBookingJpaRepository.save(weddingBookingJpaEntity));
  }

  @Override
  public IncidentalReceipt saveIncidentalReceipt(IncidentalReceipt incidentalReceipt) {
    IncidentalReceiptJpaEntity incidentalReceiptJpaEntity = new IncidentalReceiptJpaEntity();
    incidentalReceiptJpaEntity.setWeddingBooking(resolveWeddingBookingEntity(incidentalReceipt.weddingBookingId()));
    incidentalReceiptJpaEntity.setUser(resolveUser(incidentalReceipt.userId()));
    incidentalReceiptJpaEntity.setRecordedAt(incidentalReceipt.recordedAt());
    incidentalReceiptJpaEntity.setTotalAmount(incidentalReceipt.totalAmount());
    incidentalReceiptJpaEntity.setNotes(incidentalReceipt.notes());
    incidentalReceiptJpaEntity.setItems(
        mapIncidentalReceiptItems(incidentalReceipt.items(), incidentalReceiptJpaEntity));

    return toDomain(incidentalReceiptJpaRepository.save(incidentalReceiptJpaEntity));
  }

  @Override
  public CancellationReceipt saveCancellationReceipt(CancellationReceipt cancellationReceipt) {
    CancellationReceiptJpaEntity cancellationReceiptJpaEntity = new CancellationReceiptJpaEntity();
    cancellationReceiptJpaEntity.setWeddingBooking(
        resolveWeddingBookingEntity(cancellationReceipt.weddingBookingId()));
    cancellationReceiptJpaEntity.setUser(resolveUser(cancellationReceipt.userId()));
    cancellationReceiptJpaEntity.setCancelledAt(cancellationReceipt.cancelledAt());
    cancellationReceiptJpaEntity.setDaysBeforeCelebration(
        cancellationReceipt.daysBeforeCelebration());
    cancellationReceiptJpaEntity.setAppliedDepositRefundPercentage(
        cancellationReceipt.appliedDepositRefundPercentage());
    cancellationReceiptJpaEntity.setRefundAmount(cancellationReceipt.refundAmount());
    cancellationReceiptJpaEntity.setReason(cancellationReceipt.reason());

    return toDomain(cancellationReceiptJpaRepository.save(cancellationReceiptJpaEntity));
  }

  @Override
  public Invoice saveInvoice(Invoice invoice) {
    InvoiceJpaEntity invoiceJpaEntity = new InvoiceJpaEntity();
    invoiceJpaEntity.setWeddingBooking(resolveWeddingBookingEntity(invoice.weddingBookingId()));
    invoiceJpaEntity.setUser(resolveUser(invoice.userId()));
    invoiceJpaEntity.setPaidAt(invoice.paidAt());
    invoiceJpaEntity.setHallTotalAmount(invoice.hallTotalAmount());
    invoiceJpaEntity.setMenuItemsTotalAmount(invoice.menuItemsTotalAmount());
    invoiceJpaEntity.setServicesTotalAmount(invoice.servicesTotalAmount());
    invoiceJpaEntity.setIncidentalsTotalAmount(invoice.incidentalsTotalAmount());
    invoiceJpaEntity.setDepositAmount(invoice.depositAmount());
    invoiceJpaEntity.setLatePaymentPenaltyAmount(invoice.latePaymentPenaltyAmount());
    invoiceJpaEntity.setFinalAmount(invoice.finalAmount());
    invoiceJpaEntity.setNotes(invoice.notes());

    return toDomain(invoiceJpaRepository.save(invoiceJpaEntity));
  }

  private WeddingBookingJpaEntity resolveEntityForSave(WeddingBooking weddingBooking) {
    if (weddingBooking.id() == null) {
      return new WeddingBookingJpaEntity();
    }

    return weddingBookingJpaRepository
        .findById(weddingBooking.id())
        .orElseThrow(
            () ->
                new EntityNotFoundException(
                    "Wedding booking not found with id: " + weddingBooking.id()));
  }

  private WeddingBookingJpaEntity resolveWeddingBookingEntity(Long bookingId) {
    return weddingBookingJpaRepository
        .findById(bookingId)
        .orElseThrow(
            () -> new EntityNotFoundException("Wedding booking not found with id: " + bookingId));
  }

  private Specification<WeddingBookingJpaEntity> buildSearchSpecification(
      String groomName,
      String brideName,
      Long hallId,
      LocalDate celebrationDate,
      WeddingBookingStatus status) {
    Specification<WeddingBookingJpaEntity> specification =
        (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

    if (groomName != null) {
      specification =
          specification.and(
              (root, query, criteriaBuilder) ->
                  criteriaBuilder.like(
                      criteriaBuilder.lower(root.get("groomName")),
                      "%" + groomName.toLowerCase(Locale.ROOT) + "%"));
    }

    if (brideName != null) {
      specification =
          specification.and(
              (root, query, criteriaBuilder) ->
                  criteriaBuilder.like(
                      criteriaBuilder.lower(root.get("brideName")),
                      "%" + brideName.toLowerCase(Locale.ROOT) + "%"));
    }

    if (hallId != null) {
      specification =
          specification.and(
              (root, query, criteriaBuilder) ->
                  criteriaBuilder.equal(root.get("hall").get("id"), hallId));
    }

    if (celebrationDate != null) {
      specification =
          specification.and(
              (root, query, criteriaBuilder) ->
                  criteriaBuilder.equal(root.get("celebrationDate"), celebrationDate));
    }

    if (status != null) {
      specification =
          specification.and(
              (root, query, criteriaBuilder) ->
                  criteriaBuilder.equal(root.get("status"), status));
    }

    return specification;
  }

  private Specification<InvoiceJpaEntity> buildInvoiceSearchSpecification(
      String groomName, String brideName, Long hallId, LocalDate celebrationDate) {
    Specification<InvoiceJpaEntity> specification = (root, query, criteriaBuilder) -> {
      query.distinct(true);
      return criteriaBuilder.conjunction();
    };

    if (groomName != null) {
      specification =
          specification.and(
              (root, query, criteriaBuilder) -> {
                query.distinct(true);
                return criteriaBuilder.like(
                    criteriaBuilder.lower(root.join("weddingBooking").get("groomName")),
                    "%" + groomName.toLowerCase(Locale.ROOT) + "%");
              });
    }

    if (brideName != null) {
      specification =
          specification.and(
              (root, query, criteriaBuilder) -> {
                query.distinct(true);
                return criteriaBuilder.like(
                    criteriaBuilder.lower(root.join("weddingBooking").get("brideName")),
                    "%" + brideName.toLowerCase(Locale.ROOT) + "%");
              });
    }

    if (hallId != null) {
      specification =
          specification.and(
              (root, query, criteriaBuilder) -> {
                query.distinct(true);
                return criteriaBuilder.equal(root.join("weddingBooking").get("hall").get("id"), hallId);
              });
    }

    if (celebrationDate != null) {
      specification =
          specification.and(
              (root, query, criteriaBuilder) -> {
                query.distinct(true);
                return criteriaBuilder.equal(
                    root.join("weddingBooking").get("celebrationDate"), celebrationDate);
              });
    }

    return specification;
  }

  private HallJpaEntity resolveHall(Long hallId) {
    return hallJpaRepository
        .findById(hallId)
        .orElseThrow(() -> new EntityNotFoundException("Hall not found with id: " + hallId));
  }

  private ShiftJpaEntity resolveShift(Long shiftId) {
    return shiftJpaRepository
        .findById(shiftId)
        .orElseThrow(() -> new EntityNotFoundException("Shift not found with id: " + shiftId));
  }

  private List<BookingMenuItemJpaEntity> mapBookingMenuItems(
      List<BookingMenuItem> bookingMenuItems, WeddingBookingJpaEntity weddingBookingJpaEntity) {
    List<BookingMenuItemJpaEntity> bookingMenuItemJpaEntities = new ArrayList<>();

    for (BookingMenuItem bookingMenuItem : bookingMenuItems) {
      BookingMenuItemJpaEntity bookingMenuItemJpaEntity = new BookingMenuItemJpaEntity();
      bookingMenuItemJpaEntity.setWeddingBooking(weddingBookingJpaEntity);
      bookingMenuItemJpaEntity.setMenuItem(resolveMenuItem(bookingMenuItem.menuItemId()));
      bookingMenuItemJpaEntity.setQuantity(bookingMenuItem.quantity());
      bookingMenuItemJpaEntity.setPriceSnapshot(bookingMenuItem.priceSnapshot());
      bookingMenuItemJpaEntity.setLineTotal(bookingMenuItem.lineTotal());
      bookingMenuItemJpaEntity.setNotes(bookingMenuItem.notes());
      bookingMenuItemJpaEntities.add(bookingMenuItemJpaEntity);
    }

    return bookingMenuItemJpaEntities;
  }

  private List<BookingServiceJpaEntity> mapBookingServices(
      List<BookingService> bookingServices, WeddingBookingJpaEntity weddingBookingJpaEntity) {
    List<BookingServiceJpaEntity> bookingServiceJpaEntities = new ArrayList<>();

    for (BookingService bookingService : bookingServices) {
      BookingServiceJpaEntity bookingServiceJpaEntity = new BookingServiceJpaEntity();
      bookingServiceJpaEntity.setWeddingBooking(weddingBookingJpaEntity);
      bookingServiceJpaEntity.setServiceItem(resolveServiceItem(bookingService.serviceId()));
      bookingServiceJpaEntity.setQuantity(bookingService.quantity());
      bookingServiceJpaEntity.setPriceSnapshot(bookingService.priceSnapshot());
      bookingServiceJpaEntity.setLineTotal(bookingService.lineTotal());
      bookingServiceJpaEntity.setNotes(bookingService.notes());
      bookingServiceJpaEntities.add(bookingServiceJpaEntity);
    }

    return bookingServiceJpaEntities;
  }

  private DepositReceiptJpaEntity mapDepositReceipt(
      DepositReceipt depositReceipt, WeddingBookingJpaEntity weddingBookingJpaEntity) {
    DepositReceiptJpaEntity depositReceiptJpaEntity = new DepositReceiptJpaEntity();
    depositReceiptJpaEntity.setWeddingBooking(weddingBookingJpaEntity);
    depositReceiptJpaEntity.setUser(resolveUser(depositReceipt.userId()));
    depositReceiptJpaEntity.setReceivedAt(depositReceipt.receivedAt());
    depositReceiptJpaEntity.setAmount(depositReceipt.amount());
    depositReceiptJpaEntity.setPaymentMethod(depositReceipt.paymentMethod());
    depositReceiptJpaEntity.setNotes(depositReceipt.notes());
    return depositReceiptJpaEntity;
  }

  private List<IncidentalReceiptItemJpaEntity> mapIncidentalReceiptItems(
      List<IncidentalReceiptItem> incidentalReceiptItems,
      IncidentalReceiptJpaEntity incidentalReceiptJpaEntity) {
    List<IncidentalReceiptItemJpaEntity> incidentalReceiptItemJpaEntities = new ArrayList<>();

    for (IncidentalReceiptItem incidentalReceiptItem : incidentalReceiptItems) {
      IncidentalReceiptItemJpaEntity incidentalReceiptItemJpaEntity =
          new IncidentalReceiptItemJpaEntity();
      incidentalReceiptItemJpaEntity.setIncidentalReceipt(incidentalReceiptJpaEntity);
      incidentalReceiptItemJpaEntity.setServiceItem(resolveServiceItem(incidentalReceiptItem.serviceId()));
      incidentalReceiptItemJpaEntity.setQuantity(incidentalReceiptItem.quantity());
      incidentalReceiptItemJpaEntity.setAppliedUnitPrice(incidentalReceiptItem.appliedUnitPrice());
      incidentalReceiptItemJpaEntity.setLineTotal(incidentalReceiptItem.lineTotal());
      incidentalReceiptItemJpaEntity.setNotes(incidentalReceiptItem.notes());
      incidentalReceiptItemJpaEntities.add(incidentalReceiptItemJpaEntity);
    }

    return incidentalReceiptItemJpaEntities;
  }

  private MenuItemJpaEntity resolveMenuItem(Long menuItemId) {
    return menuItemJpaRepository
        .findById(menuItemId)
        .orElseThrow(
            () -> new EntityNotFoundException("Menu item not found with id: " + menuItemId));
  }

  private ServiceItemJpaEntity resolveServiceItem(Long serviceItemId) {
    return serviceItemJpaRepository
        .findById(serviceItemId)
        .orElseThrow(
            () -> new EntityNotFoundException("Service item not found with id: " + serviceItemId));
  }

  private UserJpaEntity resolveUser(Long userId) {
    if (userId == null) {
      return null;
    }

    return userJpaRepository
        .findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + userId));
  }

  private WeddingBooking toDomain(WeddingBookingJpaEntity weddingBookingJpaEntity) {
    return new WeddingBooking(
        weddingBookingJpaEntity.getId(),
        weddingBookingJpaEntity.getHall().getId(),
        weddingBookingJpaEntity.getHall().getHallName(),
        weddingBookingJpaEntity.getShift().getId(),
        weddingBookingJpaEntity.getShift().getShiftName(),
        weddingBookingJpaEntity.getGroomName(),
        weddingBookingJpaEntity.getBrideName(),
        weddingBookingJpaEntity.getGroomPhoneNumber(),
        weddingBookingJpaEntity.getBridePhoneNumber(),
        weddingBookingJpaEntity.getBookingDate(),
        weddingBookingJpaEntity.getCelebrationDate(),
        weddingBookingJpaEntity.getTableCount(),
        weddingBookingJpaEntity.getReservedTableCount(),
        weddingBookingJpaEntity.getTablePrice(),
        weddingBookingJpaEntity.getStatus(),
        weddingBookingJpaEntity.getNotes(),
        weddingBookingJpaEntity.getBookingMenuItems().stream().map(this::toDomain).toList(),
        weddingBookingJpaEntity.getBookingServices().stream().map(this::toDomain).toList(),
        toDomain(weddingBookingJpaEntity.getDepositReceipt()));
  }

  private WeddingBookingSummary toSummaryDomain(WeddingBookingJpaEntity weddingBookingJpaEntity) {
    return new WeddingBookingSummary(
        weddingBookingJpaEntity.getId(),
        weddingBookingJpaEntity.getHall().getId(),
        weddingBookingJpaEntity.getHall().getHallName(),
        weddingBookingJpaEntity.getShift().getId(),
        weddingBookingJpaEntity.getShift().getShiftName(),
        weddingBookingJpaEntity.getGroomName(),
        weddingBookingJpaEntity.getBrideName(),
        weddingBookingJpaEntity.getCelebrationDate(),
        weddingBookingJpaEntity.getTableCount(),
        weddingBookingJpaEntity.getStatus());
  }

  private InvoiceSummary toSummaryDomain(InvoiceJpaEntity invoiceJpaEntity) {
    WeddingBookingJpaEntity weddingBookingJpaEntity = invoiceJpaEntity.getWeddingBooking();
    return new InvoiceSummary(
        invoiceJpaEntity.getId(),
        weddingBookingJpaEntity.getId(),
        weddingBookingJpaEntity.getHall().getId(),
        weddingBookingJpaEntity.getHall().getHallName(),
        weddingBookingJpaEntity.getShift().getId(),
        weddingBookingJpaEntity.getShift().getShiftName(),
        weddingBookingJpaEntity.getGroomName(),
        weddingBookingJpaEntity.getBrideName(),
        weddingBookingJpaEntity.getCelebrationDate(),
        invoiceJpaEntity.getPaidAt(),
        invoiceJpaEntity.getFinalAmount());
  }

  private BookingMenuItem toDomain(BookingMenuItemJpaEntity bookingMenuItemJpaEntity) {
    return new BookingMenuItem(
        bookingMenuItemJpaEntity.getId(),
        bookingMenuItemJpaEntity.getMenuItem().getId(),
        bookingMenuItemJpaEntity.getMenuItem().getItemName(),
        bookingMenuItemJpaEntity.getQuantity(),
        bookingMenuItemJpaEntity.getPriceSnapshot(),
        bookingMenuItemJpaEntity.getLineTotal(),
        bookingMenuItemJpaEntity.getNotes());
  }

  private BookingService toDomain(BookingServiceJpaEntity bookingServiceJpaEntity) {
    return new BookingService(
        bookingServiceJpaEntity.getId(),
        bookingServiceJpaEntity.getServiceItem().getId(),
        bookingServiceJpaEntity.getServiceItem().getServiceName(),
        bookingServiceJpaEntity.getServiceItem().getUnitName(),
        bookingServiceJpaEntity.getQuantity(),
        bookingServiceJpaEntity.getPriceSnapshot(),
        bookingServiceJpaEntity.getLineTotal(),
        bookingServiceJpaEntity.getNotes());
  }

  private DepositReceipt toDomain(DepositReceiptJpaEntity depositReceiptJpaEntity) {
    return new DepositReceipt(
        depositReceiptJpaEntity.getId(),
        depositReceiptJpaEntity.getWeddingBooking().getId(),
        depositReceiptJpaEntity.getUser() == null ? null : depositReceiptJpaEntity.getUser().getId(),
        depositReceiptJpaEntity.getReceivedAt(),
        depositReceiptJpaEntity.getAmount(),
        depositReceiptJpaEntity.getPaymentMethod(),
        depositReceiptJpaEntity.getNotes());
  }

  private IncidentalReceipt toDomain(IncidentalReceiptJpaEntity incidentalReceiptJpaEntity) {
    return new IncidentalReceipt(
        incidentalReceiptJpaEntity.getId(),
        incidentalReceiptJpaEntity.getWeddingBooking().getId(),
        incidentalReceiptJpaEntity.getUser() == null ? null : incidentalReceiptJpaEntity.getUser().getId(),
        incidentalReceiptJpaEntity.getRecordedAt(),
        incidentalReceiptJpaEntity.getTotalAmount(),
        incidentalReceiptJpaEntity.getNotes(),
        incidentalReceiptJpaEntity.getItems().stream().map(this::toDomain).toList());
  }

  private IncidentalReceiptItem toDomain(IncidentalReceiptItemJpaEntity incidentalReceiptItemJpaEntity) {
    return new IncidentalReceiptItem(
        incidentalReceiptItemJpaEntity.getId(),
        incidentalReceiptItemJpaEntity.getServiceItem().getId(),
        incidentalReceiptItemJpaEntity.getServiceItem().getServiceName(),
        incidentalReceiptItemJpaEntity.getServiceItem().getUnitName(),
        incidentalReceiptItemJpaEntity.getQuantity(),
        incidentalReceiptItemJpaEntity.getAppliedUnitPrice(),
        incidentalReceiptItemJpaEntity.getLineTotal(),
        incidentalReceiptItemJpaEntity.getNotes());
  }

  private Invoice toDomain(InvoiceJpaEntity invoiceJpaEntity) {
    return new Invoice(
        invoiceJpaEntity.getId(),
        invoiceJpaEntity.getWeddingBooking().getId(),
        invoiceJpaEntity.getUser() == null ? null : invoiceJpaEntity.getUser().getId(),
        invoiceJpaEntity.getPaidAt(),
        invoiceJpaEntity.getHallTotalAmount(),
        invoiceJpaEntity.getMenuItemsTotalAmount(),
        invoiceJpaEntity.getServicesTotalAmount(),
        invoiceJpaEntity.getIncidentalsTotalAmount(),
        invoiceJpaEntity.getDepositAmount(),
        invoiceJpaEntity.getLatePaymentPenaltyAmount(),
        invoiceJpaEntity.getFinalAmount(),
        invoiceJpaEntity.getNotes());
  }

  private CancellationReceipt toDomain(CancellationReceiptJpaEntity cancellationReceiptJpaEntity) {
    return new CancellationReceipt(
        cancellationReceiptJpaEntity.getId(),
        cancellationReceiptJpaEntity.getWeddingBooking().getId(),
        cancellationReceiptJpaEntity.getUser() == null
            ? null
            : cancellationReceiptJpaEntity.getUser().getId(),
        cancellationReceiptJpaEntity.getCancelledAt(),
        cancellationReceiptJpaEntity.getDaysBeforeCelebration(),
        cancellationReceiptJpaEntity.getAppliedDepositRefundPercentage(),
        cancellationReceiptJpaEntity.getRefundAmount(),
        cancellationReceiptJpaEntity.getReason());
  }
}
