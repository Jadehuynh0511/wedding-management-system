"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { ArrowLeft, Save, Info, AlertTriangle } from "lucide-react";

import { cn } from "@/lib/utils";
import { createHall, updateHall } from "@/features/hall-catalog/lib/hall-api";
import {
  HALL_STATUS_OPTIONS,
  type CreateHallPayload,
  type Hall,
  type HallStatus
} from "@/features/hall-catalog/model/hall";
import type { HallType } from "@/features/hall-type-catalog/model/hall-type";

type HallPageFormProps = {
  mode: "create" | "edit";
  initial?: Hall;
  hallTypes: HallType[];
  totalHalls: number;
};

function formatPrice(value: number) {
  return new Intl.NumberFormat("vi-VN").format(value);
}

export function HallPageForm({ mode, initial, hallTypes, totalHalls }: HallPageFormProps) {
  const router = useRouter();
  const nextCode = `S${String(totalHalls + 1).padStart(3, "0")}`;

  const [hallName, setHallName] = useState(initial?.hallName ?? "");
  const [hallTypeId, setHallTypeId] = useState<string>(initial ? String(initial.hallTypeId) : "");
  const [status, setStatus] = useState<HallStatus>(initial?.status ?? "TRONG");
  const [maxCapacity, setMaxCapacity] = useState(initial?.maxCapacity ? String(initial.maxCapacity) : "");
  const [tablePrice, setTablePrice] = useState(initial?.tablePrice ? String(initial.tablePrice) : "");
  const [description, setDescription] = useState(initial?.description ?? "");
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const selectedHallType = hallTypes.find((ht) => String(ht.id) === hallTypeId);

  function validate(): boolean {
    const next: Record<string, string> = {};
    if (!hallName.trim()) next.hallName = "Tên sảnh không được để trống.";
    if (!hallTypeId) next.hallTypeId = "Vui lòng chọn loại sảnh.";
    const capacity = parseInt(maxCapacity);
    if (isNaN(capacity) || capacity <= 0) next.maxCapacity = "Số bàn tối đa phải lớn hơn 0.";
    const price = parseFloat(tablePrice.replace(/[^\d.]/g, ""));
    if (isNaN(price) || price <= 0) next.tablePrice = "Đơn giá bàn tối thiểu phải lớn hơn 0.";
    if (selectedHallType && !isNaN(price) && price < selectedHallType.minimumTablePrice) {
      next.tablePrice = `Đơn giá phải ≥ ${formatPrice(selectedHallType.minimumTablePrice)} đ (tối thiểu của loại ${selectedHallType.hallTypeName}).`;
    }
    setErrors(next);
    return Object.keys(next).length === 0;
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!validate()) return;

    const payload: CreateHallPayload = {
      hallTypeId: parseInt(hallTypeId),
      hallName: hallName.trim(),
      maxCapacity: parseInt(maxCapacity),
      tablePrice: parseFloat(tablePrice.replace(/[^\d.]/g, "")),
      status,
      description: description.trim() || undefined
    };

    setSubmitError(null);
    setSubmitting(true);
    try {
      if (mode === "create") {
        await createHall(payload);
      } else if (initial) {
        await updateHall(initial.id, payload);
      }
      router.push("/dashboard/halls");
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
            {mode === "create" ? "THÊM SẢNH MỚI" : "SỬA THÔNG TIN SẢNH"}
          </h1>
          <p className="mt-1 text-[13px] text-gray-500">
            {mode === "create"
              ? "Tạo một sảnh tiệc mới vào hệ thống"
              : `Chỉnh sửa sảnh S${String(initial?.id).padStart(3, "0")} — ${initial?.hallName}`}
          </p>
        </div>
        <button
          onClick={() => router.push("/dashboard/halls")}
          className="flex items-center gap-1.5 text-[12px] font-semibold text-gray-500 hover:text-gray-700 transition-colors"
        >
          <ArrowLeft className="h-3.5 w-3.5" />
          Quay lại
        </button>
      </div>

      <form onSubmit={handleSubmit}>
        <div className="rounded-xl border border-gray-100 bg-white p-6">
          <div className="mb-5 flex items-center gap-2">
            <span className="flex h-5 w-5 items-center justify-center rounded-full bg-rose-500 text-[10px] font-bold text-white">1</span>
            <h2 className="text-[13px] font-bold uppercase tracking-wide text-gray-700">Thông tin sảnh</h2>
          </div>

          <div className="grid grid-cols-2 gap-4">
            {/* Mã sảnh */}
            <div>
              <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
                Mã sảnh <span className="text-rose-500">*</span>
              </label>
              <input
                value={mode === "edit" ? `S${String(initial?.id).padStart(3, "0")}` : nextCode}
                disabled
                className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2.5 text-[13px] text-gray-400 outline-none"
              />
              <p className="mt-1 text-[11px] text-gray-400">Tự động sinh — ví dụ: {nextCode}</p>
            </div>

            {/* Tên sảnh */}
            <div>
              <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
                Tên sảnh <span className="text-rose-500">*</span>
              </label>
              <input
                value={hallName}
                onChange={(e) => setHallName(e.target.value)}
                placeholder="Ví dụ: Sảnh Crystal, Sảnh Diamond..."
                className={cn(
                  "w-full rounded-lg border px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:ring-2 focus:ring-rose-100",
                  errors.hallName ? "border-rose-400" : "border-gray-200 focus:border-rose-400"
                )}
              />
              {errors.hallName && <p className="mt-1 text-[11px] text-rose-500">{errors.hallName}</p>}
            </div>

            {/* Loại sảnh */}
            <div>
              <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
                Loại sảnh <span className="text-rose-500">*</span>
              </label>
              <select
                value={hallTypeId}
                onChange={(e) => setHallTypeId(e.target.value)}
                className={cn(
                  "w-full rounded-lg border px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:ring-2 focus:ring-rose-100",
                  errors.hallTypeId ? "border-rose-400" : "border-gray-200 focus:border-rose-400"
                )}
              >
                <option value="">Chọn loại sảnh</option>
                {hallTypes.map((ht) => (
                  <option key={ht.id} value={String(ht.id)}>
                    {ht.hallTypeName} — tối thiểu {formatPrice(ht.minimumTablePrice)} đ/bàn
                  </option>
                ))}
              </select>
              {errors.hallTypeId && <p className="mt-1 text-[11px] text-rose-500">{errors.hallTypeId}</p>}
            </div>

            {/* Trạng thái */}
            <div>
              <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
                Trạng thái <span className="text-rose-500">*</span>
              </label>
              <select
                value={status}
                onChange={(e) => setStatus(e.target.value as HallStatus)}
                className="w-full rounded-lg border border-gray-200 px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:border-rose-400 focus:ring-2 focus:ring-rose-100"
              >
                {HALL_STATUS_OPTIONS.map((opt) => (
                  <option key={opt.value} value={opt.value}>{opt.label}</option>
                ))}
              </select>
            </div>

            {/* Số bàn tối đa */}
            <div>
              <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
                Số bàn tối đa <span className="text-rose-500">*</span>
              </label>
              <input
                value={maxCapacity}
                onChange={(e) => setMaxCapacity(e.target.value)}
                placeholder="Ví dụ: 50"
                className={cn(
                  "w-full rounded-lg border px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:ring-2 focus:ring-rose-100",
                  errors.maxCapacity ? "border-rose-400" : "border-gray-200 focus:border-rose-400"
                )}
              />
              {errors.maxCapacity
                ? <p className="mt-1 text-[11px] text-rose-500">{errors.maxCapacity}</p>
                : <p className="mt-1 text-[11px] text-gray-400">Số bàn tối đa phải lớn hơn số bàn tối thiểu.</p>
              }
            </div>

            {/* Đơn giá bàn tối thiểu */}
            <div>
              <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
                Đơn giá bàn tối thiểu <span className="text-rose-500">*</span>
              </label>
              <input
                value={tablePrice}
                onChange={(e) => setTablePrice(e.target.value)}
                placeholder="Ví dụ: 5.000.000"
                className={cn(
                  "w-full rounded-lg border px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:ring-2 focus:ring-rose-100",
                  errors.tablePrice ? "border-rose-400" : "border-gray-200 focus:border-rose-400"
                )}
              />
              {errors.tablePrice
                ? <p className="mt-1 text-[11px] text-rose-500">{errors.tablePrice}</p>
                : <p className="mt-1 text-[11px] text-gray-400">
                    Đơn vị: VND. Phải lớn hơn 0.
                    {selectedHallType && ` Tối thiểu: ${formatPrice(selectedHallType.minimumTablePrice)} đ.`}
                  </p>
              }
            </div>
          </div>

          {/* Ghi chú */}
          <div className="mt-4">
            <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">Ghi chú / Mô tả</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={3}
              placeholder="Mô tả nội thất, tiện nghi, ghi chú nội bộ..."
              className="w-full resize-none rounded-lg border border-gray-200 px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:border-rose-400 focus:ring-2 focus:ring-rose-100"
            />
          </div>

          {/* Rules box */}
          <div className="mt-4 rounded-lg border border-blue-100 bg-blue-50 px-4 py-3">
            <div className="mb-2 flex items-center gap-1.5">
              <Info className="h-3.5 w-3.5 text-blue-500" />
              <span className="text-[12px] font-semibold text-blue-700">Lưu ý khi thêm sảnh</span>
            </div>
            <ul className="space-y-1">
              {[
                "Tên sảnh không được trùng với sảnh đã có trong hệ thống.",
                "Số bàn tối thiểu phải nhỏ hơn số bàn tối đa.",
                "Đơn giá bàn tối thiểu phải lớn hơn 0."
              ].map((rule) => (
                <li key={rule} className="flex items-start gap-1.5 text-[12px] text-blue-700">
                  <span className="mt-1 h-1 w-1 flex-shrink-0 rounded-full bg-blue-400" />
                  {rule}
                </li>
              ))}
            </ul>
          </div>

          {/* Warning khi sửa */}
          {mode === "edit" && (
            <div className="mt-3 flex items-start gap-2 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3">
              <AlertTriangle className="mt-0.5 h-3.5 w-3.5 flex-shrink-0 text-amber-500" />
              <p className="text-[12px] text-amber-700">
                Lưu ý: Sảnh <span className="font-bold">{initial?.hallName}</span> có thể đang có tiệc sắp diễn ra. Thay đổi số bàn hoặc đơn giá có thể ảnh hưởng đến các tiệc đó. Vui lòng kiểm tra lại trước khi lưu.
              </p>
            </div>
          )}

          {submitError && <p className="mt-3 text-[12px] text-rose-600">{submitError}</p>}

          {/* Actions */}
          <div className="mt-5 flex justify-end gap-2">
            <button
              type="button"
              onClick={() => router.push("/dashboard/halls")}
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
              {submitting ? "Đang lưu..." : mode === "create" ? "Lưu sảnh" : "Lưu thay đổi"}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
}