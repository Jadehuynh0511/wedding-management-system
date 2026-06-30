"use client";

import { useEffect, useState } from "react";
import { X } from "lucide-react";

import { cn } from "@/lib/utils";
import type { HallType, CreateHallTypePayload } from "@/features/hall-type-catalog/model/hall-type";

type HallTypeFormProps = {
  initial?: HallType | null;
  onSubmit: (payload: CreateHallTypePayload) => Promise<void>;
  onClose: () => void;
};

export function HallTypeForm({ initial, onSubmit, onClose }: HallTypeFormProps) {
  const [hallTypeName, setHallTypeName] = useState(initial?.hallTypeName ?? "");
  const [minimumTablePrice, setMinimumTablePrice] = useState(
    initial?.minimumTablePrice ? String(initial.minimumTablePrice) : ""
  );
  const [description, setDescription] = useState(initial?.description ?? "");
  const [error, setError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    setHallTypeName(initial?.hallTypeName ?? "");
    setMinimumTablePrice(initial?.minimumTablePrice ? String(initial.minimumTablePrice) : "");
    setDescription(initial?.description ?? "");
    setError(null);
  }, [initial]);

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    const price = parseFloat(minimumTablePrice.replace(/[^\d.]/g, ""));

    if (!hallTypeName.trim()) return setError("Tên loại sảnh không được để trống.");
    if (isNaN(price) || price <= 0) return setError("Đơn giá bàn tối thiểu phải lớn hơn 0.");

    setError(null);
    setSubmitting(true);

    try {
      await onSubmit({ hallTypeName: hallTypeName.trim(), minimumTablePrice: price, description: description.trim() || undefined });
      onClose();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Đã có lỗi xảy ra.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30">
      <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
        {/* Header */}
        <div className="mb-5 flex items-center justify-between">
          <h2 className="text-[15px] font-bold text-gray-800">
            {initial ? "Chỉnh sửa loại sảnh" : "Thêm loại sảnh mới"}
          </h2>
          <button onClick={onClose} className="rounded-lg p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors">
            <X className="h-4 w-4" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          {/* Tên loại */}
          <div>
            <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
              Tên loại sảnh <span className="text-rose-500">*</span>
            </label>
            <input
              value={hallTypeName}
              onChange={(e) => setHallTypeName(e.target.value)}
              placeholder="VD: Standard, VIP, VVIP..."
              className="w-full rounded-lg border border-gray-200 px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:border-rose-400 focus:ring-2 focus:ring-rose-100"
            />
          </div>

          {/* Đơn giá */}
          <div>
            <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
              Đơn giá bàn tối thiểu (₫) <span className="text-rose-500">*</span>
            </label>
            <input
              value={minimumTablePrice}
              onChange={(e) => setMinimumTablePrice(e.target.value)}
              placeholder="VD: 3500000"
              className="w-full rounded-lg border border-gray-200 px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:border-rose-400 focus:ring-2 focus:ring-rose-100"
            />
          </div>

          {/* Mô tả */}
          <div>
            <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">Mô tả</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={3}
              placeholder="Mô tả ngắn về loại sảnh..."
              className="w-full resize-none rounded-lg border border-gray-200 px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:border-rose-400 focus:ring-2 focus:ring-rose-100"
            />
          </div>

          {error && <p className="text-[12px] text-rose-600">{error}</p>}

          {/* Actions */}
          <div className="flex justify-end gap-2 pt-1">
            <button
              type="button"
              onClick={onClose}
              className="rounded-lg border border-gray-200 px-4 py-2 text-[12px] font-semibold text-gray-600 hover:bg-gray-50 transition-colors"
            >
              Hủy
            </button>
            <button
              type="submit"
              disabled={submitting}
              className={cn(
                "rounded-lg px-4 py-2 text-[12px] font-semibold text-white transition-colors",
                submitting ? "bg-rose-300 cursor-not-allowed" : "bg-rose-500 hover:bg-rose-600"
              )}
            >
              {submitting ? "Đang lưu..." : initial ? "Lưu thay đổi" : "Thêm mới"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
