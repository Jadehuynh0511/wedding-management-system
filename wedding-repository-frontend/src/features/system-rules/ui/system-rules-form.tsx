"use client";

import { useState } from "react";
import { Info, Save, AlertTriangle, CheckCircle2 } from "lucide-react";

import { cn } from "@/lib/utils";
import {
  updateDepositRule,
  updatePenaltyRule,
  updateCancellationRule,
} from "@/features/system-rules/lib/system-rules-api";
import type { SystemSettings } from "@/features/system-rules/model/system-rules";
import type { AuditLog } from "@/features/audit-logs/model/audit-log";

type SystemRulesFormProps = {
  initial: SystemSettings;
  recentChanges: AuditLog[];
  isAdmin: boolean;
};

function formatDateTime(iso: string) {
  return new Date(iso).toLocaleString("vi-VN", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });
}

export function SystemRulesForm({
  initial,
  recentChanges,
  isAdmin,
}: SystemRulesFormProps) {
  // QĐ2
  const [depositPct, setDepositPct] = useState(
    String(initial.depositRule.minimumDepositPercentage),
  );

  // QĐ4
  const [penaltyEnabled, setPenaltyEnabled] = useState(
    initial.latePaymentPenaltyRule.latePaymentPenaltyEnabled,
  );
  const [penaltyRate, setPenaltyRate] = useState(
    String(initial.latePaymentPenaltyRule.latePaymentPenaltyRate),
  );

  // QĐ12
  const [cancellationDays, setCancellationDays] = useState(
    String(initial.cancellationRule.cancellationDeadlineDays),
  );
  const [refundPct, setRefundPct] = useState(
    String(initial.cancellationRule.depositRefundPercentage),
  );

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [showConfirm, setShowConfirm] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [savedAt, setSavedAt] = useState<string | null>(null);

  function validate(): boolean {
    const next: Record<string, string> = {};
    const dep = parseFloat(depositPct);
    if (isNaN(dep) || dep <= 0 || dep > 100)
      next.depositPct = "Tỉ lệ cọc phải trong khoảng 0 < x ≤ 100%.";

    if (penaltyEnabled) {
      const rate = parseFloat(penaltyRate);
      if (isNaN(rate) || rate <= 0 || rate > 100)
        next.penaltyRate = "Tỉ lệ phạt phải trong khoảng 0 < x ≤ 100%.";
    }

    const days = parseInt(cancellationDays);
    if (isNaN(days) || days < 1) next.cancellationDays = "Số ngày phải ≥ 1.";

    const refund = parseFloat(refundPct);
    if (isNaN(refund) || refund < 0 || refund > 100)
      next.refundPct = "Tỉ lệ hoàn cọc phải trong khoảng 0 ≤ x ≤ 100%.";

    setErrors(next);
    return Object.keys(next).length === 0;
  }

  function handleSaveClick() {
    if (!validate()) return;
    setShowConfirm(true);
  }

  async function handleConfirm() {
    setSubmitError(null);
    setSubmitting(true);
    setShowConfirm(false);

    try {
      await updateDepositRule({
        minimumDepositPercentage: parseFloat(depositPct),
      });
      await updatePenaltyRule({
        latePaymentPenaltyEnabled: penaltyEnabled,
        latePaymentPenaltyRate: parseFloat(penaltyRate || "0"),
      });
      await updateCancellationRule({
        cancellationDeadlineDays: parseInt(cancellationDays),
        depositRefundPercentage: parseFloat(refundPct),
      });
      setSavedAt(new Date().toISOString());
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : "Đã có lỗi xảy ra.");
    } finally {
      setSubmitting(false);
    }
  }

  const disabled = !isAdmin || submitting;

  return (
    <div>
      {/* Header */}
      <div className="mb-6 flex items-start justify-between">
        <div>
          <h1 className="text-[20px] font-bold tracking-wide text-gray-800">THAY ĐỔI QUY ĐỊNH</h1>
          <p className="mt-1 text-[13px] text-gray-500">
            Cấu hình các quy định nghiệp vụ của hệ thống
          </p>
        </div>
        <button
          onClick={handleSaveClick}
          disabled={disabled}
          className={cn(
            "flex items-center gap-1.5 rounded-lg px-4 py-2 text-[12px] font-semibold text-white transition-colors",
            disabled ? "bg-rose-300 cursor-not-allowed" : "bg-rose-500 hover:bg-rose-600",
          )}
        >
          <Save className="h-3.5 w-3.5" />
          {submitting ? "Đang lưu..." : "Lưu thay đổi"}
        </button>
      </div>

      {/* Info banner */}
      <div className="mb-5 flex items-start gap-2 rounded-lg border border-blue-100 bg-blue-50 px-4 py-3">
        <Info className="mt-0.5 h-3.5 w-3.5 flex-shrink-0 text-blue-500" />
        <p className="text-[12px] text-blue-700">
          Các quy định được lưu dưới dạng tham số để dễ thay đổi.
          <span className="font-semibold"> Chỉ Admin mới được phép cập nhật.</span>
        </p>
      </div>

      {!isAdmin && (
        <div className="mb-5 flex items-start gap-2 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3">
          <AlertTriangle className="mt-0.5 h-3.5 w-3.5 flex-shrink-0 text-amber-500" />
          <p className="text-[12px] text-amber-700">
            Bạn đang xem ở chế độ <span className="font-semibold">chỉ đọc</span>. Liên hệ Admin để
            thay đổi quy định.
          </p>
        </div>
      )}

      {savedAt && (
        <div className="mb-5 flex items-center gap-2 rounded-lg border border-green-200 bg-green-50 px-4 py-3">
          <CheckCircle2 className="h-3.5 w-3.5 text-green-600" />
          <p className="text-[12px] text-green-700 font-semibold">
            Đã lưu thành công lúc {formatDateTime(savedAt)}. Thay đổi có hiệu lực từ đây.
          </p>
        </div>
      )}

      {submitError && (
        <div className="mb-5 rounded-lg border border-rose-200 bg-rose-50 px-4 py-3 text-[12px] text-rose-700">
          {submitError}
        </div>
      )}

      {/* 3 Cards */}
      <div className="grid grid-cols-3 gap-5">
        {/* Card A: QĐ2 — Đặt cọc */}
        <div className="rounded-xl border border-gray-100 bg-white p-5">
          <p className="text-[11px] font-bold uppercase tracking-wide text-black-400 mb-1">
            A. Quy định đặt cọc
          </p>
          <p className="text-[12px] text-gray-500 mb-5">Áp dụng cho tất cả tiệc cưới mới</p>

          <div>
            <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
              Tỉ lệ đặt cọc tối thiểu
            </label>
            <div className="flex items-center gap-2">
              <input
                type="number"
                min={1}
                max={100}
                value={depositPct}
                onChange={(e) => setDepositPct(e.target.value)}
                disabled={disabled}
                className={cn(
                  "w-24 rounded-lg border px-3 py-2 text-[13px] text-gray-800 outline-none transition focus:ring-2 focus:ring-rose-100",
                  disabled ? "bg-gray-50 text-gray-400" : "border-gray-200 focus:border-rose-400",
                  errors.depositPct ? "border-rose-400" : "",
                )}
              />
              <span className="text-[12px] text-gray-500">% tổng tạm tính</span>
            </div>
            {errors.depositPct ? (
              <p className="mt-1.5 text-[11px] text-rose-500">{errors.depositPct}</p>
            ) : (
              <p className="mt-1.5 text-[11px] text-gray-400">
                Khách hàng phải cọc tối thiểu {depositPct || "?"}% tổng tiền tạm tính khi đặt tiệc.
              </p>
            )}
          </div>
        </div>

        {/* Card B: QĐ4 — Phạt trễ */}
        <div className="rounded-xl border border-gray-100 bg-white p-5">
          <p className="text-[11px] font-bold uppercase tracking-wide text-black-400 mb-1">
            B. Quy định phạt thanh toán trễ
          </p>
          <p className="text-[12px] text-gray-500 mb-5">Phạt theo ngày trễ thanh toán</p>

          {/* Toggle */}
          <div className="mb-4 flex items-center justify-between">
            <label className="text-[12px] font-semibold text-gray-700">Bật áp dụng phạt</label>
            <button
              type="button"
              onClick={() => !disabled && setPenaltyEnabled((v) => !v)}
              disabled={disabled}
              className={cn(
                "relative h-6 w-11 rounded-full transition-colors duration-200 disabled:cursor-not-allowed",
                penaltyEnabled ? "bg-green-500" : "bg-gray-200",
              )}
            >
              <span
                className={cn(
                  "absolute top-0.5 h-5 w-5 rounded-full bg-white shadow-md transition-all duration-200",
                  penaltyEnabled ? "right-0.5 left-auto" : "left-0.5 right-auto",
                )}
              />
            </button>
          </div>

          {/* Rate input */}
          <div>
            <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
              Tỉ lệ phạt mỗi ngày
            </label>
            <div className="flex items-center gap-2">
              <input
                type="number"
                min={0.01}
                max={100}
                step={0.1}
                value={penaltyRate}
                onChange={(e) => setPenaltyRate(e.target.value)}
                disabled={disabled || !penaltyEnabled}
                className={cn(
                  "w-24 rounded-lg border px-3 py-2 text-[13px] outline-none transition focus:ring-2 focus:ring-rose-100",
                  disabled || !penaltyEnabled
                    ? "bg-gray-50 text-gray-400 border-gray-200"
                    : "text-gray-800 border-gray-200 focus:border-rose-400",
                  errors.penaltyRate ? "border-rose-400" : "",
                )}
              />
              <span className="text-[12px] text-gray-500">% / ngày</span>
            </div>
            {errors.penaltyRate ? (
              <p className="mt-1.5 text-[11px] text-rose-500">{errors.penaltyRate}</p>
            ) : (
              <p className="mt-1.5 text-[11px] text-gray-400">
                {penaltyEnabled
                  ? "Áp dụng khi hóa đơn chưa thanh toán sau ngày tiệc."
                  : "Tính năng phạt đang tắt."}
              </p>
            )}
          </div>
        </div>

        {/* Card C: QĐ12 — Hủy tiệc */}
        <div className="rounded-xl border border-gray-100 bg-white p-5">
          <p className="text-[11px] font-bold uppercase tracking-wide text-black-400 mb-1">
            C. Quy định hủy tiệc
          </p>
          <p className="text-[12px] text-gray-500 mb-5">Tỉ lệ hoàn cọc theo thời gian báo hủy</p>

          <div className="space-y-4">
            <div>
              <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
                Ngưỡng ngày hủy
              </label>
              <div className="flex items-center gap-2">
                <input
                  type="number"
                  min={1}
                  value={cancellationDays}
                  onChange={(e) => setCancellationDays(e.target.value)}
                  disabled={disabled}
                  className={cn(
                    "w-24 rounded-lg border px-3 py-2 text-[13px] outline-none transition focus:ring-2 focus:ring-rose-100",
                    disabled ? "bg-gray-50 text-gray-400" : "text-gray-800 focus:border-rose-400",
                    errors.cancellationDays ? "border-rose-400" : "border-gray-200",
                  )}
                />
                <span className="text-[12px] text-gray-500">ngày trước tiệc</span>
              </div>
              {errors.cancellationDays && (
                <p className="mt-1.5 text-[11px] text-rose-500">{errors.cancellationDays}</p>
              )}
            </div>

            <div>
              <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
                Tỉ lệ hoàn cọc (nếu hủy trước ngưỡng)
              </label>
              <div className="flex items-center gap-2">
                <input
                  type="number"
                  min={0}
                  max={100}
                  value={refundPct}
                  onChange={(e) => setRefundPct(e.target.value)}
                  disabled={disabled}
                  className={cn(
                    "w-24 rounded-lg border px-3 py-2 text-[13px] outline-none transition focus:ring-2 focus:ring-rose-100",
                    disabled ? "bg-gray-50 text-gray-400" : "text-gray-800 focus:border-rose-400",
                    errors.refundPct ? "border-rose-400" : "border-gray-200",
                  )}
                />
                <span className="text-[12px] text-gray-500">%</span>
              </div>
              {errors.refundPct ? (
                <p className="mt-1.5 text-[11px] text-rose-500">{errors.refundPct}</p>
              ) : (
                <p className="mt-1.5 text-[11px] text-gray-400">
                  Hủy sau ngưỡng → hoàn 0%. Mốc tính từ thời điểm thông báo hủy đến ngày tổ chức
                  tiệc.
                </p>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Lịch sử thay đổi */}
      {recentChanges.length > 0 && (
        <div className="mt-6 rounded-xl border border-gray-100 bg-white p-6">
          <h2 className="mb-4 text-[13px] font-bold uppercase tracking-wide text-gray-700">
            Lịch sử thay đổi quy định
          </h2>
          <table className="w-full text-[13px]">
            <thead>
              <tr className="border-b border-gray-100 bg-gray-50">
                <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500 whitespace-nowrap">
                  Thời gian
                </th>
                <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                  Người thực hiện
                </th>
                <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                  Hành động
                </th>
                <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                  Kết quả
                </th>
              </tr>
            </thead>
            <tbody>
              {recentChanges.map((log) => (
                <tr key={log.id} className="border-b border-gray-50">
                  <td className="px-4 py-3 text-[12px] text-gray-600 whitespace-nowrap">
                    {formatDateTime(log.occurredAt)}
                  </td>
                  <td className="px-4 py-3">
                    <p className="font-medium text-gray-800">{log.actorUsername}</p>
                  </td>
                  <td className="px-4 py-3 font-mono text-[12px] text-gray-600">
                    {log.actionCode}
                  </td>
                  <td className="px-4 py-3">
                    <span
                      className={cn(
                        "rounded-full px-2 py-0.5 text-[11px] font-semibold",
                        log.resultStatus === "SUCCESS"
                          ? "bg-green-50 text-green-700"
                          : "bg-rose-50 text-rose-700",
                      )}
                    >
                      {log.resultStatus === "SUCCESS" ? "Thành công" : "Thất bại"}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Confirm dialog */}
      {showConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30">
          <div className="w-full max-w-sm rounded-2xl bg-white p-6 shadow-xl">
            <h2 className="mb-2 text-[15px] font-bold text-gray-800">Xác nhận lưu thay đổi</h2>
            <div className="mb-4 space-y-1.5 text-[13px] text-gray-600">
              <p>Các thay đổi sau sẽ được áp dụng:</p>
              <ul className="mt-2 space-y-1 text-[12px]">
                <li className="flex gap-2">
                  <span className="text-gray-400">QĐ2</span> Tỉ lệ cọc tối thiểu:{" "}
                  <span className="font-semibold text-gray-800">{depositPct}%</span>
                </li>
                <li className="flex gap-2">
                  <span className="text-gray-400">QĐ4</span> Phạt trễ:{" "}
                  <span className="font-semibold text-gray-800">
                    {penaltyEnabled ? `Bật — ${penaltyRate}%/ngày` : "Tắt"}
                  </span>
                </li>
                <li className="flex gap-2">
                  <span className="text-gray-400">QĐ12</span> Hủy trước {cancellationDays} ngày →
                  hoàn <span className="font-semibold text-gray-800">{refundPct}%</span>
                </li>
              </ul>
            </div>
            <div className="rounded-lg bg-amber-50 px-3 py-2 text-[11px] text-amber-700 mb-4">
              ⚡ Thay đổi có hiệu lực ngay từ thời điểm lưu. Các tiệc đã ký kết trước đó không bị
              ảnh hưởng.
            </div>
            <div className="flex justify-end gap-2">
              <button
                onClick={() => setShowConfirm(false)}
                className="rounded-lg border border-gray-200 px-4 py-2 text-[12px] font-semibold text-gray-600 hover:bg-gray-50 transition-colors"
              >
                Hủy
              </button>
              <button
                onClick={handleConfirm}
                className="rounded-lg bg-rose-500 px-4 py-2 text-[12px] font-semibold text-white hover:bg-rose-600 transition-colors"
              >
                Xác nhận lưu
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
