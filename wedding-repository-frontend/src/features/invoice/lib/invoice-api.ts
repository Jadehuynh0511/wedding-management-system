import { backendRequest } from "@/shared/api/backend-client";
import type {
  BookingLookupFilters,
  WeddingBookingMenuItem,
  WeddingBookingPage,
  WeddingBookingService,
} from "@/features/booking-lookup/model/booking-lookup";
import type { IncidentalReceiptResponse } from "@/features/incidental-receipt/lib/incidental-api";

export type InvoiceFilters = {
  groomName: string;
  brideName: string;
  hallId: string;
  celebrationDate: string;
};

export type InvoicePreview = {
  weddingBookingId: number;
  calculatedAt: string;
  graceDeadlineAt: string;
  hallTotalAmount: number;
  menuItemsTotalAmount: number;
  servicesTotalAmount: number;
  incidentalsTotalAmount: number;
  subtotalAmount: number;
  depositAmount: number;
  outstandingAmount: number;
  latePaymentPenaltyEnabled: boolean;
  latePaymentPenaltyRate: number;
  latePaymentPenaltyDays: number;
  latePaymentPenaltyAmount: number;
  finalAmount: number;
};

export type InvoiceResponse = InvoicePreview & {
  id: number;
  userId: number | null;
  paidAt: string;
  notes: string | null;
};

export type InvoiceSummary = {
  id: number;
  weddingBookingId: number;
  hallId: number;
  hallName: string;
  shiftId: number;
  shiftName: string;
  groomName: string;
  brideName: string;
  coupleName: string;
  celebrationDate: string;
  paidAt: string;
  finalAmount: number;
};

export type InvoicePage = {
  items: InvoiceSummary[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
};

export type InvoiceDetail = {
  id: number;
  weddingBookingId: number;
  userId: number | null;
  hallId: number;
  hallName: string;
  shiftId: number;
  shiftName: string;
  groomName: string;
  brideName: string;
  coupleName: string;
  groomPhoneNumber: string | null;
  bridePhoneNumber: string;
  bookingDate: string;
  celebrationDate: string;
  tableCount: number;
  reservedTableCount: number;
  tablePrice: number;
  paidAt: string;
  hallTotalAmount: number;
  menuItemsTotalAmount: number;
  servicesTotalAmount: number;
  incidentalsTotalAmount: number;
  subtotalAmount: number;
  depositAmount: number;
  outstandingAmount: number;
  latePaymentPenaltyAmount: number;
  finalAmount: number;
  notes: string | null;
  menuItems: WeddingBookingMenuItem[];
  services: WeddingBookingService[];
  incidentalReceipts: IncidentalReceiptResponse[];
};

export async function fetchInvoicePreview(
  bookingId: number,
  accessToken?: string,
): Promise<InvoicePreview> {
  return backendRequest<InvoicePreview>(
    `/bookings/${bookingId}/invoice-preview`,
    { accessToken },
    "Khong the tai tong ket hoa don.",
  );
}

export async function createInvoice(
  bookingId: number,
  notes?: string | null,
): Promise<InvoiceResponse> {
  return backendRequest<InvoiceResponse>(
    `/bookings/${bookingId}/invoice`,
    { method: "POST", jsonBody: { notes: notes?.trim() || null } },
    "Khong the lap hoa don.",
  );
}

export async function fetchInvoiceBookingCandidates(
  filters: BookingLookupFilters,
  page: number,
  size: number,
  accessToken?: string,
): Promise<WeddingBookingPage> {
  const params = new URLSearchParams();
  params.set("page", String(page));
  params.set("size", String(size));

  if (filters.groomName.trim()) params.set("groomName", filters.groomName.trim());
  if (filters.brideName.trim()) params.set("brideName", filters.brideName.trim());
  if (filters.hallId) params.set("hallId", filters.hallId);
  if (filters.celebrationDate) params.set("date", filters.celebrationDate);
  if (filters.status) params.set("status", filters.status);

  return backendRequest<WeddingBookingPage>(
    `/bookings?${params.toString()}`,
    { accessToken },
    "Khong the tai danh sach tiec cuoi.",
  );
}

export async function fetchInvoices(
  filters: InvoiceFilters,
  page: number,
  size: number,
  accessToken?: string,
): Promise<InvoicePage> {
  const params = new URLSearchParams();
  params.set("page", String(page));
  params.set("size", String(size));

  if (filters.groomName.trim()) params.set("groomName", filters.groomName.trim());
  if (filters.brideName.trim()) params.set("brideName", filters.brideName.trim());
  if (filters.hallId) params.set("hallId", filters.hallId);
  if (filters.celebrationDate) params.set("date", filters.celebrationDate);

  return backendRequest<InvoicePage>(
    `/invoices?${params.toString()}`,
    { accessToken },
    "Khong the tai danh sach hoa don.",
  );
}

export async function fetchInvoiceDetail(
  invoiceId: number,
  accessToken?: string,
): Promise<InvoiceDetail> {
  return backendRequest<InvoiceDetail>(
    `/invoices/${invoiceId}`,
    { accessToken },
    "Khong the tai chi tiet hoa don.",
  );
}
