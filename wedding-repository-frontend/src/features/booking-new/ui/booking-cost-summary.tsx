"use client";

import { AlertTriangle, CheckCircle2 } from "lucide-react";
import { cn } from "@/lib/utils";
import type { BookingFormState } from "@/features/booking-new/model/booking";
import { calcCost } from "@/features/booking-new/model/booking";

type BookingCostSummaryProps = {
  state: BookingFormState;
  tablePrice: number;
  minimumDepositPct: number;
  currentStep: number;
  submitting: boolean;
  onCancel: () => void;
  onSave: () => void;
};

function fmt(n: number) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(n);
}

export function BookingCostSummary({
  state,
  tablePrice,
  minimumDepositPct,
  currentStep,
  submitting,
  onCancel,
  onSave
}: BookingCostSummaryProps) {
  const cost = calcCost(state, tablePrice, minimumDepositPct);
  const depositOk = cost.depositAmount >= cost.minDeposit;
  const canSave = currentStep === 4;

  return (
    <div className="flex flex-col gap-4">
      {/* Header */}
      <div className="flex items-center gap-2">
        <span className="flex h-5 w-5 items-center justify-center rounded-full bg-rose-500 text-[10px] font-bold text-white">5</span>
        <h3 className="text-[12px] font-bold uppercase tracking-wide text-gray-700">Tổng kết chi phí</h3>
      </div>

      {/* Cost rows */}
      <div className="rounded-xl border border-gray-100 bg-white p-4 space-y-2.5 text-[12px]">
        <CostRow
          label="Số lượng bàn"
          value={cost.tableCount > 0
            ? `${cost.tableCount} bàn${cost.reservedCount > 0 ? ` (+${cost.reservedCount} bàn dự trữ)` : ""}`
            : "—"}
          muted
        />
        <CostRow
          label="Đơn giá / bàn"
          value={tablePrice > 0 ? fmt(tablePrice) : "—"}
          muted
        />
        <div className="border-t border-gray-100 pt-2 space-y-2">
          <CostRow label="Tiền bàn tiệc" value={cost.hallTotal > 0 ? fmt(cost.hallTotal) : "—"} />
          <CostRow label="Tiền món ăn" value={cost.menuTotal > 0 ? fmt(cost.menuTotal) : "—"} />
          <CostRow label="Tiền dịch vụ đi kèm" value={cost.serviceTotal > 0 ? fmt(cost.serviceTotal) : "—"} />
        </div>
        <div className="border-t border-gray-100 pt-2">
          <div className="flex items-center justify-between">
            <span className="font-semibold text-gray-700">Tổng tạm tính</span>
            <span className={cn("font-bold", cost.grandTotal > 0 ? "text-rose-600" : "text-gray-400")}>
              {cost.grandTotal > 0 ? fmt(cost.grandTotal) : "—"}
            </span>
          </div>
        </div>
        <div className="border-t border-gray-100 pt-2 space-y-2">
          <CostRow
            label={`Tiền cọc tối thiểu (${minimumDepositPct}%)`}
            value={cost.minDeposit > 0 ? fmt(cost.minDeposit) : "—"}
            muted
          />
          <div className="flex items-center justify-between">
            <span className={cn("font-semibold", !depositOk && cost.depositAmount > 0 ? "text-rose-500" : "text-gray-700")}>
              Tiền đặt cọc
            </span>
            <span className={cn("font-bold", !depositOk && cost.depositAmount > 0 ? "text-rose-500" : "text-gray-800")}>
              {cost.depositAmount > 0 ? fmt(cost.depositAmount) : "—"}
            </span>
          </div>
          {!depositOk && cost.depositAmount > 0 && (
            <p className="text-[11px] text-rose-500">Chưa đủ mức cọc tối thiểu {minimumDepositPct}%</p>
          )}
          <CostRow
            label="Còn lại phải thu"
            value={cost.grandTotal > 0 ? fmt(cost.remaining) : "—"}
          />
        </div>
      </div>

      {/* Warning */}
      {cost.grandTotal > 0 && (
        <div className={cn(
          "rounded-lg border px-3 py-2.5 text-[11px]",
          depositOk
            ? "border-green-200 bg-green-50 text-green-700"
            : "border-amber-200 bg-amber-50 text-amber-700"
        )}>
          {depositOk ? (
            <div className="flex items-start gap-1.5">
              <CheckCircle2 className="mt-0.5 h-3 w-3 flex-shrink-0" />
              <span>Tiền cọc đã đủ theo quy định hiện hành.</span>
            </div>
          ) : (
            <div className="flex items-start gap-1.5">
              <AlertTriangle className="mt-0.5 h-3 w-3 flex-shrink-0" />
              <span>
                Khách hàng phải đặt cọc tối thiểu <span className="font-bold">{minimumDepositPct}%</span> tổng tạm tính theo quy định hiện hành. Hóa đơn cuối lập vào ngày diễn ra tiệc.
              </span>
            </div>
          )}
        </div>
      )}

      {/* Actions */}
      {canSave && (
        <div className="flex gap-2">
          <button
            type="button"
            onClick={onCancel}
            className="flex-1 rounded-lg border border-gray-200 py-2 text-[12px] font-semibold text-gray-600 hover:bg-gray-50 transition-colors"
          >
            Hủy
          </button>
          <button
            type="button"
            onClick={onSave}
            disabled={submitting}
            className={cn(
              "flex-1 rounded-lg py-2 text-[12px] font-semibold text-white transition-colors",
              submitting ? "bg-rose-300 cursor-not-allowed" : "bg-rose-500 hover:bg-rose-600"
            )}
          >
            {submitting ? "Đang lưu..." : "Lưu phiếu"}
          </button>
        </div>
      )}
    </div>
  );
}

function CostRow({ label, value, muted }: { label: string; value: string; muted?: boolean }) {
  return (
    <div className="flex items-center justify-between">
      <span className={cn("text-[12px]", muted ? "text-gray-400" : "text-gray-600")}>{label}</span>
      <span className={cn("text-[12px]", muted ? "text-gray-400" : "text-gray-700")}>{value}</span>
    </div>
  );
}