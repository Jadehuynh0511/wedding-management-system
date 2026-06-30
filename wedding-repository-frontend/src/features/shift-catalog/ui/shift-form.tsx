"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { ArrowLeft, Clock, Save } from "lucide-react";

import { cn } from "@/lib/utils";
import { createShift, updateShift } from "@/features/shift-catalog/lib/shift-api";
import type { Shift, CreateShiftPayload } from "@/features/shift-catalog/model/shift";

type ShiftFormProps = {
  mode: "create" | "edit";
  initial?: Shift;
  existingShifts: Shift[];
};

export function ShiftForm({ mode, initial, existingShifts }: ShiftFormProps) {
  const router = useRouter();
  const nextCode = `CA${String((existingShifts.length + 1)).padStart(2, "0")}`;

  const [shiftName, setShiftName] = useState(initial?.shiftName ?? "");
  const [startTime, setStartTime] = useState(initial?.startTime ?? "");
  const [endTime, setEndTime] = useState(initial?.endTime ?? "");
  const [description, setDescription] = useState(initial?.description ?? "");
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  // Các ca hiện có để hiển thị sidebar (loại bỏ ca đang sửa)
  const otherShifts = existingShifts.filter((s) => s.id !== initial?.id);

  function validate(): boolean {
    const next: Record<string, string> = {};
    if (!shiftName.trim()) next.shiftName = "Tên ca không được để trống.";
    if (!startTime) next.startTime = "Vui lòng nhập giờ bắt đầu.";
    if (!endTime) next.endTime = "Vui lòng nhập giờ kết thúc.";
    if (startTime && endTime && startTime >= endTime) {
      next.endTime = "Giờ kết thúc phải sau giờ bắt đầu.";
    }
    setErrors(next);
    return Object.keys(next).length === 0;
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!validate()) return;

    setSubmitError(null);
    setSubmitting(true);

    const payload: CreateShiftPayload = {
      shiftName: shiftName.trim(),
      startTime,
      endTime,
      description: description.trim() || undefined
    };

    try {
      if (mode === "create") {
        await createShift(payload);
      } else if (initial) {
        await updateShift(initial.id, payload);
      }
      router.push("/dashboard/shifts");
      router.refresh();
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : "Đã có lỗi xảy ra.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      {/* Page header */}
      <div className="mb-6 flex items-start justify-between">
        <div>
          <h1 className="text-[20px] font-bold tracking-wide text-gray-800">
            {mode === "create" ? "THÊM CA TIỆC" : "CHỈNH SỬA CA TIỆC"}
          </h1>
          <p className="mt-1 text-[13px] text-gray-500">
            {mode === "create" ? "Thêm mới một ca phục vụ vào hệ thống" : "Cập nhật thông tin ca tiệc"}
          </p>
        </div>
        <button
          onClick={() => router.push("/dashboard/shifts")}
          className="flex items-center gap-1.5 text-[12px] font-semibold text-gray-500 hover:text-gray-700 transition-colors"
        >
          <ArrowLeft className="h-3.5 w-3.5" />
          Quay lại
        </button>
      </div>

      <div className="flex gap-6">
        {/* Form */}
        <div className="flex-1">
          <form onSubmit={handleSubmit}>
            <div className="rounded-xl border border-gray-100 bg-white p-6">
              <div className="mb-5 flex items-center gap-2">
                <span className="flex h-5 w-5 items-center justify-center rounded-full bg-rose-500 text-[10px] font-bold text-white">1</span>
                <h2 className="text-[13px] font-bold text-gray-700 uppercase tracking-wide">Thông tin ca</h2>
              </div>

              <div className="grid grid-cols-2 gap-4">
                {/* Mã ca */}
                <div>
                  <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
                    Mã ca <span className="text-rose-500">*</span>
                  </label>
                  <input
                    value={mode === "edit" ? `CA${String(initial?.id).padStart(2, "0")}` : nextCode}
                    disabled
                    className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2.5 text-[13px] text-gray-400 outline-none"
                  />
                  <p className="mt-1 text-[11px] text-gray-400">
                    Tự động sinh — ví dụ: {nextCode}
                  </p>
                </div>

                {/* Tên ca */}
                <div>
                  <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
                    Tên ca <span className="text-rose-500">*</span>
                  </label>
                  <input
                    value={shiftName}
                    onChange={(e) => setShiftName(e.target.value)}
                    placeholder="Ví dụ: Sáng, Trưa, Tối, Đêm..."
                    className={cn(
                      "w-full rounded-lg border px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:ring-2 focus:ring-rose-100",
                      errors.shiftName ? "border-rose-400" : "border-gray-200 focus:border-rose-400"
                    )}
                  />
                  {errors.shiftName ? (
                    <p className="mt-1 text-[11px] text-rose-500">{errors.shiftName}</p>
                  ) : (
                    <p className="mt-1 text-[11px] text-gray-400">Không được trùng với tên ca đã có.</p>
                  )}
                </div>

                {/* Giờ bắt đầu */}
                <div>
                  <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
                    Giờ bắt đầu <span className="text-rose-500">*</span>
                  </label>
                  <input
                    type="time"
                    value={startTime}
                    onChange={(e) => setStartTime(e.target.value)}
                    className={cn(
                      "w-full rounded-lg border px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:ring-2 focus:ring-rose-100",
                      errors.startTime ? "border-rose-400" : "border-gray-200 focus:border-rose-400"
                    )}
                  />
                  {errors.startTime && <p className="mt-1 text-[11px] text-rose-500">{errors.startTime}</p>}
                  <p className="mt-1 text-[11px] text-gray-400">Định dạng HH:MM, 24 giờ.</p>
                </div>

                {/* Giờ kết thúc */}
                <div>
                  <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
                    Giờ kết thúc <span className="text-rose-500">*</span>
                  </label>
                  <input
                    type="time"
                    value={endTime}
                    onChange={(e) => setEndTime(e.target.value)}
                    className={cn(
                      "w-full rounded-lg border px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:ring-2 focus:ring-rose-100",
                      errors.endTime ? "border-rose-400" : "border-gray-200 focus:border-rose-400"
                    )}
                  />
                  {errors.endTime && <p className="mt-1 text-[11px] text-rose-500">{errors.endTime}</p>}
                </div>
              </div>

              {/* Mô tả */}
              <div className="mt-4">
                <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">Mô tả</label>
                <textarea
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  rows={3}
                  placeholder="Ghi chú về ca này..."
                  className="w-full resize-none rounded-lg border border-gray-200 px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:border-rose-400 focus:ring-2 focus:ring-rose-100"
                />
              </div>

              {submitError && (
                <p className="mt-3 text-[12px] text-rose-600">{submitError}</p>
              )}

              {/* Actions */}
              <div className="mt-5 flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => router.push("/dashboard/shifts")}
                  className="rounded-lg border border-gray-200 px-4 py-2 text-[12px] font-semibold text-gray-600 hover:bg-gray-50 transition-colors"
                >
                  Hủy
                </button>
                <button
                  type="submit"
                  disabled={submitting}
                  className={cn(
                    "flex items-center gap-1.5 rounded-lg px-4 py-2 text-[12px] font-semibold text-white transition-colors",
                    submitting ? "bg-rose-300 cursor-not-allowed" : "bg-rose-500 hover:bg-rose-600"
                  )}
                >
                  <Save className="h-3.5 w-3.5" />
                  {submitting ? "Đang lưu..." : "Lưu ca tiệc"}
                </button>
              </div>
            </div>
          </form>
        </div>

        {/* Sidebar: ca hiện có */}
        <div className="w-72 flex-shrink-0">
          <div className="rounded-xl border border-gray-100 bg-white p-5">
            <div className="mb-4 flex items-center gap-2">
              <Clock className="h-4 w-4 text-gray-400" />
              <h3 className="text-[12px] font-bold uppercase tracking-wide text-gray-600">Ca hiện có</h3>
            </div>
            <div className="space-y-2">
              {otherShifts.length === 0 ? (
                <p className="text-[12px] text-gray-400">Chưa có ca nào.</p>
              ) : (
                otherShifts.map((s) => (
                  <div key={s.id} className="flex items-center justify-between rounded-lg bg-gray-50 px-3 py-2">
                    <div className="flex items-center gap-2">
                      <span className="text-[12px] font-medium text-gray-700">{s.shiftName}</span>
                      <span className="text-[10px] font-mono text-gray-400">CA{String(s.id).padStart(2, "0")}</span>
                    </div>
                    <span className="text-[11px] text-gray-500">{s.startTime} – {s.endTime}</span>
                  </div>
                ))
              )}
            </div>
            <div className="mt-4 flex items-start gap-2 rounded-lg bg-blue-50 px-3 py-2.5">
              <span className="mt-0.5 text-[11px] text-blue-500">ℹ</span>
              <p className="text-[11px] text-blue-700">Khung giờ các ca không được chồng lấp nhau.</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
