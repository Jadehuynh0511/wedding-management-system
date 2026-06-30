"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { RotateCcw, Search } from "lucide-react";

import { cn } from "@/lib/utils";
import { fetchWeddingBookings } from "@/features/booking-lookup/lib/booking-lookup-api";
import {
  BOOKING_STATUS_BADGE,
  BOOKING_STATUS_OPTIONS,
  type BookingLookupFilters,
  type WeddingBookingPage
} from "@/features/booking-lookup/model/booking-lookup";

type HallOption = {
  id: number;
  hallName: string;
};

type BookingLookupPanelProps = {
  halls: HallOption[];
};

const DEFAULT_FILTERS: BookingLookupFilters = {
  groomName: "",
  brideName: "",
  hallId: "",
  celebrationDate: "",
  status: ""
};

const PAGE_SIZE = 10;

export function BookingLookupPanel({ halls }: BookingLookupPanelProps) {
  const [draftFilters, setDraftFilters] = useState<BookingLookupFilters>(DEFAULT_FILTERS);
  const [appliedFilters, setAppliedFilters] = useState<BookingLookupFilters>(DEFAULT_FILTERS);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [result, setResult] = useState<WeddingBookingPage>({
    items: [],
    page: 0,
    size: PAGE_SIZE,
    totalElements: 0,
    totalPages: 0
  });

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);

    fetchWeddingBookings(appliedFilters, page, PAGE_SIZE)
      .then((data) => {
        if (cancelled) return;
        setResult(data);
      })
      .catch((err) => {
        if (cancelled) return;
        setResult((prev) => ({ ...prev, items: [], totalElements: 0, totalPages: 0 }));
        setError(err instanceof Error ? err.message : "Không thể tải dữ liệu tra cứu.");
      })
      .finally(() => {
        if (!cancelled) setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, [appliedFilters, page]);

  const fromIndex = useMemo(() => {
    if (result.totalElements === 0) return 0;
    return page * PAGE_SIZE + 1;
  }, [page, result.totalElements]);

  const toIndex = useMemo(() => {
    if (result.totalElements === 0) return 0;
    return Math.min((page + 1) * PAGE_SIZE, result.totalElements);
  }, [page, result.totalElements]);

  function updateDraft<K extends keyof BookingLookupFilters>(key: K, value: BookingLookupFilters[K]) {
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
      <div className="grid grid-cols-5 gap-3">
        <input
          value={draftFilters.groomName}
          onChange={(e) => updateDraft("groomName", e.target.value)}
          placeholder="Tên chú rể"
          className="rounded-lg border border-gray-200 px-3 py-2 text-[13px] outline-none focus:border-rose-300"
        />
        <input
          value={draftFilters.brideName}
          onChange={(e) => updateDraft("brideName", e.target.value)}
          placeholder="Tên cô dâu"
          className="rounded-lg border border-gray-200 px-3 py-2 text-[13px] outline-none focus:border-rose-300"
        />
        <select
          value={draftFilters.hallId}
          onChange={(e) => updateDraft("hallId", e.target.value)}
          className="rounded-lg border border-gray-200 px-3 py-2 text-[13px] outline-none focus:border-rose-300"
        >
          <option value="">Tất cả sảnh</option>
          {halls.map((hall) => (
            <option key={hall.id} value={String(hall.id)}>
              {hall.hallName}
            </option>
          ))}
        </select>
        <input
          type="date"
          value={draftFilters.celebrationDate}
          onChange={(e) => updateDraft("celebrationDate", e.target.value)}
          className="rounded-lg border border-gray-200 px-3 py-2 text-[13px] outline-none focus:border-rose-300"
        />
        <select
          value={draftFilters.status}
          onChange={(e) => updateDraft("status", e.target.value as BookingLookupFilters["status"])}
          className="rounded-lg border border-gray-200 px-3 py-2 text-[13px] outline-none focus:border-rose-300"
        >
          {BOOKING_STATUS_OPTIONS.map((opt) => (
            <option key={opt.value || "all"} value={opt.value}>
              {opt.label}
            </option>
          ))}
        </select>
      </div>

      <div className="mt-3 flex items-center justify-end gap-2">
        <button
          type="button"
          onClick={resetFilters}
          className="inline-flex items-center gap-1 rounded-lg border border-gray-200 px-3 py-2 text-[12px] font-semibold text-gray-600 hover:bg-gray-50"
        >
          <RotateCcw className="h-3.5 w-3.5" />
          Xóa lọc
        </button>
        <button
          type="button"
          onClick={applyFilters}
          className="inline-flex items-center gap-1 rounded-lg bg-rose-500 px-3 py-2 text-[12px] font-semibold text-white hover:bg-rose-600"
        >
          <Search className="h-3.5 w-3.5" />
          Áp dụng
        </button>
      </div>

      {error && <p className="mt-3 text-[12px] text-rose-600">{error}</p>}

      <div className="mt-4 overflow-x-auto">
        <table className="w-full min-w-[920px] text-[13px]">
          <thead>
            <tr className="border-y border-gray-100 bg-gray-50">
              <th className="px-3 py-2 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">Mã tiệc</th>
              <th className="px-3 py-2 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">Cô dâu</th>
              <th className="px-3 py-2 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">Chú rể</th>
              <th className="px-3 py-2 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">Sảnh</th>
              <th className="px-3 py-2 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">Ngày đãi</th>
              <th className="px-3 py-2 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">Ca</th>
              <th className="px-3 py-2 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500">Số bàn</th>
              <th className="px-3 py-2 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">Trạng thái</th>
              <th className="px-3 py-2 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500">Thao tác</th>
            </tr>
          </thead>
          <tbody>
            {loading && (
              <tr>
                <td colSpan={9} className="px-3 py-8 text-center text-gray-400">
                  Đang tải dữ liệu...
                </td>
              </tr>
            )}

            {!loading && result.items.length === 0 && (
              <tr>
                <td colSpan={9} className="px-3 py-8 text-center text-gray-400">
                  Không có tiệc cưới phù hợp với bộ lọc.
                </td>
              </tr>
            )}

            {!loading &&
              result.items.map((item) => (
                <tr key={item.id} className="border-b border-gray-50">
                  <td className="px-3 py-2.5 font-medium text-gray-700">TC{String(item.id).padStart(4, "0")}</td>
                  <td className="px-3 py-2.5 text-gray-700">{item.brideName}</td>
                  <td className="px-3 py-2.5 text-gray-700">{item.groomName}</td>
                  <td className="px-3 py-2.5 text-gray-700">{item.hallName}</td>
                  <td className="px-3 py-2.5 text-gray-700">{item.celebrationDate}</td>
                  <td className="px-3 py-2.5 text-gray-700">{item.shiftName}</td>
                  <td className="px-3 py-2.5 text-right text-gray-700">{item.tableCount}</td>
                  <td className="px-3 py-2.5">
                    <span className={cn("inline-flex rounded px-2 py-0.5 text-[11px] font-medium", BOOKING_STATUS_BADGE[item.status])}>
                      {BOOKING_STATUS_OPTIONS.find((x) => x.value === item.status)?.label ?? item.status}
                    </span>
                  </td>
                  <td className="px-3 py-2.5 text-right">
                    <Link
                      href={`/dashboard/bookings/${item.id}`}
                      className="inline-flex rounded border border-gray-200 px-2 py-1 text-[12px] font-semibold text-gray-600 hover:bg-gray-50"
                    >
                      Chi tiết
                    </Link>
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
            onClick={() => setPage((p) => Math.max(0, p - 1))}
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
            onClick={() => setPage((p) => p + 1)}
            className="rounded border border-gray-200 px-2.5 py-1.5 disabled:opacity-40"
          >
            Sau
          </button>
        </div>
      </div>
    </div>
  );
}
