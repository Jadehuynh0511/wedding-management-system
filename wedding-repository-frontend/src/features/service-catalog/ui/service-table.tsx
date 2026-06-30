"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { Pencil, Eye, EyeOff, Trash2, RefreshCw, Plus, Search } from "lucide-react";

import { cn } from "@/lib/utils";
import { updateService, deleteService } from "@/features/service-catalog/lib/service-api";
import type { ServiceItem, ServiceItemStatus } from "@/features/service-catalog/model/service";

type ServiceTableProps = {
  initialData: ServiceItem[];
};

function formatPrice(value: number) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value);
}

function formatDate(instant: string) {
  return new Date(instant).toLocaleDateString("vi-VN", { day: "2-digit", month: "2-digit", year: "numeric" });
}

export function ServiceTable({ initialData }: ServiceTableProps) {
  const router = useRouter();
  const [data, setData] = useState<ServiceItem[]>(initialData);
  const [search, setSearch] = useState("");
  const [refreshing, setRefreshing] = useState(false);
  const [togglingId, setTogglingId] = useState<number | null>(null);
  const [deleting, setDeleting] = useState<ServiceItem | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const filtered = data.filter((s) =>
    s.serviceName.toLowerCase().includes(search.toLowerCase())
  );

  async function handleRefresh() {
    setRefreshing(true);
    try {
      const { fetchServices } = await import("@/features/service-catalog/lib/service-api");
      const fresh = await fetchServices();
      setData(fresh);
    } finally {
      setRefreshing(false);
    }
  }

  async function handleDelete() {
    if (!deleting) return;
    setDeleteError(null);
    try {
      await deleteService(deleting.id);
      setData((prev) => prev.filter((s) => s.id !== deleting.id));
      setDeleting(null);
    } catch (err) {
      setDeleteError(err instanceof Error ? err.message : "Không thể xóa dịch vụ.");
    }
  }

  async function handleToggleStatus(item: ServiceItem) {
    const nextStatus: ServiceItemStatus = item.status === "HOAT_DONG" ? "NGUNG_HOAT_DONG" : "HOAT_DONG";
    setTogglingId(item.id);
    try {
      const updated = await updateService(item.id, {
        serviceName: item.serviceName,
        serviceCategory: item.serviceCategory,
        unitName: item.unitName,
        status: nextStatus,
        description: item.description ?? undefined
      });
      setData((prev) => prev.map((s) => (s.id === updated.id ? updated : s)));
    } finally {
      setTogglingId(null);
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
            placeholder="Tìm kiếm dịch vụ theo mã hoặc tên..."
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
          onClick={() => router.push("/dashboard/services/new")}
          className="flex items-center gap-1.5 rounded-lg bg-rose-500 px-3 py-2.5 text-[12px] font-semibold text-white hover:bg-rose-600 transition-colors"
        >
          <Plus className="h-3.5 w-3.5" />
          Thêm dịch vụ
        </button>
      </div>

      {/* Table */}
      <div className="overflow-hidden rounded-xl border border-gray-100 bg-white">
        <table className="w-full text-[13px]">
          <thead>
            <tr className="border-b border-gray-100 bg-gray-50">
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500 w-12">STT</th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500 w-28">Mã dịch vụ</th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">Tên dịch vụ</th>
              <th className="px-4 py-3 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500 w-36 whitespace-nowrap">Đơn giá</th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500 w-36 whitespace-nowrap">Ngày cập nhật</th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">Ghi chú</th>
              <th className="px-4 py-3 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500 w-24 whitespace-nowrap">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {filtered.length === 0 ? (
              <tr>
                <td colSpan={7} className="py-12 text-center text-[13px] text-gray-400">Không có dữ liệu</td>
              </tr>
            ) : (
              filtered.map((item, index) => {
                const isToggling = togglingId === item.id;
                return (
                  <tr
                    key={item.id}
                    className={cn(
                      "border-b border-gray-50 transition-colors",
                      !item.active ? "opacity-60" : "hover:bg-rose-50/30"
                    )}
                  >
                    <td className="px-4 py-3 text-gray-500">{index + 1}</td>
                    <td className="px-4 py-3 font-mono font-bold text-gray-700">
                      DV{String(item.id).padStart(2, "0")}
                    </td>
                    <td className="px-4 py-3 font-medium text-gray-800">{item.serviceName}</td>
                    <td className="px-4 py-3 text-right font-medium text-gray-800">
                      {formatPrice(item.currentPrice)}
                    </td>
                    <td className="px-4 py-3 text-gray-600">
                      {formatDate(item.priceEffectiveFrom)}
                    </td>
                    <td className="px-4 py-3 max-w-xs truncate text-gray-500">
                      {item.description || "—"}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          onClick={() => router.push(`/dashboard/services/${item.id}/edit`)}
                          className="rounded-lg p-1.5 text-gray-400 hover:bg-blue-50 hover:text-blue-600 transition-colors"
                          title="Chỉnh sửa"
                        >
                          <Pencil className="h-3.5 w-3.5" />
                        </button>
                        <button
                          onClick={() => handleToggleStatus(item)}
                          disabled={isToggling}
                          className={cn(
                            "rounded-lg p-1.5 transition-colors disabled:opacity-40",
                            item.active
                              ? "text-gray-400 hover:bg-gray-100 hover:text-gray-600"
                              : "text-gray-300 hover:bg-green-50 hover:text-green-600"
                          )}
                          title={item.active ? "Ngừng hoạt động" : "Kích hoạt lại"}
                        >
                          {item.active
                            ? <Eye className="h-3.5 w-3.5" />
                            : <EyeOff className="h-3.5 w-3.5" />
                          }
                        </button>
                        <button
                          onClick={() => { setDeleting(item); setDeleteError(null); }}
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
      </div>

      {/* Delete confirm */}
      {deleting && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30">
          <div className="w-full max-w-sm rounded-2xl bg-white p-6 shadow-xl">
            <h2 className="mb-2 text-[15px] font-bold text-gray-800">Xác nhận xóa</h2>
            <p className="mb-4 text-[13px] text-gray-600">
              Bạn có chắc muốn xóa dịch vụ <span className="font-semibold">{deleting.serviceName}</span>?
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