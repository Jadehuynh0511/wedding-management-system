import { backendRequest } from "@/shared/api/backend-client";

export type CreateIncidentalReceiptPayload = {
  notes?: string | null;
  items: Array<{
    serviceId: number;
    quantity: number;
    notes?: string | null;
  }>;
};

export type IncidentalReceiptResponse = {
  id: number;
  weddingBookingId: number;
  userId: number;
  recordedAt: string;
  totalAmount: number;
  notes: string | null;
  items: Array<{
    id: number;
    serviceId: number;
    serviceName: string;
    unitName: string;
    quantity: number;
    appliedUnitPrice: number;
    lineTotal: number;
    notes: string | null;
  }>;
};

export async function createIncidentalReceipt(
  bookingId: number,
  payload: CreateIncidentalReceiptPayload,
): Promise<IncidentalReceiptResponse> {
  return backendRequest<IncidentalReceiptResponse>(
    `/bookings/${bookingId}/incidentals`,
    { method: "POST", jsonBody: payload },
    "Không thể tạo phiếu dịch vụ phát sinh.",
  );
}

export async function fetchIncidentalReceipts(
  bookingId: number,
  accessToken?: string,
): Promise<IncidentalReceiptResponse[]> {
  return backendRequest<IncidentalReceiptResponse[]>(
    `/bookings/${bookingId}/incidentals`,
    { accessToken },
    "Không thể tải danh sách phiếu dịch vụ phát sinh.",
  );
}
