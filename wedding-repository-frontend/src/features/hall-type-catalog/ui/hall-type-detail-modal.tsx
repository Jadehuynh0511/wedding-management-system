"use client";

import { X } from "lucide-react";

import type { HallType } from "@/features/hall-type-catalog/model/hall-type";

type HallTypeDetailModalProps = {
  hallType: HallType;
  onClose: () => void;
};

function formatPrice(value: number) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value);
}

export function HallTypeDetailModal({ hallType, onClose }: HallTypeDetailModalProps) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30">
      <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
        <div className="mb-5 flex items-center justify-between">
          <h2 className="text-[15px] font-bold text-gray-800">Chi tiết loại sảnh</h2>
          <button onClick={onClose} className="rounded-lg p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors">
            <X className="h-4 w-4" />
          </button>
        </div>

        <div className="space-y-3">
          <Row label="Mã loại" value={`LS${String(hallType.id).padStart(2, "0")}`} />
          <Row label="Tên loại sảnh" value={hallType.hallTypeName} />
          <Row label="Đơn giá bàn tối thiểu" value={formatPrice(hallType.minimumTablePrice)} />
          <Row label="Mô tả" value={hallType.description || "—"} />
        </div>

        <div className="mt-5 flex justify-end">
          <button
            onClick={onClose}
            className="rounded-lg border border-gray-200 px-4 py-2 text-[12px] font-semibold text-gray-600 hover:bg-gray-50 transition-colors"
          >
            Đóng
          </button>
        </div>
      </div>
    </div>
  );
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="flex items-start gap-3 rounded-lg bg-gray-50 px-4 py-3">
      <span className="w-40 flex-shrink-0 text-[12px] font-semibold text-gray-500">{label}</span>
      <span className="text-[13px] text-gray-800">{value}</span>
    </div>
  );
}
