"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Eye, Pencil, Trash2, RefreshCw, Plus, Search } from "lucide-react";

import { cn } from "@/lib/utils";
import { deleteShift } from "@/features/shift-catalog/lib/shift-api";
import { ShiftDetailModal } from "@/features/shift-catalog/ui/shift-detail-modal";
import type { Shift } from "@/features/shift-catalog/model/shift";

type ShiftTableProps = {
  initialData: Shift[];
};

export function ShiftTable({ initialData }: ShiftTableProps) {
  const router = useRouter();
  const [data, setData] = useState<Shift[]>(initialData);
  const [search, setSearch] = useState("");
  const [viewing, setViewing] = useState<Shift | null>(null);
  const [deleting, setDeleting] = useState<Shift | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const [refreshing, setRefreshing] = useState(false);

  const filtered = data.filter((s) =>
    s.shiftName.toLowerCase().includes(search.toLowerCase())
  );

  async function handleRefresh() {
    setRefreshing(true);
    try {
      const { fetchShifts } = await import("@/features/shift-catalog/lib/shift-api");
      const fresh = await fetchShifts();
      setData(fresh);
    } finally {
      setRefreshing(false);
    }
  }

  async function handleDelete() {
    if (!deleting) return;
    setDeleteError(null);
    try {
      await deleteShift(deleting.id);
      setData((prev) => prev.filter((s) => s.id !== deleting.id));
      setDeleting(null);
    } catch (err) {
      setDeleteError(err instanceof Error ? err.message : "Không thể xóa ca tiệc này.");
    }
  }

  return (
    <div>
      {/* Toolbar */}
      <div className="mb-4 flex items-center gap-3">
        <div className="flex flex-1 items-center gap-2 rounded-lg border border-gray-200 bg-white px-3 py-2.5">
          <Search className="h-3.5 w-3.5 flex-shrink-0 text-gray-400" />
          <input
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Tìm theo mã hoặc tên ca tiệc..."
            className="flex-1 bg-transparent text-[13px] text-gray-700 placeholder:text-gray-400 outline-none"
          />
        </div>
        <button
          onClick={handleRefresh}
          disabled={refreshing}
          className="flex items-center gap-1.5 rounded-lg border border-gray-200 bg-white px-3 py-2.5 text-[12px] font-semibold text-gray-600 hover:bg-gray-50 transition-colors disabled:opacity-50"
        >
          <RefreshCw className={cn("h-3.5 w-3.5", refreshing && "animate-spin")} />
          Làm mới
        </button>
        <button
          onClick={() => router.push("/dashboard/shifts/new")}
          className="flex items-center gap-1.5 rounded-lg bg-rose-500 px-3 py-2.5 text-[12px] font-semibold text-white hover:bg-rose-600 transition-colors"
        >
          <Plus className="h-3.5 w-3.5" />
          Thêm mới
        </button>
      </div>

      {/* Table */}
      <div className="overflow-hidden rounded-xl border border-gray-100 bg-white">
        <table className="w-full text-[13px]">
          <thead>
            <tr className="border-b border-gray-100 bg-gray-50">
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500 w-12">STT</th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500 w-28">Mã ca</th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">Tên ca</th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">Giờ bắt đầu</th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">Giờ kết thúc</th>
              <th className="px-4 py-3 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500 w-28">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {filtered.length === 0 ? (
              <tr>
                <td colSpan={6} className="py-12 text-center text-[13px] text-gray-400">
                  Không có dữ liệu
                </td>
              </tr>
            ) : (
              filtered.map((shift, index) => (
                <tr key={shift.id} className="border-b border-gray-50 hover:bg-rose-50/30 transition-colors">
                  <td className="px-4 py-3 text-gray-500">{index + 1}</td>
                  <td className="px-4 py-3 font-mono font-semibold text-gray-700">
                    CA{String(shift.id).padStart(2, "0")}
                  </td>
                  <td className="px-4 py-3 font-medium text-gray-800">{shift.shiftName}</td>
                  <td className="px-4 py-3 text-gray-700">{shift.startTime}</td>
                  <td className="px-4 py-3 text-gray-700">{shift.endTime}</td>
                  <td className="px-4 py-3">
                    <div className="flex items-center justify-end gap-2">
                      <button
                        onClick={() => setViewing(shift)}
                        className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors"
                        title="Xem chi tiết"
                      >
                        <Eye className="h-3.5 w-3.5" />
                      </button>
                      <button
                        onClick={() => router.push(`/dashboard/shifts/${shift.id}/edit`)}
                        className="rounded-lg p-1.5 text-gray-400 hover:bg-blue-50 hover:text-blue-600 transition-colors"
                        title="Chỉnh sửa"
                      >
                        <Pencil className="h-3.5 w-3.5" />
                      </button>
                      <button
                        onClick={() => { setDeleting(shift); setDeleteError(null); }}
                        className="rounded-lg p-1.5 text-gray-400 hover:bg-rose-50 hover:text-rose-600 transition-colors"
                        title="Xóa"
                      >
                        <Trash2 className="h-3.5 w-3.5" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>

      {/* Detail modal */}
      {viewing && <ShiftDetailModal shift={viewing} onClose={() => setViewing(null)} />}

      {/* Delete confirm */}
      {deleting && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30">
          <div className="w-full max-w-sm rounded-2xl bg-white p-6 shadow-xl">
            <h2 className="mb-2 text-[15px] font-bold text-gray-800">Xác nhận xóa</h2>
            <p className="mb-1 text-[13px] text-gray-600">
              Bạn có chắc muốn xóa ca <span className="font-semibold text-gray-800">{deleting.shiftName}</span>?
            </p>
            {deleteError && <p className="mb-3 text-[12px] text-rose-600">{deleteError}</p>}
            <div className="flex justify-end gap-2">
              <button
                onClick={() => { setDeleting(null); setDeleteError(null); }}
                className="rounded-lg border border-gray-200 px-4 py-2 text-[12px] font-semibold text-gray-600 hover:bg-gray-50 transition-colors"
              >
                Hủy
              </button>
              <button
                onClick={handleDelete}
                className="rounded-lg bg-rose-500 px-4 py-2 text-[12px] font-semibold text-white hover:bg-rose-600 transition-colors"
              >
                Xóa
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
