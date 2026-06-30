package com.uit.weddingmanagement.modules.booking.infrastructure.persistence.entity;

import com.uit.weddingmanagement.common.entity.BaseEntity;
import com.uit.weddingmanagement.modules.booking.domain.model.WeddingBookingStatus;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity.HallJpaEntity;
import com.uit.weddingmanagement.modules.catalog.infrastructure.persistence.entity.ShiftJpaEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "wedding_bookings")
public class WeddingBookingJpaEntity extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "hall_id", nullable = false)
  private HallJpaEntity hall;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "shift_id", nullable = false)
  private ShiftJpaEntity shift;

  @Column(name = "groom_name", nullable = false, length = 150)
  private String groomName;

  @Column(name = "bride_name", nullable = false, length = 150)
  private String brideName;

  @Column(name = "groom_phone_number", length = 20)
  private String groomPhoneNumber;

  @Column(name = "bride_phone_number", nullable = false, length = 20)
  private String bridePhoneNumber;

  @Column(name = "booking_date", nullable = false)
  private LocalDate bookingDate;

  @Column(name = "celebration_date", nullable = false)
  private LocalDate celebrationDate;

  @Column(name = "table_count", nullable = false)
  private Integer tableCount;

  @Column(name = "reserved_table_count", nullable = false)
  private Integer reservedTableCount;

  @Column(name = "table_price", nullable = false, precision = 18, scale = 2)
  private BigDecimal tablePrice;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private WeddingBookingStatus status;

  @Column(name = "notes")
  private String notes;

  @OneToMany(mappedBy = "weddingBooking", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<BookingMenuItemJpaEntity> bookingMenuItems = new ArrayList<>();

  @OneToMany(mappedBy = "weddingBooking", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<BookingServiceJpaEntity> bookingServices = new ArrayList<>();

  @OneToOne(mappedBy = "weddingBooking", cascade = CascadeType.ALL, orphanRemoval = true)
  private DepositReceiptJpaEntity depositReceipt;

  // Getters and setters
  public Long getId() {
    return id;
  }

  public HallJpaEntity getHall() {
    return hall;
  }

  public void setHall(HallJpaEntity hall) {
    this.hall = hall;
  }

  public ShiftJpaEntity getShift() {
    return shift;
  }

  public void setShift(ShiftJpaEntity shift) {
    this.shift = shift;
  }

  public String getGroomName() {
    return groomName;
  }

  public void setGroomName(String groomName) {
    this.groomName = groomName;
  }

  public String getBrideName() {
    return brideName;
  }

  public void setBrideName(String brideName) {
    this.brideName = brideName;
  }

  public String getGroomPhoneNumber() {
    return groomPhoneNumber;
  }

  public void setGroomPhoneNumber(String groomPhoneNumber) {
    this.groomPhoneNumber = groomPhoneNumber;
  }

  public String getBridePhoneNumber() {
    return bridePhoneNumber;
  }

  public void setBridePhoneNumber(String bridePhoneNumber) {
    this.bridePhoneNumber = bridePhoneNumber;
  }

  public LocalDate getBookingDate() {
    return bookingDate;
  }

  public void setBookingDate(LocalDate bookingDate) {
    this.bookingDate = bookingDate;
  }

  public LocalDate getCelebrationDate() {
    return celebrationDate;
  }

  public void setCelebrationDate(LocalDate celebrationDate) {
    this.celebrationDate = celebrationDate;
  }

  public Integer getTableCount() {
    return tableCount;
  }

  public void setTableCount(Integer tableCount) {
    this.tableCount = tableCount;
  }

  public Integer getReservedTableCount() {
    return reservedTableCount;
  }

  public void setReservedTableCount(Integer reservedTableCount) {
    this.reservedTableCount = reservedTableCount;
  }

  public BigDecimal getTablePrice() {
    return tablePrice;
  }

  public void setTablePrice(BigDecimal tablePrice) {
    this.tablePrice = tablePrice;
  }

  public WeddingBookingStatus getStatus() {
    return status;
  }

  public void setStatus(WeddingBookingStatus status) {
    this.status = status;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public List<BookingMenuItemJpaEntity> getBookingMenuItems() {
    return bookingMenuItems;
  }

  public void setBookingMenuItems(List<BookingMenuItemJpaEntity> bookingMenuItems) {
    this.bookingMenuItems = bookingMenuItems;
  }

  public List<BookingServiceJpaEntity> getBookingServices() {
    return bookingServices;
  }

  public void setBookingServices(List<BookingServiceJpaEntity> bookingServices) {
    this.bookingServices = bookingServices;
  }

  public DepositReceiptJpaEntity getDepositReceipt() {
    return depositReceipt;
  }

  public void setDepositReceipt(DepositReceiptJpaEntity depositReceipt) {
    this.depositReceipt = depositReceipt;
  }
}
