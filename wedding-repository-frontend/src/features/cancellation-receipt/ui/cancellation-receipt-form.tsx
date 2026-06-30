"use client";

import { useMemo, useState } from "react";
import { ArrowLeft, Printer, Save } from "lucide-react";

import { cn } from "@/lib/utils";
import type { WeddingBookingDetail } from "@/features/booking-lookup/model/booking-lookup";
import { createCancellationReceipt } from "@/features/cancellation-receipt/lib/cancellation-api";
import {
  calculateCancellationPreview,
  formatDateForInput,
} from "@/features/cancellation-receipt/model/cancellation";
import type { SystemSettings } from "@/features/system-rules/model/system-rules";

type CancellationReceiptFormProps = {
  bookingDetail: WeddingBookingDetail;
  settings: SystemSettings;
};

function formatCurrency(value: number) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value);
}

function getCancellationTimingText(daysBeforeCelebration: number) {
  if (daysBeforeCelebration < 0) {
    return `Tiệc đã qua ${Math.abs(daysBeforeCelebration)} ngày so với ngày đãi`;
  }

  if (daysBeforeCelebration === 0) {
    return "Tiệc diễn ra trong hôm nay";
  }

  return `Tiệc còn ${daysBeforeCelebration} ngày trước ngày đãi`;
}

export function CancellationReceiptForm({ bookingDetail, settings }: CancellationReceiptFormProps) {
  const [reason, setReason] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const cancellationDate = formatDateForInput(new Date());

  const preview = useMemo(
    () => calculateCancellationPreview(bookingDetail, settings, cancellationDate),
    [bookingDetail, cancellationDate, settings],
  );
  const isCelebrationPast = preview.daysBeforeCelebration < 0;
  const cancellationTimingText = getCancellationTimingText(preview.daysBeforeCelebration);

  async function handleSubmit() {
    if (isCelebrationPast) {
      setSubmitError("Không thể lập phiếu hủy cho tiệc đã qua ngày đãi.");
      return;
    }

    const trimmedReason = reason.trim();
    if (!trimmedReason) {
      setSubmitError("Vui lòng nhập lý do hủy tiệc.");
      return;
    }

    setSubmitting(true);
    setSubmitError(null);
    try {
      await createCancellationReceipt(bookingDetail.id, trimmedReason);
      window.location.assign(`/dashboard/bookings/${bookingDetail.id}`);
    } catch (error) {
      setSubmitError(error instanceof Error ? error.message : "Không thể lưu phiếu hủy tiệc.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <div className="mb-4 flex items-start justify-between">
        <div>
          <h1 className="text-[20px] font-bold tracking-wide text-gray-800">
            LẬP PHIẾU HỦY TIỆC CƯỚI
          </h1>
          <p className="mt-1 text-[13px] text-gray-500">
            Ghi nhận thông tin hủy tiệc và hoàn cọc theo quy định
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => window.location.assign(`/dashboard/bookings/${bookingDetail.id}`)}
            className="rounded-lg border border-gray-200 px-3 py-2 text-[12px] font-semibold text-gray-600 hover:bg-gray-50"
          >
            <span className="inline-flex items-center gap-1.5">
              <ArrowLeft className="h-3.5 w-3.5" />
              Hủy
            </span>
          </button>
          <button
            type="button"
            onClick={() => window.print()}
            className="rounded-lg border border-gray-200 px-3 py-2 text-[12px] font-semibold text-gray-600 hover:bg-gray-50"
          >
            <span className="inline-flex items-center gap-1.5">
              <Printer className="h-3.5 w-3.5" />
              In phiếu
            </span>
          </button>
          <button
            type="button"
            onClick={handleSubmit}
            disabled={submitting || isCelebrationPast}
            className={cn(
              "rounded-lg px-3 py-2 text-[12px] font-semibold text-white",
              submitting || isCelebrationPast
                ? "cursor-not-allowed bg-rose-300"
                : "bg-rose-500 hover:bg-rose-600",
            )}
          >
            <span className="inline-flex items-center gap-1.5">
              <Save className="h-3.5 w-3.5" />
              {submitting ? "Đang lưu..." : "Lưu phiếu hủy"}
            </span>
          </button>
        </div>
      </div>

      <div className="rounded-xl border border-gray-100 bg-white p-4">
        <div className="grid grid-cols-3 gap-3">
          <ReadonlyField label="Mã tiệc" value={`TC${String(bookingDetail.id).padStart(4, "0")}`} />
          <ReadonlyField
            label="Cô dâu - Chú rể"
            value={`${bookingDetail.brideName} - ${bookingDetail.groomName}`}
          />
          <ReadonlyField label="Ngày hủy" value={cancellationDate} />
        </div>

        <div className="mt-3">
          <label className="mb-1 block text-[11px] font-semibold uppercase tracking-wide text-gray-500">
            Lý do hủy
          </label>
          <textarea
            value={reason}
            onChange={(event) => setReason(event.target.value)}
            rows={4}
            placeholder="Nhập lý do hủy tiệc..."
            className="w-full rounded-lg border border-gray-200 px-3 py-2 text-[13px] text-gray-700 outline-none focus:border-rose-300"
          />
        </div>

        <div className="mt-3 grid grid-cols-3 gap-3">
          <ReadonlyField
            label="Tiền cọc"
            value={formatCurrency(bookingDetail.depositReceipt.amount)}
          />
          <ReadonlyField label="Số tiền hoàn" value={formatCurrency(preview.refundAmount)} />
          <ReadonlyField label="Số tiền giữ lại" value={formatCurrency(preview.retainedAmount)} />
        </div>

        <div
          className={cn(
            "mt-3 rounded-lg border px-3 py-2 text-[12px]",
            isCelebrationPast
              ? "border-rose-100 bg-rose-50 text-rose-700"
              : "border-amber-100 bg-amber-50 text-amber-700",
          )}
        >
          QĐ12: Hủy trước {settings.cancellationRule.cancellationDeadlineDays} ngày được hoàn{" "}
          {settings.cancellationRule.depositRefundPercentage}% tiền cọc. {cancellationTimingText},
          áp dụng hoàn {preview.appliedRefundPercentage}%.
          {isCelebrationPast ? " Không thể lưu phiếu hủy cho tiệc đã qua ngày đãi." : ""}
        </div>

        {submitError && <p className="mt-3 text-[12px] text-rose-600">{submitError}</p>}
      </div>
    </div>
  );
}

function ReadonlyField({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <label className="mb-1 block text-[11px] font-semibold uppercase tracking-wide text-gray-500">
        {label}
      </label>
      <input
        value={value}
        disabled
        className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2 text-[13px] text-gray-600"
      />
    </div>
  );
}
