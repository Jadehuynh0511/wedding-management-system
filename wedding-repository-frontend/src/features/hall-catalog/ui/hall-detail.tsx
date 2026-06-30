"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { ArrowLeft, Pencil, Trash2, Calendar, TrendingUp, Users } from "lucide-react";

import { cn } from "@/lib/utils";
import { deleteHall } from "@/features/hall-catalog/lib/hall-api";
import { HALL_STATUS_CONFIG, type Hall } from "@/features/hall-catalog/model/hall";

type HallDetailProps = {
  hall: Hall;
};

function formatPrice(value: number) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value);
}

export function HallDetail({ hall }: HallDetailProps) {
  const router = useRouter();
  const [showDelete, setShowDelete] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [deleting, setDeleting] = useState(false);

  const statusConfig = HALL_STATUS_CONFIG[hall.status];

  async function handleDelete() {
    setDeleteError(null);
    setDeleting(true);
    try {
      await deleteHall(hall.id);
      router.push("/dashboard/halls");
      router.refresh();
    } catch (err) {
      setDeleteError(err instanceof Error ? err.message : "Không thể xóa sảnh.");
      setDeleting(false);
    }
  }

  return (
    <div>
      {/* Page header */}
      <div className="mb-6 flex items-start justify-between">
        <div>
          <h1 className="text-[20px] font-bold tracking-wide text-gray-800">THÔNG TIN SẢNH</h1>
          <p className="mt-1 text-[13px] text-gray-500">
            Chi tiết sảnh S{String(hall.id).padStart(3, "0")} — {hall.hallName}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={() => router.push("/dashboard/halls")}
            className="flex items-center gap-1.5 text-[12px] font-semibold text-gray-500 hover:text-gray-700 transition-colors"
          >
            <ArrowLeft className="h-3.5 w-3.5" />
            Quay lại
          </button>
          <button
            onClick={() => router.push(`/dashboard/halls/${hall.id}/edit`)}
            className="flex items-center gap-1.5 rounded-lg border border-gray-200 px-3 py-2 text-[12px] font-semibold text-gray-600 hover:bg-gray-50 transition-colors"
          >
            <Pencil className="h-3.5 w-3.5" />
            Sửa thông tin
          </button>
          <button
            onClick={() => setShowDelete(true)}
            className="flex items-center gap-1.5 rounded-lg bg-rose-500 px-3 py-2 text-[12px] font-semibold text-white hover:bg-rose-600 transition-colors"
          >
            <Trash2 className="h-3.5 w-3.5" />
            Xóa sảnh
          </button>
        </div>
      </div>

      <div className="flex gap-6">
        {/* Left: thông tin chung */}
        <div className="flex-1">
          <div className="rounded-xl border border-gray-100 bg-white p-6">
            <div className="mb-5 flex items-center gap-2">
              <span className="flex h-5 w-5 items-center justify-center rounded-full bg-rose-500 text-[10px] font-bold text-white">1</span>
              <h2 className="text-[13px] font-bold uppercase tracking-wide text-gray-700">Thông tin chung</h2>
            </div>
            <div className="grid grid-cols-2 gap-x-8 gap-y-4">
              <InfoRow label="Mã sảnh" value={`S${String(hall.id).padStart(3, "0")}`} bold />
              <InfoRow label="Tên sảnh" value={hall.hallName} bold />
              <div>
                <p className="text-[11px] font-semibold text-gray-500 mb-1">Loại sảnh</p>
                <div className="flex items-center gap-2">
                  <span className="text-[13px] font-medium text-gray-800">{hall.hallTypeName}</span>
                  <span className="rounded bg-gray-100 px-1.5 py-0.5 font-mono text-[10px] text-gray-500">
                    LS{String(hall.hallTypeId).padStart(2, "0")}
                  </span>
                </div>
              </div>
              <div>
                <p className="text-[11px] font-semibold text-gray-500 mb-1">Trạng thái</p>
                <span className={cn("rounded-full px-2.5 py-1 text-[11px] font-semibold", statusConfig.className)}>
                  {statusConfig.label}
                </span>
              </div>
              <InfoRow label="Số bàn tối đa" value={`${hall.maxCapacity} bàn`} />
              <InfoRow label="Đơn giá bàn tối thiểu" value={formatPrice(hall.tablePrice)} />
            </div>
            {hall.description && (
              <div className="mt-4 rounded-lg border border-gray-100 bg-gray-50 px-4 py-3 text-[13px] text-gray-600">
                {hall.description}
              </div>
            )}
          </div>
        </div>

        {/* Right: stats cards */}
        <div className="w-64 flex-shrink-0 space-y-3">
          <StatCard icon={<Calendar className="h-4 w-4" />} label="TỔNG TIỆC ĐÃ TỔ CHỨC" value="—" />
          <StatCard icon={<Calendar className="h-4 w-4" />} label="TIỆC TRONG THÁNG" value="—" />
          <StatCard icon={<TrendingUp className="h-4 w-4" />} label="DOANH THU THÁNG" value="—" />
          <StatCard icon={<Users className="h-4 w-4" />} label="BÀN TRUNG BÌNH/TIỆC" value="—" />
        </div>
      </div>

      {/* Section 2: lịch đặt sắp tới (placeholder) */}
      <div className="mt-6 rounded-xl border border-gray-100 bg-white p-6">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <span className="flex h-5 w-5 items-center justify-center rounded-full bg-gray-400 text-[10px] font-bold text-white">2</span>
            <h2 className="text-[13px] font-bold uppercase tracking-wide text-gray-700">Lịch đặt sắp tới</h2>
          </div>
        </div>
        <p className="text-[13px] text-gray-400">Dữ liệu đặt tiệc sẽ hiển thị sau khi module Tiệc cưới được triển khai.</p>
      </div>

      {/* Delete confirm */}
      {showDelete && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30">
          <div className="w-full max-w-sm rounded-2xl bg-white p-6 shadow-xl">
            <h2 className="mb-2 text-[15px] font-bold text-gray-800">Xác nhận xóa sảnh</h2>
            <p className="mb-4 text-[13px] text-gray-600">
              Bạn có chắc muốn xóa sảnh <span className="font-semibold">{hall.hallName}</span>?
            </p>
            {deleteError && <p className="mb-3 text-[12px] text-rose-600">{deleteError}</p>}
            <div className="flex justify-end gap-2">
              <button onClick={() => { setShowDelete(false); setDeleteError(null); }} className="rounded-lg border border-gray-200 px-4 py-2 text-[12px] font-semibold text-gray-600 hover:bg-gray-50 transition-colors">Hủy</button>
              <button onClick={handleDelete} disabled={deleting} className="rounded-lg bg-rose-500 px-4 py-2 text-[12px] font-semibold text-white hover:bg-rose-600 disabled:opacity-50 transition-colors">
                {deleting ? "Đang xóa..." : "Xóa"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

function InfoRow({ label, value, bold }: { label: string; value: string; bold?: boolean }) {
  return (
    <div>
      <p className="text-[11px] font-semibold text-gray-500 mb-1">{label}</p>
      <p className={cn("text-[13px] text-gray-800", bold && "font-bold")}>{value}</p>
    </div>
  );
}

function StatCard({ icon, label, value }: { icon: React.ReactNode; label: string; value: string }) {
  return (
    <div className="rounded-xl border border-gray-100 bg-white p-4">
      <div className="mb-2 flex items-center gap-2 text-gray-400">{icon}</div>
      <p className="text-[10px] font-semibold uppercase tracking-wide text-gray-400">{label}</p>
      <p className="mt-1 text-[20px] font-bold text-gray-800">{value}</p>
    </div>
  );
}