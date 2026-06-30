"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Eye, Pencil, Trash2, RefreshCw, Plus, Search } from "lucide-react";

import { cn } from "@/lib/utils";
import { deleteHall } from "@/features/hall-catalog/lib/hall-api";
import { HALL_STATUS_CONFIG, type Hall, type HallStatus } from "@/features/hall-catalog/model/hall";
import type { HallType } from "@/features/hall-type-catalog/model/hall-type";

type HallTableProps = {
  initialData: Hall[];
  hallTypes: HallType[];
};

function formatPrice(value: number) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value);
}

const PAGE_SIZE = 10;

export function HallTable({ initialData, hallTypes }: HallTableProps) {
  const router = useRouter();
  const [data, setData] = useState<Hall[]>(initialData);
  const [search, setSearch] = useState("");
  const [typeFilter, setTypeFilter] = useState<string>("ALL");
  const [statusFilter, setStatusFilter] = useState<"ALL" | HallStatus>("ALL");
  const [page, setPage] = useState(1);
  const [refreshing, setRefreshing] = useState(false);
  const [deleting, setDeleting] = useState<Hall | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const filtered = data.filter((h) => {
    const matchSearch = h.hallName.toLowerCase().includes(search.toLowerCase());
    const matchType = typeFilter === "ALL" || String(h.hallTypeId) === typeFilter;
    const matchStatus = statusFilter === "ALL" || h.status === statusFilter;
    return matchSearch && matchType && matchStatus;
  });

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const paginated = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  async function handleRefresh() {
    setRefreshing(true);
    try {
      const { fetchHalls } = await import("@/features/hall-catalog/lib/hall-api");
      const fresh = await fetchHalls();
      setData(fresh);
      setPage(1);
    } finally {
      setRefreshing(false);
    }
  }

  async function handleDelete() {
    if (!deleting) return;
    setDeleteError(null);
    try {
      await deleteHall(deleting.id);
      setData((prev) => prev.filter((h) => h.id !== deleting.id));
      setDeleting(null);
    } catch (err) {
      setDeleteError(err instanceof Error ? err.message : "Không thể xóa sảnh.");
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
            onChange={(e) => { setSearch(e.target.value); setPage(1); }}
            placeholder="Tìm theo mã sảnh, tên sảnh..."
            className="flex-1 bg-transparent text-[13px] text-gray-700 placeholder:text-gray-400 outline-none"
          />
        </div>
        <select
          value={typeFilter}
          onChange={(e) => { setTypeFilter(e.target.value); setPage(1); }}
          className="rounded-lg border border-gray-200 bg-white pl-3 pr-8 py-2.5 text-[13px] text-gray-600 outline-none focus:border-rose-300 transition-colors"
        >
          <option value="ALL">Tất cả loại sảnh</option>
          {hallTypes.map((ht) => (
            <option key={ht.id} value={String(ht.id)}>{ht.hallTypeName}</option>
          ))}
        </select>
        <select
          value={statusFilter}
          onChange={(e) => { setStatusFilter(e.target.value as "ALL" | HallStatus); setPage(1); }}
          className="rounded-lg border border-gray-200 bg-white pl-3 pr-8 py-2.5 text-[13px] text-gray-600 outline-none focus:border-rose-300 transition-colors"
        >
          <option value="ALL">Tất cả trạng thái</option>
          <option value="TRONG">Trống</option>
          <option value="DANG_DUNG">Đang dùng</option>
          <option value="BAO_TRI">Bảo trì</option>
        </select>
        <button
          onClick={handleRefresh}
          disabled={refreshing}
          className="flex items-center gap-1.5 rounded-lg border border-gray-200 bg-white px-3 py-2.5 text-[12px] font-semibold text-gray-600 hover:bg-gray-50 transition-colors disabled:opacity-50"
        >
          <RefreshCw className={cn("h-3.5 w-3.5", refreshing && "animate-spin")} />
          Làm mới
        </button>
        <button
          onClick={() => router.push("/dashboard/halls/new")}
          className="flex items-center gap-1.5 rounded-lg bg-rose-500 px-3 py-2.5 text-[12px] font-semibold text-white hover:bg-rose-600 transition-colors"
        >
          <Plus className="h-3.5 w-3.5" />
          Thêm sảnh
        </button>
      </div>

      {/* Table */}
      <div className="overflow-hidden rounded-xl border border-gray-100 bg-white">
        <table className="w-full text-[13px]">
          <thead>
            <tr className="border-b border-gray-100 bg-gray-50">
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500 w-12">STT</th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500 w-24">Mã sảnh</th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">Tên sảnh</th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500 w-32">Loại sảnh</th>
              <th className="px-4 py-3 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500 w-36 whitespace-nowrap">Số bàn tối đa</th>
              <th className="px-4 py-3 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500 w-48 whitespace-nowrap">Đơn giá bàn tối thiểu</th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500 w-32 whitespace-nowrap">Trạng thái</th>
              <th className="px-4 py-3 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500 w-28 whitespace-nowrap">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {paginated.length === 0 ? (
              <tr>
                <td colSpan={8} className="py-12 text-center text-[13px] text-gray-400">Không có dữ liệu</td>
              </tr>
            ) : (
              paginated.map((hall, index) => {
                const status = HALL_STATUS_CONFIG[hall.status];
                return (
                  <tr key={hall.id} className="border-b border-gray-50 hover:bg-rose-50/30 transition-colors">
                    <td className="px-4 py-3 text-gray-500">{(page - 1) * PAGE_SIZE + index + 1}</td>
                    <td className="px-4 py-3 font-mono font-bold text-gray-700">
                      S{String(hall.id).padStart(3, "0")}
                    </td>
                    <td className="px-4 py-3 font-medium text-gray-800">{hall.hallName}</td>
                    <td className="px-4 py-3 text-gray-600">{hall.hallTypeName}</td>
                    <td className="px-4 py-3 text-right text-gray-700">{hall.maxCapacity}</td>
                    <td className="px-4 py-3 text-right font-medium text-gray-800">{formatPrice(hall.tablePrice)}</td>
                    <td className="px-4 py-3">
                      <span className={cn("rounded-full px-2.5 py-1 text-[11px] font-semibold", status.className)}>
                        {status.label}
                      </span>
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          onClick={() => router.push(`/dashboard/halls/${hall.id}`)}
                          className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors"
                          title="Xem chi tiết"
                        >
                          <Eye className="h-3.5 w-3.5" />
                        </button>
                        <button
                          onClick={() => router.push(`/dashboard/halls/${hall.id}/edit`)}
                          className="rounded-lg p-1.5 text-gray-400 hover:bg-blue-50 hover:text-blue-600 transition-colors"
                          title="Chỉnh sửa"
                        >
                          <Pencil className="h-3.5 w-3.5" />
                        </button>
                        <button
                          onClick={() => { setDeleting(hall); setDeleteError(null); }}
                          className="rounded-lg p-1.5 text-gray-400 hover:bg-rose-50 hover:text-rose-600 transition-colors"
                          title="Xóa"
                        >
                          <Trash2 className="h-3.5 w-3.5" />
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>

        {/* Pagination */}
        <div className="flex items-center justify-between border-t border-gray-100 px-4 py-3">
          <p className="text-[12px] text-gray-500">
            Hiển thị {filtered.length === 0 ? 0 : (page - 1) * PAGE_SIZE + 1}–{Math.min(page * PAGE_SIZE, filtered.length)} trên {filtered.length} sảnh
          </p>
          <div className="flex gap-2">
            <button
              onClick={() => setPage((p) => Math.max(1, p - 1))}
              disabled={page === 1}
              className="rounded-lg border border-gray-200 px-3 py-1.5 text-[12px] font-semibold text-gray-600 hover:bg-gray-50 transition-colors disabled:opacity-40"
            >
              Trước
            </button>
            <button
              onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
              disabled={page === totalPages}
              className="rounded-lg border border-gray-200 px-3 py-1.5 text-[12px] font-semibold text-gray-600 hover:bg-gray-50 transition-colors disabled:opacity-40"
            >
              Sau
            </button>
          </div>
        </div>
      </div>

      {/* Delete confirm */}
      {deleting && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30">
          <div className="w-full max-w-sm rounded-2xl bg-white p-6 shadow-xl">
            <h2 className="mb-2 text-[15px] font-bold text-gray-800">Xác nhận xóa</h2>
            <p className="mb-1 text-[13px] text-gray-600">
              Bạn có chắc muốn xóa sảnh <span className="font-semibold">{deleting.hallName}</span>?
            </p>
            {deleteError && <p className="mb-3 text-[12px] text-rose-600">{deleteError}</p>}
            <div className="flex justify-end gap-2">
              <button onClick={() => { setDeleting(null); setDeleteError(null); }} className="rounded-lg border border-gray-200 px-4 py-2 text-[12px] font-semibold text-gray-600 hover:bg-gray-50 transition-colors">Hủy</button>
              <button onClick={handleDelete} className="rounded-lg bg-rose-500 px-4 py-2 text-[12px] font-semibold text-white hover:bg-rose-600 transition-colors">Xóa</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}