import { backendRequest } from "@/shared/api/backend-client";

export type CancellationReceiptResponse = {
  id: number;
  weddingBookingId: number;
  userId: number;
  cancelledAt: string;
  daysBeforeCelebration: number;
  cancellationDeadlineDays: number;
  configuredDepositRefundPercentage: number;
  appliedDepositRefundPercentage: number;
  refundAmount: number;
  reason: string;
};

export async function createCancellationReceipt(
  bookingId: number,
  reason: string,
): Promise<CancellationReceiptResponse> {
  return backendRequest<CancellationReceiptResponse>(
    `/bookings/${bookingId}/cancel`,
    {
      method: "POST",
      jsonBody: { reason },
    },
    "Không thể lưu phiếu hủy tiệc.",
  );
}
