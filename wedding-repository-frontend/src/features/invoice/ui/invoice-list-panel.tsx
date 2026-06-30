"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { Eye, Filter, Printer, RotateCcw } from "lucide-react";

import { fetchInvoices, type InvoiceFilters, type InvoicePage } from "@/features/invoice/lib/invoice-api";

const PAGE_SIZE = 10;
const DEFAULT_FILTERS: InvoiceFilters = {
  groomName: "",
  brideName: "",
  hallId: "",
  celebrationDate: "",
};

type HallOption = { id: number; hallName: string };

type InvoiceListPanelProps = {
  halls: HallOption[];
};

function formatCurrency(value: number) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value ?? 0);
}

function formatPaidAt(value: string) {
  return new Intl.DateTimeFormat("vi-VN", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(new Date(value));
}

function invoiceCode(invoiceId: number) {
  return `HD${String(invoiceId).padStart(4, "0")}`;
}

function bookingCode(bookingId: number) {
  return `TC${String(bookingId).padStart(4, "0")}`;
}

export function InvoiceListPanel({ halls }: InvoiceListPanelProps) {
  const [draftFilters, setDraftFilters] = useState<InvoiceFilters>(DEFAULT_FILTERS);
  const [appliedFilters, setAppliedFilters] = useState<InvoiceFilters>(DEFAULT_FILTERS);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<InvoicePage>({
    items: [],
    page: 0,
    size: PAGE_SIZE,
    totalElements: 0,
    totalPages: 0,
  });

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);

    fetchInvoices(appliedFilters, page, PAGE_SIZE)
      .then((data) => {
        if (!cancelled) setResult(data);
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : "Không thể tải danh sách hóa đơn.");
        }
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [appliedFilters, page]);

  const fromIndex = useMemo(
    () => (result.totalElements === 0 ? 0 : page * PAGE_SIZE + 1),
    [page, result.totalElements],
  );
  const toIndex = useMemo(
    () => (result.totalElements === 0 ? 0 : Math.min((page + 1) * PAGE_SIZE, result.totalElements)),
    [page, result.totalElements],
  );

  function updateDraft<K extends keyof InvoiceFilters>(key: K, value: InvoiceFilters[K]) {
    setDraftFilters((prev) => ({ ...prev, [key]: value }));
  }

  function applyFilters() {
    setPage(0);
    setAppliedFilters(draftFilters);
  }

  function resetFilters() {
    setDraftFilters(DEFAULT_FILTERS);
    setAppliedFilters(DEFAULT_FILTERS);
    setPage(0);
  }

  return (
    <div className="rounded-xl border border-gray-100 bg-white p-5">
      <div className="grid grid-cols-12 gap-3">
        <div className="col-span-4">
          <input
            value={draftFilters.groomName}
            onChange={(event) => updateDraft("groomName", event.target.value)}
            placeholder="Tên chú rể"
            className="w-full rounded-lg border border-gray-200 px-3 py-2 text-[13px] outline-none focus:border-rose-300"
          />
        </div>
        <div className="col-span-4">
          <input
            value={draftFilters.brideName}
            onChange={(event) => updateDraft("brideName", event.target.value)}
            placeholder="Tên cô dâu"
            className="w-full rounded-lg border border-gray-200 px-3 py-2 text-[13px] outline-none focus:border-rose-300"
          />
        </div>
        <div className="col-span-4">
          <select
            value={draftFilters.hallId}
            onChange={(event) => updateDraft("hallId", event.target.value)}
            className="w-full rounded-lg border border-gray-200 px-3 py-2 text-[13px] outline-none focus:border-rose-300"
          >
            <option value="">Tất cả sảnh</option>
            {halls.map((hall) => (
              <option key={hall.id} value={String(hall.id)}>
                {hall.hallName}
              </option>
            ))}
          </select>
        </div>
        <div className="col-span-3">
          <input
            type="date"
            value={draftFilters.celebrationDate}
            onChange={(event) => updateDraft("celebrationDate", event.target.value)}
            className="w-full rounded-lg border border-gray-200 px-3 py-2 text-[13px] outline-none focus:border-rose-300"
          />
        </div>
        <div className="col-span-9 flex items-center justify-end gap-2">
          <button
            type="button"
            onClick={resetFilters}
            className="inline-flex items-center gap-1 rounded-lg border border-gray-200 px-3 py-2 text-[12px] font-semibold text-gray-600 hover:bg-gray-50"
          >
            <RotateCcw className="h-3.5 w-3.5" />
            Làm mới
          </button>
          <button
            type="button"
            onClick={applyFilters}
            className="inline-flex items-center gap-1 rounded-lg bg-rose-500 px-3 py-2 text-[12px] font-semibold text-white hover:bg-rose-600"
          >
            <Filter className="h-3.5 w-3.5" />
            Lọc
          </button>
        </div>
      </div>

      {error && <p className="mt-3 text-[12px] text-rose-600">{error}</p>}

      <div className="mt-4 overflow-x-auto">
        <table className="w-full min-w-[980px] text-[13px]">
          <thead>
            <tr className="border-y border-gray-100 bg-gray-50">
              <th className="px-3 py-2 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Mã HĐ
              </th>
              <th className="px-3 py-2 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Mã tiệc
              </th>
              <th className="px-3 py-2 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Cô dâu - chú rể
              </th>
              <th className="px-3 py-2 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Ngày đãi
              </th>
              <th className="px-3 py-2 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Ngày thanh toán
              </th>
              <th className="px-3 py-2 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Tổng tiền
              </th>
              <th className="px-3 py-2 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Thao tác
              </th>
            </tr>
          </thead>
          <tbody>
            {loading && (
              <tr>
                <td colSpan={7} className="px-3 py-8 text-center text-gray-400">
                  Đang tải dữ liệu...
                </td>
              </tr>
            )}
            {!loading && result.items.length === 0 && (
              <tr>
                <td colSpan={7} className="px-3 py-8 text-center text-gray-400">
                  Chưa có hóa đơn phù hợp.
                </td>
              </tr>
            )}
            {!loading &&
              result.items.map((item) => (
                <tr key={item.id} className="border-b border-gray-50">
                  <td className="px-3 py-2.5 font-medium text-gray-700">{invoiceCode(item.id)}</td>
                  <td className="px-3 py-2.5 font-medium text-gray-700">
                    {bookingCode(item.weddingBookingId)}
                  </td>
                  <td className="px-3 py-2.5 text-gray-700">{item.brideName} - {item.groomName}</td>
                  <td className="px-3 py-2.5 text-gray-700">{item.celebrationDate}</td>
                  <td className="px-3 py-2.5 text-gray-700">{formatPaidAt(item.paidAt)}</td>
                  <td className="px-3 py-2.5 text-right font-semibold text-gray-700">
                    {formatCurrency(item.finalAmount)}
                  </td>
                  <td className="px-3 py-2.5">
                    <div className="flex items-center justify-end gap-2">
                      <Link
                        href={`/dashboard/invoices/${item.id}`}
                        className="rounded p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-700"
                        title="Xem chi tiết hóa đơn"
                      >
                        <Eye className="h-4 w-4" />
                      </Link>
                      <Link
                        href={`/dashboard/invoices/${item.id}?print=1`}
                        className="rounded p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-700"
                        title="Mở để in hóa đơn"
                      >
                        <Printer className="h-3.5 w-3.5" />
                      </Link>
                    </div>
                  </td>
                </tr>
              ))}
          </tbody>
        </table>
      </div>

      <div className="mt-3 flex items-center justify-between text-[12px] text-gray-500">
        <span>
          Hiển thị {fromIndex}–{toIndex} trên tổng {result.totalElements} kết quả
        </span>
        <div className="flex items-center gap-2">
          <button
            type="button"
            disabled={page <= 0}
            onClick={() => setPage((current) => Math.max(0, current - 1))}
            className="rounded border border-gray-200 px-2.5 py-1.5 disabled:opacity-40"
          >
            Trước
          </button>
          <span>
            Trang {result.totalPages === 0 ? 0 : page + 1}/{Math.max(result.totalPages, 1)}
          </span>
          <button
            type="button"
            disabled={result.totalPages === 0 || page >= result.totalPages - 1}
            onClick={() => setPage((current) => current + 1)}
            className="rounded border border-gray-200 px-2.5 py-1.5 disabled:opacity-40"
          >
            Sau
          </button>
        </div>
      </div>
    </div>
  );
}
