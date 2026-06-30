"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { ArrowLeft, Save } from "lucide-react";

import { cn } from "@/lib/utils";
import { createMenuItem, updateMenuItem } from "@/features/menu-item-catalog/lib/menu-item-api";
import type { CreateMenuItemPayload, MenuItem, MenuItemStatus } from "@/features/menu-item-catalog/model/menu-item";

type MenuItemPageFormProps = {
  mode: "create" | "edit";
  initial?: MenuItem;
  totalItems: number;
};

export function MenuItemPageForm({ mode, initial, totalItems }: MenuItemPageFormProps) {
  const router = useRouter();
  const nextCode = `MA${String(totalItems + 1).padStart(2, "0")}`;

  const [itemName, setItemName] = useState(initial?.itemName ?? "");
  const [itemCategory, setItemCategory] = useState(initial?.itemCategory ?? "");
  const [currentPrice, setCurrentPrice] = useState(
    initial?.currentPrice ? String(initial.currentPrice) : ""
  );
  const [status, setStatus] = useState<MenuItemStatus>(initial?.status ?? "CON");
  const [description, setDescription] = useState(initial?.description ?? "");
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  function validate(): boolean {
    const next: Record<string, string> = {};
    if (!itemName.trim()) next.itemName = "Tên món ăn không được để trống.";
    if (!itemCategory.trim()) next.itemCategory = "Phân loại không được để trống.";
    const price = parseFloat(currentPrice.replace(/[^\d.]/g, ""));
    if (isNaN(price) || price <= 0) next.currentPrice = "Đơn giá phải lớn hơn 0.";
    setErrors(next);
    return Object.keys(next).length === 0;
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!validate()) return;

    const price = parseFloat(currentPrice.replace(/[^\d.]/g, ""));
    const payload: CreateMenuItemPayload = {
      itemName: itemName.trim(),
      itemCategory: itemCategory.trim(),
      currentPrice: price,
      status,
      description: description.trim() || undefined
    };

    setSubmitError(null);
    setSubmitting(true);
    try {
      if (mode === "create") {
        await createMenuItem(payload);
      } else if (initial) {
        await updateMenuItem(initial.id, payload);
      }
      router.push("/dashboard/menu-items");
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
            {mode === "create" ? "THÊM MÓN ĂN" : "CHỈNH SỬA MÓN ĂN"}
          </h1>
          <p className="mt-1 text-[13px] text-gray-500">
            {mode === "create"
              ? "Bổ sung món ăn mới vào thực đơn"
              : `Chỉnh sửa món ăn MA${String(initial?.id).padStart(2, "0")} — ${initial?.itemName}`}
          </p>
        </div>
        <button
          onClick={() => router.push("/dashboard/menu-items")}
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
            <h2 className="text-[13px] font-bold uppercase tracking-wide text-gray-700">Thông tin món ăn</h2>
          </div>

          <div className="grid grid-cols-2 gap-4">
            {/* Mã món */}
            <div>
              <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">Mã món</label>
              <input
                value={mode === "edit" ? `MA${String(initial?.id).padStart(2, "0")}` : nextCode}
                disabled
                className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2.5 text-[13px] text-gray-400 outline-none"
              />
              <p className="mt-1 text-[11px] text-gray-400">Tự động sinh — ví dụ: {nextCode}</p>
            </div>

            {/* Tên món ăn */}
            <div>
              <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
                Tên món ăn <span className="text-rose-500">*</span>
              </label>
              <input
                value={itemName}
                onChange={(e) => setItemName(e.target.value)}
                placeholder="Ví dụ: Súp cua, Bò lúc lắc, Cá hấp..."
                className={cn(
                  "w-full rounded-lg border px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:ring-2 focus:ring-rose-100",
                  errors.itemName ? "border-rose-400" : "border-gray-200 focus:border-rose-400"
                )}
              />
              {errors.itemName && <p className="mt-1 text-[11px] text-rose-500">{errors.itemName}</p>}
            </div>

            {/* Đơn giá */}
            <div>
              <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
                Đơn giá <span className="text-rose-500">*</span>
              </label>
              <input
                value={currentPrice}
                onChange={(e) => setCurrentPrice(e.target.value)}
                placeholder="Ví dụ: 250.000"
                className={cn(
                  "w-full rounded-lg border px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:ring-2 focus:ring-rose-100",
                  errors.currentPrice ? "border-rose-400" : "border-gray-200 focus:border-rose-400"
                )}
              />
              {errors.currentPrice
                ? <p className="mt-1 text-[11px] text-rose-500">{errors.currentPrice}</p>
                : <p className="mt-1 text-[11px] text-gray-400">Đơn vị: VND / bàn.</p>
              }
            </div>

            {/* Trạng thái */}
            <div>
              <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
                Trạng thái <span className="text-rose-500">*</span>
              </label>
              <select
                value={status}
                onChange={(e) => setStatus(e.target.value as MenuItemStatus)}
                className="w-full rounded-lg border border-gray-200 px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:border-rose-400 focus:ring-2 focus:ring-rose-100"
              >
                <option value="CON">Còn</option>
                <option value="HET">Hết</option>
              </select>
            </div>

            {/* Phân loại */}
            <div className="col-span-2">
              <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
                Phân loại <span className="text-rose-500">*</span>
              </label>
              <input
                value={itemCategory}
                onChange={(e) => setItemCategory(e.target.value)}
                placeholder="Ví dụ: Khai vị, Món chính, Tráng miệng..."
                className={cn(
                  "w-full rounded-lg border px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:ring-2 focus:ring-rose-100",
                  errors.itemCategory ? "border-rose-400" : "border-gray-200 focus:border-rose-400"
                )}
              />
              {errors.itemCategory && <p className="mt-1 text-[11px] text-rose-500">{errors.itemCategory}</p>}
            </div>
          </div>

          {/* Ghi chú / Mô tả */}
          <div className="mt-4">
            <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">Ghi chú / Mô tả</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={3}
              placeholder="Mô tả thành phần, cách chế biến, cách phục vụ..."
              className="w-full resize-none rounded-lg border border-gray-200 px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:border-rose-400 focus:ring-2 focus:ring-rose-100"
            />
          </div>

          {submitError && <p className="mt-3 text-[12px] text-rose-600">{submitError}</p>}

          {/* Actions */}
          <div className="mt-5 flex justify-end gap-2">
            <button
              type="button"
              onClick={() => router.push("/dashboard/menu-items")}
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
              {submitting ? "Đang lưu..." : "Lưu món ăn"}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
}
