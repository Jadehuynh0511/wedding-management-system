export type WeddingBookingStatus =
  | "CHO_XAC_NHAN"
  | "DA_XAC_NHAN"
  | "DANG_DIEN_RA"
  | "DA_THANH_TOAN"
  | "DA_HUY";

export type PaymentMethod = "TIEN_MAT" | "CHUYEN_KHOAN" | "THE";

export const PAYMENT_METHOD_LABEL: Record<PaymentMethod, string> = {
  TIEN_MAT: "Tiền mặt",
  CHUYEN_KHOAN: "Chuyển khoản",
  THE: "Thẻ"
};

export type AvailableHall = {
  id: number;
  hallTypeId: number;
  hallTypeName: string;
  minimumTablePrice: number;
  hallName: string;
  maxCapacity: number;
  tablePrice: number;
  status: string;
  description: string | null;
};

// Form state — tất cả state của form booking
export type BookingFormState = {
  // Step 1: Thông tin khách hàng + tiệc
  groomName: string;
  brideName: string;
  bridePhoneNumber: string;
  groomPhoneNumber: string;
  celebrationDate: string; // YYYY-MM-DD
  shiftId: string;
  hallId: string;
  tableCount: string;
  reservedTableCount: string;
  depositAmount: string;
  paymentMethod: PaymentMethod;
  notes: string;

  // Step 2: Món ăn
  menuItems: BookingMenuItem[];

  // Step 3: Dịch vụ
  services: BookingService[];
};

export type BookingMenuItem = {
  menuItemId: number;
  menuItemName: string;
  itemCategory: string;
  pricePerUnit: number;
  quantity: number;
  notes: string;
};

export type BookingService = {
  serviceId: number;
  serviceName: string;
  unitName: string;
  pricePerUnit: number;
  quantity: number;
  notes: string;
};

export const DEFAULT_FORM_STATE: BookingFormState = {
  groomName: "",
  brideName: "",
  bridePhoneNumber: "",
  groomPhoneNumber: "",
  celebrationDate: "",
  shiftId: "",
  hallId: "",
  tableCount: "",
  reservedTableCount: "0",
  depositAmount: "",
  paymentMethod: "CHUYEN_KHOAN",
  notes: "",
  menuItems: [],
  services: []
};

// Tính toán chi phí
export function calcCost(state: BookingFormState, tablePrice: number, minimumDepositPct: number) {
  const tableCount = parseInt(state.tableCount) || 0;
  const reservedCount = parseInt(state.reservedTableCount) || 0;
  const hallTotal = tableCount * tablePrice;
  const menuTotal = state.menuItems.reduce((s, m) => s + m.pricePerUnit * m.quantity * tableCount, 0);
  const serviceTotal = state.services.reduce((s, sv) => s + sv.pricePerUnit * sv.quantity, 0);
  const grandTotal = hallTotal + menuTotal + serviceTotal;
  const minDeposit = (hallTotal * minimumDepositPct) / 100;
  const depositAmount = parseFloat(state.depositAmount) || 0;
  const remaining = grandTotal - depositAmount;
  return { tableCount, reservedCount, hallTotal, menuTotal, serviceTotal, grandTotal, minDeposit, depositAmount, remaining };
}
