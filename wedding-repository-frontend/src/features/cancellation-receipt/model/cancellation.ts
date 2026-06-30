import type { WeddingBookingDetail } from "@/features/booking-lookup/model/booking-lookup";
import type { SystemSettings } from "@/features/system-rules/model/system-rules";

export type CancellationPreview = {
  cancellationDate: string;
  daysBeforeCelebration: number;
  appliedRefundPercentage: number;
  refundAmount: number;
  retainedAmount: number;
};

function parseLocalDate(value: string) {
  const [year, month, day] = value.split("-").map(Number);
  return new Date(year, month - 1, day);
}

export function formatDateForInput(value: Date) {
  const year = value.getFullYear();
  const month = String(value.getMonth() + 1).padStart(2, "0");
  const day = String(value.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

export function calculateCancellationPreview(
  booking: WeddingBookingDetail,
  settings: SystemSettings,
  cancellationDate: string,
): CancellationPreview {
  const cancellationDay = parseLocalDate(cancellationDate);
  const celebrationDay = parseLocalDate(booking.celebrationDate);
  const millisecondsPerDay = 24 * 60 * 60 * 1000;
  const daysBeforeCelebration = Math.floor(
    (celebrationDay.getTime() - cancellationDay.getTime()) / millisecondsPerDay,
  );
  const refundPercentage =
    daysBeforeCelebration > settings.cancellationRule.cancellationDeadlineDays
      ? settings.cancellationRule.depositRefundPercentage
      : 0;
  const depositAmount = booking.depositReceipt.amount;
  const refundAmount = (depositAmount * refundPercentage) / 100;

  return {
    cancellationDate,
    daysBeforeCelebration,
    appliedRefundPercentage: refundPercentage,
    refundAmount,
    retainedAmount: depositAmount - refundAmount,
  };
}
