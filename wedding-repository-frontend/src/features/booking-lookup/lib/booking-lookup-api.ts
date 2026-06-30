import { backendRequest } from "@/shared/api/backend-client";
import type {
  BookingLookupFilters,
  WeddingBookingDetail,
  WeddingBookingPage,
} from "@/features/booking-lookup/model/booking-lookup";

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

export async function fetchWeddingBookings(
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
    "Không thể tải danh sách tiệc cưới.",
  );
}

export async function fetchWeddingBookingDetail(
  bookingId: number,
  accessToken?: string,
): Promise<WeddingBookingDetail> {
  return backendRequest<WeddingBookingDetail>(
    `/bookings/${bookingId}`,
    { accessToken },
    "Không thể tải chi tiết tiệc cưới.",
  );
}

export async function fetchInvoicePreview(
  bookingId: number,
  accessToken?: string,
): Promise<InvoicePreview> {
  return backendRequest<InvoicePreview>(
    `/bookings/${bookingId}/invoice-preview`,
    { accessToken },
    "Không thể tải tổng kết hóa đơn tạm tính.",
  );
}
