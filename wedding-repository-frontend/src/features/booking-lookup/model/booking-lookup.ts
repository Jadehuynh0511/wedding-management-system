export type WeddingBookingStatus = "CHO_XAC_NHAN" | "DA_XAC_NHAN" | "DANG_DIEN_RA" | "DA_THANH_TOAN" | "DA_HUY";

export type WeddingBookingSummary = {
  id: number;
  hallId: number;
  hallName: string;
  shiftId: number;
  shiftName: string;
  groomName: string;
  brideName: string;
  coupleName: string;
  celebrationDate: string;
  tableCount: number;
  status: WeddingBookingStatus;
};

export type WeddingBookingPage = {
  items: WeddingBookingSummary[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
};

export type WeddingBookingMenuItem = {
  id: number;
  menuItemId: number;
  menuItemName: string;
  quantity: number;
  priceSnapshot: number;
  lineTotal: number;
  notes: string | null;
};

export type WeddingBookingService = {
  id: number;
  serviceId: number;
  serviceName: string;
  unitName: string;
  quantity: number;
  priceSnapshot: number;
  lineTotal: number;
  notes: string | null;
};

export type DepositReceipt = {
  id: number;
  weddingBookingId: number;
  userId: number | null;
  receivedAt: string;
  amount: number;
  paymentMethod: "TIEN_MAT" | "CHUYEN_KHOAN" | "THE";
  notes: string | null;
};

export type WeddingBookingDetail = {
  id: number;
  hallId: number;
  hallName: string;
  shiftId: number;
  shiftName: string;
  groomName: string;
  brideName: string;
  groomPhoneNumber: string | null;
  bridePhoneNumber: string;
  bookingDate: string;
  celebrationDate: string;
  tableCount: number;
  reservedTableCount: number;
  tablePrice: number;
  hallTotalAmount: number;
  status: WeddingBookingStatus;
  notes: string | null;
  menuItems: WeddingBookingMenuItem[];
  services: WeddingBookingService[];
  depositReceipt: DepositReceipt;
};

export type BookingLookupFilters = {
  groomName: string;
  brideName: string;
  hallId: string;
  celebrationDate: string;
  status: "" | WeddingBookingStatus;
};

export const BOOKING_STATUS_OPTIONS: Array<{ value: "" | WeddingBookingStatus; label: string }> = [
  { value: "", label: "Tất cả trạng thái" },
  { value: "CHO_XAC_NHAN", label: "Chờ xác nhận" },
  { value: "DA_XAC_NHAN", label: "Đã đặt cọc" },
  { value: "DANG_DIEN_RA", label: "Đang diễn ra" },
  { value: "DA_THANH_TOAN", label: "Đã thanh toán" },
  { value: "DA_HUY", label: "Đã hủy" }
];

export const BOOKING_STATUS_BADGE: Record<WeddingBookingStatus, string> = {
  CHO_XAC_NHAN: "bg-amber-50 text-amber-700",
  DA_XAC_NHAN: "bg-blue-50 text-blue-700",
  DANG_DIEN_RA: "bg-violet-50 text-violet-700",
  DA_THANH_TOAN: "bg-green-50 text-green-700",
  DA_HUY: "bg-rose-50 text-rose-700"
};
