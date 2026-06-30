"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Pencil, Eye, EyeOff, RefreshCw, Plus, Search, Trash2 } from "lucide-react";

import { cn } from "@/lib/utils";
import { deleteMenuItem, updateMenuItem } from "@/features/menu-item-catalog/lib/menu-item-api";
import {
  MENU_ITEM_STATUS_LABEL,
  type MenuItem,
  type MenuItemStatus,
} from "@/features/menu-item-catalog/model/menu-item";

type MenuItemTableProps = {
  initialData: MenuItem[];
};

function formatPrice(value: number) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value);
}

export function MenuItemTable({ initialData }: MenuItemTableProps) {
  const router = useRouter();
  const [data, setData] = useState<MenuItem[]>(initialData);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState<"ALL" | MenuItemStatus>("ALL");
  const [refreshing, setRefreshing] = useState(false);
  const [togglingId, setTogglingId] = useState<number | null>(null);
  const [deletingId, setDeletingId] = useState<number | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<MenuItem | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const filtered = data.filter((item) => {
    const matchSearch = item.itemName.toLowerCase().includes(search.toLowerCase());
    const matchStatus = statusFilter === "ALL" || item.status === statusFilter;
    return matchSearch && matchStatus;
  });

  async function handleRefresh() {
    setRefreshing(true);
    try {
      const { fetchMenuItems } = await import("@/features/menu-item-catalog/lib/menu-item-api");
      const fresh = await fetchMenuItems();
      setData(fresh);
    } finally {
      setRefreshing(false);
    }
  }

  async function handleToggleStatus(item: MenuItem) {
    const nextStatus: MenuItemStatus = item.status === "CON" ? "HET" : "CON";
    setTogglingId(item.id);
    try {
      const updated = await updateMenuItem(item.id, {
        itemName: item.itemName,
        itemCategory: item.itemCategory,
        currentPrice: item.currentPrice,
        status: nextStatus,
        description: item.description ?? undefined,
      });
      setData((prev) => prev.map((m) => (m.id === updated.id ? updated : m)));
    } finally {
      setTogglingId(null);
    }
  }

  async function handleConfirmDelete() {
    if (!deleteTarget) return;
    setDeletingId(deleteTarget.id);
    setDeleteError(null);
    try {
      await deleteMenuItem(deleteTarget.id);
      setData((prev) => prev.filter((m) => m.id !== deleteTarget.id));
      setDeleteTarget(null);
    } catch (error) {
      setDeleteError(error instanceof Error ? error.message : "Không thể xóa món ăn.");
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
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Tìm kiếm món ăn theo mã hoặc tên..."
            className="flex-1 bg-transparent text-[13px] text-gray-700 placeholder:text-gray-400 outline-none"
          />
        </div>

        <select
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value as "ALL" | MenuItemStatus)}
          className="rounded-lg border border-gray-200 bg-white py-2.5 pl-4 pr-8 text-[13px] text-gray-600 outline-none transition-colors focus:border-rose-300"
        >
          <option value="ALL">Tất cả trạng thái</option>
          <option value="CON">Còn</option>
          <option value="HET">Hết</option>
        </select>

        <button
          onClick={handleRefresh}
          disabled={refreshing}
          className="flex items-center gap-1.5 rounded-lg border border-gray-200 bg-white px-3 py-2.5 text-[12px] font-semibold text-gray-600 transition-colors hover:bg-gray-50 disabled:opacity-50"
        >
          <RefreshCw className={cn("h-3.5 w-3.5", refreshing && "animate-spin")} />
          Làm mới
        </button>
        <button
          onClick={() => router.push("/dashboard/menu-items/new")}
          className="flex items-center gap-1.5 rounded-lg bg-rose-500 px-3 py-2.5 text-[12px] font-semibold text-white transition-colors hover:bg-rose-600"
        >
          <Plus className="h-3.5 w-3.5" />
          Thêm món ăn
        </button>
      </div>

      <div className="overflow-hidden rounded-xl border border-gray-100 bg-white">
        <table className="w-full text-[13px]">
          <thead>
            <tr className="border-b border-gray-100 bg-gray-50">
              <th className="w-12 whitespace-nowrap px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                STT
              </th>
              <th className="w-24 whitespace-nowrap px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Mã món
              </th>
              <th className="w-64 whitespace-nowrap px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Tên món ăn
              </th>
              <th className="w-32 whitespace-nowrap px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Phân loại
              </th>
              <th className="w-28 whitespace-nowrap px-4 py-3 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Đơn giá
              </th>
              <th className="w-28 whitespace-nowrap px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Trạng thái
              </th>
              <th className="whitespace-nowrap px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Ghi chú
              </th>
              <th className="w-20 whitespace-nowrap px-4 py-3 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Thao tác
              </th>
            </tr>
          </thead>
          <tbody>
            {filtered.length === 0 ? (
              <tr>
                <td colSpan={8} className="py-12 text-center text-[13px] text-gray-400">
                  Không có dữ liệu
                </td>
              </tr>
            ) : (
              filtered.map((item, index) => {
                const statusConfig = MENU_ITEM_STATUS_LABEL[item.status];
                const isToggling = togglingId === item.id;
                const isDeleting = deletingId === item.id;

                return (
                  <tr
                    key={item.id}
                    className={cn(
                      "border-b border-gray-50 transition-colors",
                      item.status === "HET" ? "opacity-60" : "hover:bg-rose-50/30",
                    )}
                  >
                    <td className="whitespace-nowrap px-4 py-3 text-gray-500">{index + 1}</td>
                    <td className="whitespace-nowrap px-4 py-3 font-mono font-semibold text-gray-700">
                      MA{String(item.id).padStart(2, "0")}
                    </td>
                    <td className="whitespace-nowrap px-4 py-3 font-medium text-gray-800">
                      {item.itemName}
                    </td>
                    <td className="whitespace-nowrap px-4 py-3 text-gray-600">
                      {item.itemCategory}
                    </td>
                    <td className="whitespace-nowrap px-4 py-3 text-right font-medium text-gray-800">
                      {formatPrice(item.currentPrice)}
                    </td>
                    <td className="whitespace-nowrap px-4 py-3">
                      <span
                        className={cn(
                          "rounded-full px-2.5 py-1 text-[11px] font-semibold",
                          statusConfig.className,
                        )}
                      >
                        {statusConfig.label}
                      </span>
                    </td>
                    <td className="max-w-xs truncate whitespace-nowrap px-4 py-3 text-gray-500">
                      {item.description || "—"}
                    </td>
                    <td className="whitespace-nowrap px-4 py-3">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          onClick={() => router.push(`/dashboard/menu-items/${item.id}/edit`)}
                          className="rounded-lg p-1.5 text-gray-400 transition-colors hover:bg-blue-50 hover:text-blue-600"
                          title="Chỉnh sửa"
                        >
                          <Pencil className="h-3.5 w-3.5" />
                        </button>
                        <button
                          onClick={() => handleToggleStatus(item)}
                          disabled={isToggling || isDeleting}
                          className={cn(
                            "rounded-lg p-1.5 transition-colors disabled:opacity-40",
                            item.status === "CON"
                              ? "text-gray-400 hover:bg-orange-50 hover:text-orange-500"
                              : "text-gray-400 hover:bg-green-50 hover:text-green-600",
                          )}
                          title={item.status === "CON" ? "Đánh dấu Hết" : "Đánh dấu Còn"}
                        >
                          {item.status === "CON" ? (
                            <Eye className="h-3.5 w-3.5" />
                          ) : (
                            <EyeOff className="h-3.5 w-3.5" />
                          )}
                        </button>
                        <button
                          onClick={() => {
                            setDeleteError(null);
                            setDeleteTarget(item);
                          }}
                          disabled={isDeleting || isToggling}
                          className="rounded-lg p-1.5 text-gray-400 transition-colors hover:bg-rose-50 hover:text-rose-600 disabled:opacity-40"
                          title="Xóa món ăn"
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

      {deleteTarget && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/35 px-4">
          <div className="w-full max-w-[480px] rounded-2xl bg-white p-5 shadow-xl">
            <h3 className="text-[22px] font-semibold leading-none tracking-tight text-gray-900">
              Xác nhận xóa
            </h3>
            <p className="mt-4 text-[18px] leading-relaxed text-gray-700">
              Bạn có chắc muốn xóa món ăn{" "}
              <span className="font-semibold">{deleteTarget.itemName}</span> không?
            </p>

            {deleteError && <p className="mt-3 text-[14px] text-rose-600">{deleteError}</p>}
            <div className="mt-5 flex justify-end gap-3">
              <button
                type="button"
                onClick={() => {
                  if (deletingId) return;
                  setDeleteError(null);
                  setDeleteTarget(null);
                }}
                className="rounded-full border border-gray-200 px-5 py-2 text-[16px] font-semibold text-gray-600 transition-colors hover:bg-gray-50"
              >
                Hủy
              </button>
              <button
                type="button"
                onClick={handleConfirmDelete}
                disabled={deletingId === deleteTarget.id}
                className="rounded-full bg-rose-500 px-5 py-2 text-[16px] font-semibold text-white transition-colors hover:bg-rose-600 disabled:cursor-not-allowed disabled:bg-rose-300"
              >
                {deletingId === deleteTarget.id ? "Đang xóa..." : "Xóa"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
