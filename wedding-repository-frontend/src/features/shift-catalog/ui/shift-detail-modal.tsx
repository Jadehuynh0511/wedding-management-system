"use client";

import { X } from "lucide-react";
import type { Shift } from "@/features/shift-catalog/model/shift";

type ShiftDetailModalProps = {
  shift: Shift;
  onClose: () => void;
};

export function ShiftDetailModal({ shift, onClose }: ShiftDetailModalProps) {
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30">
      <div className="w-full max-w-md rounded-2xl bg-white p-6 shadow-xl">
        <div className="mb-5 flex items-center justify-between">
          <h2 className="text-[15px] font-bold text-gray-800">Chi tiết ca tiệc</h2>
          <button onClick={onClose} className="rounded-lg p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors">
            <X className="h-4 w-4" />
          </button>
        </div>
        <div className="space-y-3">
          <Row label="Mã ca" value={`CA${String(shift.id).padStart(2, "0")}`} />
          <Row label="Tên ca" value={shift.shiftName} />
          <Row label="Giờ bắt đầu" value={shift.startTime} />
          <Row label="Giờ kết thúc" value={shift.endTime} />
          <Row label="Mô tả" value={shift.description || "—"} />
        </div>
        <div className="mt-5 flex justify-end">
          <button onClick={onClose} className="rounded-lg border border-gray-200 px-4 py-2 text-[12px] font-semibold text-gray-600 hover:bg-gray-50 transition-colors">
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
      <span className="w-36 flex-shrink-0 text-[12px] font-semibold text-gray-500">{label}</span>
      <span className="text-[13px] text-gray-800">{value}</span>
    </div>
  );
}
