"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Eye, Pencil, Plus, RefreshCw, Search, Trash2 } from "lucide-react";

import { cn } from "@/lib/utils";
import { deleteHallType } from "@/features/hall-type-catalog/lib/hall-type-api";
import { HallTypeDetailModal } from "@/features/hall-type-catalog/ui/hall-type-detail-modal";
import type { Hall, HallType } from "@/features/hall-type-catalog/model/hall-type";

type HallTypeTableProps = {
  initialData: HallType[];
  halls: Hall[];
};

function formatPrice(value: number) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value);
}

function countHallsByType(halls: Hall[]) {
  return halls.reduce<Record<number, number>>((counts, hall) => {
    counts[hall.hallTypeId] = (counts[hall.hallTypeId] ?? 0) + 1;
    return counts;
  }, {});
}

export function HallTypeTable({ initialData, halls }: HallTypeTableProps) {
  const router = useRouter();
  const [data, setData] = useState<HallType[]>(initialData);
  const [hallUsageCounts, setHallUsageCounts] = useState<Record<number, number>>(() =>
    countHallsByType(halls),
  );
  const [search, setSearch] = useState("");
  const [viewing, setViewing] = useState<HallType | null>(null);
  const [refreshing, setRefreshing] = useState(false);
  const [deletingId, setDeletingId] = useState<number | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const filtered = data.filter((hallType) =>
    `${hallType.id} ${hallType.hallTypeName}`.toLowerCase().includes(search.toLowerCase()),
  );

  async function handleRefresh() {
    setRefreshing(true);
    try {
      const [{ fetchHallTypes }, { fetchHalls }] = await Promise.all([
        import("@/features/hall-type-catalog/lib/hall-type-api"),
        import("@/features/hall-catalog/lib/hall-api"),
      ]);
      const [freshHallTypes, freshHalls] = await Promise.all([
        fetchHallTypes(),
        fetchHalls().catch(() => []),
      ]);
      setData(freshHallTypes);
      setHallUsageCounts(countHallsByType(freshHalls));
    } finally {
      setRefreshing(false);
    }
  }

  async function handleDelete(hallType: HallType) {
    const usageCount = hallUsageCounts[hallType.id] ?? 0;
    if (usageCount > 0) return;

    const confirmed = window.confirm(`Xóa loại sảnh ${hallType.hallTypeName}?`);
    if (!confirmed) return;

    setDeletingId(hallType.id);
    setDeleteError(null);
    try {
      await deleteHallType(hallType.id);
      setData((current) => current.filter((item) => item.id !== hallType.id));
      router.refresh();
    } catch (error) {
      setDeleteError(error instanceof Error ? error.message : "Không thể xóa loại sảnh.");
    } finally {
      setDeletingId(null);
    }
  }

  return (
    <div>
      <div className="mb-4 flex items-center gap-3">
        <div className="flex flex-1 items-center gap-2 rounded-lg border border-gray-200 bg-white px-3 py-2.5">
          <Search className="h-3.5 w-3.5 flex-shrink-0 text-gray-400" />
          <input
            value={search}
            onChange={(event) => setSearch(event.target.value)}
            placeholder="Tìm theo mã hoặc tên loại sảnh..."
            className="flex-1 bg-transparent text-[13px] text-gray-700 placeholder:text-gray-400 outline-none"
          />
        </div>
        <button
          onClick={handleRefresh}
          disabled={refreshing}
          className="flex items-center gap-1.5 rounded-lg border border-gray-200 bg-white px-3 py-2.5 text-[12px] font-semibold text-gray-600 transition-colors hover:bg-gray-50 disabled:opacity-50"
        >
          <RefreshCw className={cn("h-3.5 w-3.5", refreshing && "animate-spin")} />
          Làm mới
        </button>
        <button
          onClick={() => router.push("/dashboard/hall-types/new")}
          className="flex items-center gap-1.5 rounded-lg bg-rose-500 px-3 py-2.5 text-[12px] font-semibold text-white transition-colors hover:bg-rose-600"
        >
          <Plus className="h-3.5 w-3.5" />
          Thêm mới
        </button>
      </div>

      {deleteError && (
        <div className="mb-3 rounded-lg border border-rose-100 bg-rose-50 px-4 py-2 text-[12px] text-rose-600">
          {deleteError}
        </div>
      )}

      <div className="overflow-hidden rounded-xl border border-gray-100 bg-white">
        <table className="w-full text-[13px]">
          <thead>
            <tr className="border-b border-gray-100 bg-gray-50">
              <th className="w-12 px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                STT
              </th>
              <th className="w-28 px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Mã loại
              </th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Tên loại
              </th>
              <th className="px-4 py-3 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Đơn giá bàn tối thiểu
              </th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Mô tả
              </th>
              <th className="w-32 px-4 py-3 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Thao tác
              </th>
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
              filtered.map((hallType, index) => {
                const usageCount = hallUsageCounts[hallType.id] ?? 0;
                const canDelete = usageCount === 0;

                return (
                  <tr
                    key={hallType.id}
                    className="border-b border-gray-50 transition-colors hover:bg-rose-50/30"
                  >
                    <td className="px-4 py-3 text-gray-500">{index + 1}</td>
                    <td className="px-4 py-3 font-mono font-semibold text-gray-700">
                      LS{String(hallType.id).padStart(2, "0")}
                    </td>
                    <td className="px-4 py-3 font-medium text-gray-800">{hallType.hallTypeName}</td>
                    <td className="px-4 py-3 text-right font-medium text-gray-800">
                      {formatPrice(hallType.minimumTablePrice)}
                    </td>
                    <td className="max-w-xs truncate px-4 py-3 text-gray-500">
                      {hallType.description || "—"}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          onClick={() => setViewing(hallType)}
                          className="rounded-lg p-1.5 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600"
                          title="Xem chi tiết"
                        >
                          <Eye className="h-3.5 w-3.5" />
                        </button>
                        <button
                          onClick={() => router.push(`/dashboard/hall-types/${hallType.id}/edit`)}
                          className="rounded-lg p-1.5 text-gray-400 transition-colors hover:bg-blue-50 hover:text-blue-600"
                          title="Chỉnh sửa"
                        >
                          <Pencil className="h-3.5 w-3.5" />
                        </button>
                        <button
                          onClick={() => handleDelete(hallType)}
                          disabled={!canDelete || deletingId === hallType.id}
                          className={cn(
                            "rounded-lg p-1.5 transition-colors",
                            canDelete
                              ? "text-gray-400 hover:bg-rose-50 hover:text-rose-600"
                              : "cursor-not-allowed text-gray-200",
                          )}
                          title={
                            canDelete
                              ? "Xóa loại sảnh"
                              : `Không thể xóa vì hiện có ${usageCount} sảnh đang sử dụng loại này`
                          }
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
      </div>

      {viewing && <HallTypeDetailModal hallType={viewing} onClose={() => setViewing(null)} />}
    </div>
  );
}
