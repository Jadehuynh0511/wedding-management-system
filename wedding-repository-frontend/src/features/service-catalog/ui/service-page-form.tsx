"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { ArrowLeft, Save, Info, AlertTriangle } from "lucide-react";

import { cn } from "@/lib/utils";
import { createService, updateService, updateServicePrice } from "@/features/service-catalog/lib/service-api";
import type { CreateServicePayload, ServiceItemDetail, ServiceItemStatus } from "@/features/service-catalog/model/service";

type ServicePageFormProps = {
  mode: "create" | "edit";
  initial?: ServiceItemDetail;
  totalServices: number;
};

function formatPrice(value: number) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value);
}

function formatDate(instant: string) {
  return new Date(instant).toLocaleDateString("vi-VN", { day: "2-digit", month: "2-digit", year: "numeric" });
}

export function ServicePageForm({ mode, initial, totalServices }: ServicePageFormProps) {
  const router = useRouter();
  const nextCode = `DV${String(totalServices + 1).padStart(2, "0")}`;

  const [serviceName, setServiceName] = useState(initial?.serviceName ?? "");
  // Lưu ý: 2 giá trị này hiện chỉ đọc (form chưa có input chỉnh sửa) — bỏ setter để tránh biến thừa.
  const [serviceCategory] = useState(initial?.serviceCategory ?? "");
  const [unitName] = useState(initial?.unitName ?? "");
  const [currentPrice, setCurrentPrice] = useState("");
  const [newPrice, setNewPrice] = useState("");
  const [description, setDescription] = useState(initial?.description ?? "");
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  function validate(): boolean {
    const next: Record<string, string> = {};
    if (!serviceName.trim()) next.serviceName = "Tên dịch vụ không được để trống.";
    if (mode === "create") {
      const price = parseFloat(currentPrice.replace(/[^\d.]/g, ""));
      if (isNaN(price) || price <= 0) next.currentPrice = "Đơn giá khởi điểm phải lớn hơn 0.";
    } else if (newPrice.trim()) {
      const price = parseFloat(newPrice.replace(/[^\d.]/g, ""));
      if (isNaN(price) || price <= 0) next.newPrice = "Đơn giá mới phải lớn hơn 0.";
    }
    setErrors(next);
    return Object.keys(next).length === 0;
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!validate()) return;

    setSubmitError(null);
    setSubmitting(true);

    try {
      if (mode === "create") {
        const price = parseFloat(currentPrice.replace(/[^\d.]/g, ""));
        const payload: CreateServicePayload = {
          serviceName: serviceName.trim(),
          serviceCategory: serviceCategory.trim() || "Khác",
          unitName: unitName.trim() || "lần",
          currentPrice: price,
          status: "HOAT_DONG",
          description: description.trim() || undefined
        };
        await createService(payload);
      } else if (initial) {
        // 1. Cập nhật metadata (không có giá)
        await updateService(initial.id, {
          serviceName: serviceName.trim(),
          serviceCategory: serviceCategory.trim() || initial.serviceCategory,
          unitName: unitName.trim() || initial.unitName,
          status: initial.status as ServiceItemStatus,
          description: description.trim() || undefined
        });

        // 2. Cập nhật giá nếu có giá mới
        const trimmedNewPrice = newPrice.trim();
        if (trimmedNewPrice) {
          const price = parseFloat(trimmedNewPrice.replace(/[^\d.]/g, ""));
          if (!isNaN(price) && price > 0) {
            await updateServicePrice(initial.id, { newPrice: price });
          }
        }
      }
      router.push("/dashboard/services");
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
            {mode === "create" ? "THÊM DỊCH VỤ" : "SỬA DỊCH VỤ"}
          </h1>
          <p className="mt-1 text-[13px] text-gray-500">
            {mode === "create"
              ? "Tạo mới một dịch vụ đi kèm tiệc cưới"
              : `Chỉnh sửa thông tin dịch vụ DV${String(initial?.id).padStart(2, "0")} — ${initial?.serviceName}`}
          </p>
        </div>
        <button
          onClick={() => router.push("/dashboard/services")}
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
            <h2 className="text-[13px] font-bold uppercase tracking-wide text-gray-700">Thông tin dịch vụ</h2>
          </div>

          <div className="grid grid-cols-2 gap-4">
            {/* Mã dịch vụ */}
            <div>
              <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">Mã dịch vụ</label>
              <input
                value={mode === "edit" ? `DV${String(initial?.id).padStart(2, "0")}` : nextCode}
                disabled
                className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2.5 text-[13px] text-gray-400 outline-none"
              />
              {mode === "create" && (
                <p className="mt-1 text-[11px] text-gray-400">Tự động sinh — ví dụ: {nextCode}</p>
              )}
            </div>

            {/* Tên dịch vụ */}
            <div>
              <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
                Tên dịch vụ <span className="text-rose-500">*</span>
              </label>
              <input
                value={serviceName}
                onChange={(e) => setServiceName(e.target.value)}
                placeholder="Ví dụ: MC, Ban nhạc, Máy chiếu..."
                className={cn(
                  "w-full rounded-lg border px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:ring-2 focus:ring-rose-100",
                  errors.serviceName ? "border-rose-400" : "border-gray-200 focus:border-rose-400"
                )}
              />
              {errors.serviceName && <p className="mt-1 text-[11px] text-rose-500">{errors.serviceName}</p>}
            </div>

            {/* Giá — create: khởi điểm / edit: hiện tại + mới */}
            {mode === "create" ? (
              <div className="col-span-2 grid grid-cols-2 gap-4">
                <div>
                  <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
                    Đơn giá khởi điểm <span className="text-rose-500">*</span>
                  </label>
                  <input
                    value={currentPrice}
                    onChange={(e) => setCurrentPrice(e.target.value)}
                    placeholder="Ví dụ: 3.000.000"
                    className={cn(
                      "w-full rounded-lg border px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:ring-2 focus:ring-rose-100",
                      errors.currentPrice ? "border-rose-400" : "border-gray-200 focus:border-rose-400"
                    )}
                  />
                  {errors.currentPrice
                    ? <p className="mt-1 text-[11px] text-rose-500">{errors.currentPrice}</p>
                    : <p className="mt-1 text-[11px] text-gray-400">Đơn vị: VND. Đây là mức giá đầu tiên.</p>
                  }
                </div>
              </div>
            ) : (
              <>
                <div>
                  <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">Đơn giá hiện tại</label>
                  <input
                    value={initial ? formatPrice(initial.currentPrice) : ""}
                    disabled
                    className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2.5 text-[13px] text-gray-500 outline-none"
                  />
                  <p className="mt-1 text-[11px] text-gray-400">
                    Không thể sửa trực tiếp — nhập Đơn giá mới để cập nhật.
                    {initial && <span className="ml-1">Hiệu lực từ {formatDate(initial.priceEffectiveFrom)}.</span>}
                  </p>
                </div>
                <div>
                  <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">Đơn giá mới (tùy chọn)</label>
                  <input
                    value={newPrice}
                    onChange={(e) => setNewPrice(e.target.value)}
                    placeholder="Nhập giá mới nếu muốn cập nhật..."
                    className={cn(
                      "w-full rounded-lg border px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:ring-2 focus:ring-rose-100",
                      errors.newPrice ? "border-rose-400" : "border-gray-200 focus:border-rose-400"
                    )}
                  />
                  {errors.newPrice
                    ? <p className="mt-1 text-[11px] text-rose-500">{errors.newPrice}</p>
                    : <p className="mt-1 text-[11px] text-gray-400">Để trống nếu không thay đổi giá.</p>
                  }
                </div>
              </>
            )}
          </div>

          {/* Ghi chú */}
          <div className="mt-4">
            <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">Ghi chú / Mô tả</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={3}
              placeholder="Phạm vi cung cấp, đặc điểm dịch vụ..."
              className="w-full resize-none rounded-lg border border-gray-200 px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:border-rose-400 focus:ring-2 focus:ring-rose-100"
            />
          </div>

          {/* Info / Warning box */}
          {mode === "create" ? (
            <div className="mt-4 rounded-lg border border-blue-100 bg-blue-50 px-4 py-3">
              <div className="mb-2 flex items-center gap-1.5">
                <Info className="h-3.5 w-3.5 text-blue-500" />
                <span className="text-[12px] font-semibold text-blue-700">Lưu ý</span>
              </div>
              <ul className="space-y-1">
                <li className="flex items-start gap-1.5 text-[12px] text-blue-700">
                  <span className="mt-1 h-1 w-1 flex-shrink-0 rounded-full bg-blue-400" />
                  Sau khi tạo, mọi thay đổi giá sẽ được ghi vào lịch sử giá.
                </li>
                <li className="flex items-start gap-1.5 text-[12px] text-blue-700">
                  <span className="mt-1 h-1 w-1 flex-shrink-0 rounded-full bg-blue-400" />
                  Đơn giá mới chỉ áp dụng cho tiệc đặt <span className="font-semibold mx-0.5">sau</span> thời điểm cập nhật.
                </li>
              </ul>
            </div>
          ) : (
            <div className="mt-4 flex items-start gap-2 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3">
              <AlertTriangle className="mt-0.5 h-3.5 w-3.5 flex-shrink-0 text-amber-500" />
              <p className="text-[12px] text-amber-700">
                Đơn giá không hồi tố: Giá mới chỉ áp dụng cho các tiệc đặt <span className="font-semibold">sau</span> thời điểm cập nhật. Các tiệc đã ký kết trước đó <span className="font-semibold">giữ nguyên giá cũ</span>.
              </p>
            </div>
          )}

          {submitError && <p className="mt-3 text-[12px] text-rose-600">{submitError}</p>}

          {/* Actions */}
          <div className="mt-5 flex justify-end gap-2">
            <button
              type="button"
              onClick={() => router.push("/dashboard/services")}
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
              {submitting ? "Đang lưu..." : mode === "create" ? "Lưu dịch vụ" : "Lưu thay đổi"}
            </button>
          </div>
        </div>
      </form>

      {/* Lịch sử giá (edit only) */}
      {mode === "edit" && initial && initial.priceHistory.length > 0 && (
        <div className="mt-6 rounded-xl border border-gray-100 bg-white p-6">
          <div className="mb-4 flex items-center gap-2">
            <span className="flex h-5 w-5 items-center justify-center rounded-full bg-gray-400 text-[10px] font-bold text-white">2</span>
            <h2 className="text-[13px] font-bold uppercase tracking-wide text-gray-700">Lịch sử giá</h2>
          </div>
          <table className="w-full text-[13px]">
            <thead>
              <tr className="border-b border-gray-100 bg-gray-50">
                <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">Giá cũ</th>
                <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">Hiệu lực từ</th>
                <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">Hiệu lực đến</th>
              </tr>
            </thead>
            <tbody>
              {initial.priceHistory.map((h) => (
                <tr key={h.id} className="border-b border-gray-50">
                  <td className="px-4 py-3 font-medium text-gray-800">{formatPrice(h.oldPrice)}</td>
                  <td className="px-4 py-3 text-gray-600">{formatDate(h.effectiveFrom)}</td>
                  <td className="px-4 py-3 text-gray-600">{h.effectiveTo ? formatDate(h.effectiveTo) : "—"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}