package com.uit.weddingmanagement.modules.booking.presentation.controller;

import com.uit.weddingmanagement.common.api.ApiResponse;
import com.uit.weddingmanagement.modules.booking.application.port.in.CreateCancellationReceiptUseCase;
import com.uit.weddingmanagement.modules.booking.application.port.in.CreateIncidentalReceiptUseCase;
import com.uit.weddingmanagement.modules.booking.application.port.in.CreateInvoiceUseCase;
import com.uit.weddingmanagement.modules.booking.application.port.in.CreateWeddingBookingUseCase;
import com.uit.weddingmanagement.modules.booking.application.port.in.GetDepositSlipUseCase;
import com.uit.weddingmanagement.modules.booking.application.port.in.GetInvoiceDetailUseCase;
import com.uit.weddingmanagement.modules.booking.application.port.in.GetInvoicePreviewUseCase;
import com.uit.weddingmanagement.modules.booking.application.port.in.GetWeddingBookingDetailUseCase;
import com.uit.weddingmanagement.modules.booking.application.port.in.ListAvailableHallsUseCase;
import com.uit.weddingmanagement.modules.booking.application.port.in.ListIncidentalReceiptsUseCase;
import com.uit.weddingmanagement.modules.booking.application.port.in.SearchInvoicesUseCase;
import com.uit.weddingmanagement.modules.booking.application.port.in.SearchWeddingBookingsUseCase;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingStatus;
import com.uit.weddingmanagement.modules.booking.presentation.dto.request.CreateCancellationReceiptRequest;
import com.uit.weddingmanagement.modules.booking.presentation.dto.request.CreateIncidentalReceiptRequest;
import com.uit.weddingmanagement.modules.booking.presentation.dto.request.CreateInvoiceRequest;
import com.uit.weddingmanagement.modules.booking.presentation.dto.request.CreateWeddingBookingRequest;
import com.uit.weddingmanagement.modules.booking.presentation.dto.response.AvailableHallResponse;
import com.uit.weddingmanagement.modules.booking.presentation.dto.response.CancellationReceiptResponse;
import com.uit.weddingmanagement.modules.booking.presentation.dto.response.DepositSlipResponse;
import com.uit.weddingmanagement.modules.booking.presentation.dto.response.IncidentalReceiptResponse;
import com.uit.weddingmanagement.modules.booking.presentation.dto.response.InvoiceDetailResponse;
import com.uit.weddingmanagement.modules.booking.presentation.dto.response.InvoicePageResponse;
import com.uit.weddingmanagement.modules.booking.presentation.dto.response.InvoicePreviewResponse;
import com.uit.weddingmanagement.modules.booking.presentation.dto.response.InvoiceResponse;
import com.uit.weddingmanagement.modules.booking.presentation.dto.response.InvoiceSummaryResponse;
import com.uit.weddingmanagement.modules.booking.presentation.dto.response.WeddingBookingDetailResponse;
import com.uit.weddingmanagement.modules.booking.presentation.dto.response.WeddingBookingPageResponse;
import com.uit.weddingmanagement.modules.booking.presentation.dto.response.WeddingBookingSummaryResponse;
import com.uit.weddingmanagement.modules.booking.presentation.mapper.BookingCommandMapper;
import com.uit.weddingmanagement.modules.booking.presentation.mapper.BookingPresentationMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@Tag(name = "Wedding Bookings", description = "APIs for wedding booking intake and deposit collection.")
public class BookingController {

  private static final String WEDDING_BOOKING_CREATE_PERMISSION =
      "@authorizationService.hasPermission('WEDDING_BOOKING_CREATE')";
  private static final String WEDDING_BOOKING_VIEW_PERMISSION =
      "@authorizationService.hasPermission('WEDDING_BOOKING_VIEW')";
  private static final String INCIDENTAL_RECEIPT_CREATE_PERMISSION =
      "@authorizationService.hasPermission('INCIDENTAL_RECEIPT_CREATE')";
  private static final String INVOICE_VIEW_PERMISSION =
      "@authorizationService.hasPermission('INVOICE_VIEW')";
  private static final String INVOICE_CREATE_PERMISSION =
      "@authorizationService.hasPermission('INVOICE_CREATE')";
  private static final String CANCELLATION_RECEIPT_CREATE_PERMISSION =
      "@authorizationService.hasPermission('CANCELLATION_RECEIPT_CREATE')";

  private final CreateWeddingBookingUseCase createWeddingBookingUseCase;
  private final CreateCancellationReceiptUseCase createCancellationReceiptUseCase;
  private final CreateIncidentalReceiptUseCase createIncidentalReceiptUseCase;
  private final GetInvoicePreviewUseCase getInvoicePreviewUseCase;
  private final CreateInvoiceUseCase createInvoiceUseCase;
  private final ListAvailableHallsUseCase listAvailableHallsUseCase;
  private final SearchInvoicesUseCase searchInvoicesUseCase;
  private final SearchWeddingBookingsUseCase searchWeddingBookingsUseCase;
  private final GetInvoiceDetailUseCase getInvoiceDetailUseCase;
  private final GetWeddingBookingDetailUseCase getWeddingBookingDetailUseCase;
  private final ListIncidentalReceiptsUseCase listIncidentalReceiptsUseCase;
  private final GetDepositSlipUseCase getDepositSlipUseCase;
  private final BookingPresentationMapper bookingPresentationMapper;
  private final BookingCommandMapper bookingCommandMapper;

  public BookingController(
      CreateWeddingBookingUseCase createWeddingBookingUseCase,
      CreateCancellationReceiptUseCase createCancellationReceiptUseCase,
      CreateIncidentalReceiptUseCase createIncidentalReceiptUseCase,
      GetInvoicePreviewUseCase getInvoicePreviewUseCase,
      CreateInvoiceUseCase createInvoiceUseCase,
      ListAvailableHallsUseCase listAvailableHallsUseCase,
      SearchInvoicesUseCase searchInvoicesUseCase,
      SearchWeddingBookingsUseCase searchWeddingBookingsUseCase,
      GetInvoiceDetailUseCase getInvoiceDetailUseCase,
      GetWeddingBookingDetailUseCase getWeddingBookingDetailUseCase,
      ListIncidentalReceiptsUseCase listIncidentalReceiptsUseCase,
      GetDepositSlipUseCase getDepositSlipUseCase,
      BookingPresentationMapper bookingPresentationMapper,
      BookingCommandMapper bookingCommandMapper) {
    this.createWeddingBookingUseCase = createWeddingBookingUseCase;
    this.createCancellationReceiptUseCase = createCancellationReceiptUseCase;
    this.createIncidentalReceiptUseCase = createIncidentalReceiptUseCase;
    this.getInvoicePreviewUseCase = getInvoicePreviewUseCase;
    this.createInvoiceUseCase = createInvoiceUseCase;
    this.listAvailableHallsUseCase = listAvailableHallsUseCase;
    this.searchInvoicesUseCase = searchInvoicesUseCase;
    this.searchWeddingBookingsUseCase = searchWeddingBookingsUseCase;
    this.getInvoiceDetailUseCase = getInvoiceDetailUseCase;
    this.getWeddingBookingDetailUseCase = getWeddingBookingDetailUseCase;
    this.listIncidentalReceiptsUseCase = listIncidentalReceiptsUseCase;
    this.getDepositSlipUseCase = getDepositSlipUseCase;
    this.bookingPresentationMapper = bookingPresentationMapper;
    this.bookingCommandMapper = bookingCommandMapper;
  }

  @GetMapping("/api/halls/availability")
  @PreAuthorize(WEDDING_BOOKING_CREATE_PERMISSION)
  @Operation(
      summary = "List available halls by date and shift",
      description = "Returns halls that are not under maintenance and not already booked for the requested slot.")
  @SecurityRequirement(name = "bearerAuth")
  @ApiResponses({
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "200",
        description = "Available halls loaded successfully."),
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
        responseCode = "403",
        description = "Current user does not have WEDDING_BOOKING_CREATE permission.")
  })
  public ApiResponse<List<AvailableHallResponse>> listAvailableHalls(
      @RequestParam("date")
          @NotNull(message = "Celebration date is required.")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate celebrationDate,
      @RequestParam("shiftId") @Positive(message = "Shift id must be greater than 0.") Long shiftId) {
    List<AvailableHallResponse> availableHalls =
        listAvailableHallsUseCase.listAvailableHalls(celebrationDate, shiftId).stream()
            .map(bookingPresentationMapper::toResponse)
            .toList();

    return ApiResponse.success("Available halls loaded successfully.", availableHalls);
  }

  @GetMapping("/api/bookings")
  @PreAuthorize(WEDDING_BOOKING_VIEW_PERMISSION)
  @Operation(
      summary = "Search wedding bookings",
      description =
          "Returns paged BM3 wedding booking summaries filtered by groom name, bride name, hall, celebration date, and status.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<WeddingBookingPageResponse> searchWeddingBookings(
      @RequestParam(required = false) String groomName,
      @RequestParam(required = false) String brideName,
      @RequestParam(required = false) Long hallId,
      @RequestParam(value = "date", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate celebrationDate,
      @RequestParam(required = false) WeddingBookingStatus status,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    // BM3 tra cứu đọc nhiều hơn ghi, nên response được tách riêng thành read-model có phân trang.
    Page<WeddingBookingSummaryResponse> resultPage =
        searchWeddingBookingsUseCase
            .searchWeddingBookings(
                new SearchWeddingBookingsUseCase.SearchWeddingBookingsQuery(
                    groomName,
                    brideName,
                    hallId,
                    celebrationDate,
                    status,
                    PageRequest.of(
                        page,
                        size,
                        Sort.by(Sort.Order.asc("celebrationDate"), Sort.Order.desc("id")))))
            .map(bookingPresentationMapper::toResponse);

    WeddingBookingPageResponse response =
        new WeddingBookingPageResponse(
            resultPage.getContent(),
            resultPage.getTotalElements(),
            resultPage.getTotalPages(),
            resultPage.getNumber(),
            resultPage.getSize());

    return ApiResponse.success("Wedding bookings loaded successfully.", response);
  }

  @GetMapping("/api/invoices")
  @PreAuthorize(INVOICE_VIEW_PERMISSION)
  @Operation(
      summary = "Search invoices",
      description =
          "Returns paged invoice summaries filtered by groom name, bride name, hall, and celebration date.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<InvoicePageResponse> searchInvoices(
      @RequestParam(required = false) String groomName,
      @RequestParam(required = false) String brideName,
      @RequestParam(required = false) Long hallId,
      @RequestParam(value = "date", required = false)
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
          LocalDate celebrationDate,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    Page<InvoiceSummaryResponse> resultPage =
        searchInvoicesUseCase
            .searchInvoices(
                new SearchInvoicesUseCase.SearchInvoicesQuery(
                    groomName,
                    brideName,
                    hallId,
                    celebrationDate,
                    PageRequest.of(
                        page,
                        size,
                        Sort.by(Sort.Order.desc("paidAt"), Sort.Order.desc("id")))))
            .map(bookingPresentationMapper::toResponse);

    InvoicePageResponse response =
        new InvoicePageResponse(
            resultPage.getContent(),
            resultPage.getTotalElements(),
            resultPage.getTotalPages(),
            resultPage.getNumber(),
            resultPage.getSize());

    return ApiResponse.success("Invoices loaded successfully.", response);
  }

  @GetMapping("/api/bookings/{id}")
  @PreAuthorize(WEDDING_BOOKING_VIEW_PERMISSION)
  @Operation(
      summary = "Get wedding booking detail",
      description = "Returns the full wedding booking detail including menu items, services, and deposit receipt.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<WeddingBookingDetailResponse> getWeddingBookingDetail(
      @PathVariable("id") @Positive(message = "Wedding booking id must be greater than 0.") Long bookingId) {
    return ApiResponse.success(
        "Wedding booking detail loaded successfully.",
        bookingPresentationMapper.toResponse(getWeddingBookingDetailUseCase.getWeddingBookingDetail(bookingId)));
  }

  @GetMapping("/api/invoices/{id}")
  @PreAuthorize(INVOICE_VIEW_PERMISSION)
  @Operation(
      summary = "Get invoice detail",
      description =
          "Returns the read-only invoice detail with payment totals, booking snapshot, and incidental receipt snapshot.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<InvoiceDetailResponse> getInvoiceDetail(
      @PathVariable("id") @Positive(message = "Invoice id must be greater than 0.") Long invoiceId) {
    return ApiResponse.success(
        "Invoice detail loaded successfully.",
        bookingPresentationMapper.toResponse(getInvoiceDetailUseCase.getInvoiceDetail(invoiceId)));
  }

  @GetMapping("/api/bookings/{id}/incidentals")
  @PreAuthorize(INCIDENTAL_RECEIPT_CREATE_PERMISSION)
  @Operation(
      summary = "List incidental receipts",
      description =
          "Returns all incidental receipts already recorded for the wedding booking, ordered from newest to oldest.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<List<IncidentalReceiptResponse>> listIncidentalReceipts(
      @PathVariable("id") @Positive(message = "Wedding booking id must be greater than 0.") Long bookingId) {
    List<IncidentalReceiptResponse> incidentalReceipts =
        listIncidentalReceiptsUseCase.listIncidentalReceipts(bookingId).stream()
            .map(bookingPresentationMapper::toResponse)
            .toList();

    return ApiResponse.success("Incidental receipts loaded successfully.", incidentalReceipts);
  }

  @GetMapping("/api/bookings/{id}/deposit-slip")
  @PreAuthorize(WEDDING_BOOKING_VIEW_PERMISSION)
  @Operation(
      summary = "Get deposit slip",
      description =
          "Returns the read-only deposit slip snapshot for a wedding booking. This endpoint does not allow any update or delete flow.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<DepositSlipResponse> getDepositSlip(
      @PathVariable("id") @Positive(message = "Wedding booking id must be greater than 0.") Long bookingId) {
    // Endpoint này phục vụ BM10 đọc/in lại phiếu cọc, nên chỉ trả đúng phần snapshot cần xem.
    return ApiResponse.success(
        "Deposit slip loaded successfully.",
        bookingPresentationMapper.toResponse(getDepositSlipUseCase.getDepositSlip(bookingId)));
  }

  @PatchMapping("/api/bookings/{id}/deposit-slip")
  @PreAuthorize(WEDDING_BOOKING_VIEW_PERMISSION)
  public ApiResponse<Void> updateDepositSlip(
      @PathVariable("id") @Positive(message = "Wedding booking id must be greater than 0.") Long bookingId) {
    throw new AccessDeniedException("Deposit slip is read-only.");
  }

  @DeleteMapping("/api/bookings/{id}/deposit-slip")
  @PreAuthorize(WEDDING_BOOKING_VIEW_PERMISSION)
  public ApiResponse<Void> deleteDepositSlip(
      @PathVariable("id") @Positive(message = "Wedding booking id must be greater than 0.") Long bookingId) {
    throw new AccessDeniedException("Deposit slip is read-only.");
  }

  @PostMapping("/api/bookings")
  @PreAuthorize(WEDDING_BOOKING_CREATE_PERMISSION)
  @Operation(
      summary = "Create wedding booking",
      description =
          "Creates a wedding booking, snapshots menu/service prices, and stores the immutable deposit receipt in one transaction.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<WeddingBookingDetailResponse> createWeddingBooking(
      @Valid @RequestBody CreateWeddingBookingRequest request) {
    return ApiResponse.success(
        "Wedding booking created successfully.",
        bookingPresentationMapper.toResponse(
            createWeddingBookingUseCase.createWeddingBooking(bookingCommandMapper.toCommand(request))));
  }

  @PostMapping("/api/bookings/{id}/incidentals")
  @PreAuthorize(INCIDENTAL_RECEIPT_CREATE_PERMISSION)
  @Operation(
      summary = "Create incidental receipt",
      description =
          "Creates a BM11 incidental receipt when the wedding booking exists, is not cancelled, and has not been fully paid. Service prices are taken from the current service catalog at receipt creation time.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<IncidentalReceiptResponse> createIncidentalReceipt(
      @PathVariable("id") @Positive(message = "Wedding booking id must be greater than 0.") Long bookingId,
      @Valid @RequestBody CreateIncidentalReceiptRequest request) {
    return ApiResponse.success(
        "Incidental receipt created successfully.",
        bookingPresentationMapper.toResponse(
            createIncidentalReceiptUseCase.createIncidentalReceipt(
                bookingId, bookingCommandMapper.toCommand(request))));
  }

  @PostMapping("/api/bookings/{id}/cancel")
  @PreAuthorize(CANCELLATION_RECEIPT_CREATE_PERMISSION)
  @Operation(
      summary = "Create cancellation receipt",
      description =
          "Creates the BM12 cancellation receipt, calculates the deposit refund from the current cancellation rule, and marks the wedding booking as cancelled in one transaction.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<CancellationReceiptResponse> createCancellationReceipt(
      @PathVariable("id") @Positive(message = "Wedding booking id must be greater than 0.") Long bookingId,
      @Valid @RequestBody CreateCancellationReceiptRequest request) {
    return ApiResponse.success(
        "Cancellation receipt created successfully.",
        bookingPresentationMapper.toResponse(
            createCancellationReceiptUseCase.createCancellationReceipt(
                bookingId, bookingCommandMapper.toCommand(request))));
  }

  @GetMapping("/api/bookings/{id}/invoice-preview")
  @PreAuthorize(INVOICE_CREATE_PERMISSION)
  @Operation(
      summary = "Preview invoice",
      description =
          "Calculates the BM4 invoice breakdown, including late payment penalty, without persisting any invoice.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<InvoicePreviewResponse> getInvoicePreview(
      @PathVariable("id") @Positive(message = "Wedding booking id must be greater than 0.") Long bookingId) {
    return ApiResponse.success(
        "Invoice preview loaded successfully.",
        bookingPresentationMapper.toResponse(getInvoicePreviewUseCase.getInvoicePreview(bookingId)));
  }

  @PostMapping("/api/bookings/{id}/invoice")
  @PreAuthorize(INVOICE_CREATE_PERMISSION)
  @Operation(
      summary = "Create invoice",
      description =
          "Creates the BM4 invoice, snapshots the computed payment totals, and marks the wedding booking as fully paid in one transaction.")
  @SecurityRequirement(name = "bearerAuth")
  public ApiResponse<InvoiceResponse> createInvoice(
      @PathVariable("id") @Positive(message = "Wedding booking id must be greater than 0.") Long bookingId,
      @RequestBody(required = false) CreateInvoiceRequest request) {
    return ApiResponse.success(
        "Invoice created successfully.",
        bookingPresentationMapper.toResponse(
            createInvoiceUseCase.createInvoice(bookingId, bookingCommandMapper.toCommand(request))));
  }
}
