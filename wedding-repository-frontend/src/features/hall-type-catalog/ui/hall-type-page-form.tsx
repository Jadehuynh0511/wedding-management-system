"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { AlertTriangle, ArrowLeft, Info, Save } from "lucide-react";

import { cn } from "@/lib/utils";
import { createHallType, updateHallType } from "@/features/hall-type-catalog/lib/hall-type-api";
import type {
  CreateHallTypePayload,
  Hall,
  HallType,
} from "@/features/hall-type-catalog/model/hall-type";
import { HALL_STATUS_CONFIG } from "@/features/hall-catalog/model/hall";

type HallTypePageFormProps = {
  mode: "create" | "edit";
  initial?: HallType;
  hallsUsingType?: Hall[];
  totalHallTypes: number;
};

export function HallTypePageForm({
  mode,
  initial,
  hallsUsingType = [],
  totalHallTypes,
}: HallTypePageFormProps) {
  const router = useRouter();
  const nextCode = `LS${String(totalHallTypes + 1).padStart(2, "0")}`;

  const [hallTypeName, setHallTypeName] = useState(initial?.hallTypeName ?? "");
  const [minimumTablePrice, setMinimumTablePrice] = useState(
    initial?.minimumTablePrice ? String(initial.minimumTablePrice) : "",
  );
  const [description, setDescription] = useState(initial?.description ?? "");
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const hasHallsInUse = hallsUsingType.length > 0;

  function validate(): boolean {
    const next: Record<string, string> = {};
    if (!hallTypeName.trim()) next.hallTypeName = "Tên loại sảnh không được để trống.";
    const price = parseFloat(minimumTablePrice.replace(/[^\d.]/g, ""));
    if (isNaN(price) || price <= 0)
      next.minimumTablePrice = "Đơn giá bàn tối thiểu phải lớn hơn 0.";
    setErrors(next);
    return Object.keys(next).length === 0;
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!validate()) return;

    const price = parseFloat(minimumTablePrice.replace(/[^\d.]/g, ""));
    const payload: CreateHallTypePayload = {
      hallTypeName: hallTypeName.trim(),
      minimumTablePrice: price,
      description: description.trim() || undefined,
    };

    setSubmitError(null);
    setSubmitting(true);
    try {
      if (mode === "create") {
        await createHallType(payload);
      } else if (initial) {
        await updateHallType(initial.id, payload);
      }
      router.push("/dashboard/hall-types");
      router.refresh();
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : "Đã có lỗi xảy ra.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <div className="mb-6 flex items-start justify-between">
        <div>
          <h1 className="text-[20px] font-bold tracking-wide text-gray-800">
            {mode === "create" ? "THÊM LOẠI SẢNH" : "SỬA LOẠI SẢNH"}
          </h1>
          <p className="mt-1 text-[13px] text-gray-500">
            {mode === "create"
              ? "Thêm mới một loại sảnh tiệc vào hệ thống"
              : `Chỉnh sửa loại sảnh LS${String(initial?.id).padStart(2, "0")} — ${initial?.hallTypeName}`}
          </p>
        </div>
        <button
          onClick={() => router.push("/dashboard/hall-types")}
          className="flex items-center gap-1.5 text-[12px] font-semibold text-gray-500 transition-colors hover:text-gray-700"
        >
          <ArrowLeft className="h-3.5 w-3.5" />
          Quay lại
        </button>
      </div>

      <form onSubmit={handleSubmit}>
        <div className="rounded-xl border border-gray-100 bg-white p-6">
          <div className="mb-5 flex items-center gap-2">
            <span className="flex h-5 w-5 items-center justify-center rounded-full bg-rose-500 text-[10px] font-bold text-white">
              1
            </span>
            <h2 className="text-[13px] font-bold uppercase tracking-wide text-gray-700">
              Thông tin loại sảnh
            </h2>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
                Mã loại <span className="text-rose-500">*</span>
              </label>
              <input
                value={mode === "edit" ? `LS${String(initial?.id).padStart(2, "0")}` : nextCode}
                disabled
                className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2.5 text-[13px] text-gray-400 outline-none"
              />
            </div>

            <div>
              <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
                Tên loại sảnh <span className="text-rose-500">*</span>
              </label>
              <input
                value={hallTypeName}
                onChange={(e) => setHallTypeName(e.target.value)}
                placeholder="VD: Standard, VIP, VVIP..."
                className={cn(
                  "w-full rounded-lg border px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:ring-2 focus:ring-rose-100",
                  errors.hallTypeName ? "border-rose-400" : "border-gray-200 focus:border-rose-400",
                )}
              />
              {errors.hallTypeName ? (
                <p className="mt-1 text-[11px] text-rose-500">{errors.hallTypeName}</p>
              ) : (
                <p className="mt-1 text-[11px] text-gray-400">
                  Tên loại sảnh không được trùng với loại đã có.
                </p>
              )}
            </div>

            <div>
              <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
                Đơn giá bàn tối thiểu <span className="text-rose-500">*</span>
              </label>
              <input
                value={minimumTablePrice}
                onChange={(e) => setMinimumTablePrice(e.target.value)}
                placeholder="VD: 3500000"
                className={cn(
                  "w-full rounded-lg border px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:ring-2 focus:ring-rose-100",
                  errors.minimumTablePrice
                    ? "border-rose-400"
                    : "border-gray-200 focus:border-rose-400",
                )}
              />
              {errors.minimumTablePrice ? (
                <p className="mt-1 text-[11px] text-rose-500">{errors.minimumTablePrice}</p>
              ) : (
                <p className="mt-1 text-[11px] text-gray-400">
                  Đơn vị: VND. Áp dụng cho tất cả sảnh thuộc loại này.
                </p>
              )}
            </div>
          </div>

          <div className="mt-4">
            <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">Mô tả</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={3}
              placeholder="Mô tả ngắn về loại sảnh..."
              className="w-full resize-none rounded-lg border border-gray-200 px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:border-rose-400 focus:ring-2 focus:ring-rose-100"
            />
          </div>

          <div className="mt-4 rounded-lg border border-blue-100 bg-blue-50 px-4 py-3">
            <div className="mb-2 flex items-center gap-1.5">
              <Info className="h-3.5 w-3.5 text-blue-500" />
              <span className="text-[12px] font-semibold text-blue-700">Quy tắc hệ thống</span>
            </div>
            <ul className="space-y-1">
              {[
                "Tên loại sảnh không được trùng với loại sảnh đã có.",
                "Không thể xóa loại sảnh nếu đang có sảnh sử dụng.",
                "Thay đổi đơn giá chỉ ảnh hưởng đến tiệc mới, không áp dụng hồi tố.",
              ].map((rule) => (
                <li key={rule} className="flex items-start gap-1.5 text-[12px] text-blue-700">
                  <span className="mt-1 h-1 w-1 flex-shrink-0 rounded-full bg-blue-400" />
                  {rule}
                </li>
              ))}
            </ul>
          </div>

          {mode === "edit" && hasHallsInUse && (
            <div className="mt-3 flex items-start gap-2 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3">
              <AlertTriangle className="mt-0.5 h-3.5 w-3.5 flex-shrink-0 text-amber-500" />
              <p className="text-[12px] text-amber-700">
                Không thể xóa loại sảnh này vì hiện có{" "}
                <span className="font-bold">{hallsUsingType.length} sảnh</span> đang sử dụng loại{" "}
                <span className="font-bold">{initial?.hallTypeName}</span>. Hãy đổi loại sảnh của
                các sảnh đó trước khi xóa.
              </p>
            </div>
          )}

          {submitError && <p className="mt-3 text-[12px] text-rose-600">{submitError}</p>}

          <div className="mt-5 flex justify-end gap-2">
            <button
              type="button"
              onClick={() => router.push("/dashboard/hall-types")}
              className="rounded-lg border border-gray-200 px-4 py-2 text-[12px] font-semibold text-gray-600 transition-colors hover:bg-gray-50"
            >
              Hủy
            </button>
            <button
              type="submit"
              disabled={submitting}
              className={cn(
                "flex items-center gap-1.5 rounded-lg px-4 py-2 text-[12px] font-semibold text-white transition-colors",
                submitting ? "cursor-not-allowed bg-rose-300" : "bg-rose-500 hover:bg-rose-600",
              )}
            >
              <Save className="h-3.5 w-3.5" />
              {submitting ? "Đang lưu..." : mode === "create" ? "Thêm mới" : "Cập nhật"}
            </button>
          </div>
        </div>
      </form>

      {mode === "edit" && hallsUsingType.length > 0 && (
        <div className="mt-6 rounded-xl border border-gray-100 bg-white p-6">
          <div className="mb-4 flex items-center justify-between">
            <div className="flex items-center gap-2">
              <span className="flex h-5 w-5 items-center justify-center rounded-full bg-rose-500 text-[10px] font-bold text-white">
                2
              </span>
              <h2 className="text-[13px] font-bold uppercase tracking-wide text-gray-700">
                Sảnh đang sử dụng loại này
              </h2>
            </div>
            <span className="text-[12px] text-gray-500">{hallsUsingType.length} sảnh</span>
          </div>
          <table className="w-full text-[13px]">
            <thead>
              <tr className="border-b border-gray-100 bg-gray-50">
                <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                  Mã sảnh
                </th>
                <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                  Tên sảnh
                </th>
                <th className="px-4 py-3 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                  Số bàn tối đa
                </th>
                <th className="px-4 py-3 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                  Trạng thái
                </th>
              </tr>
            </thead>
            <tbody>
              {hallsUsingType.map((hall) => {
                const status = HALL_STATUS_CONFIG[hall.status] ?? {
                  label: hall.status,
                  className: "bg-gray-100 text-gray-600",
                };
                return (
                  <tr key={hall.id} className="border-b border-gray-50">
                    <td className="px-4 py-3 font-mono font-semibold text-gray-700">
                      S{String(hall.id).padStart(3, "0")}
                    </td>
                    <td className="px-4 py-3 font-medium text-gray-800">{hall.hallName}</td>
                    <td className="px-4 py-3 text-right text-gray-700">{hall.maxCapacity} bàn</td>
                    <td className="px-4 py-3 text-right">
                      <span
                        className={cn(
                          "rounded-full px-2.5 py-1 text-[11px] font-semibold",
                          status.className,
                        )}
                      >
                        {status.label}
                      </span>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
