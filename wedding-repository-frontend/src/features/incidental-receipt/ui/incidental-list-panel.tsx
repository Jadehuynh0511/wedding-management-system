"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { Eye, Filter, Plus, RotateCcw, X } from "lucide-react";

import { cn } from "@/lib/utils";
import { fetchWeddingBookings } from "@/features/booking-lookup/lib/booking-lookup-api";
import {
  BOOKING_STATUS_BADGE,
  BOOKING_STATUS_OPTIONS,
  type BookingLookupFilters,
  type WeddingBookingPage,
} from "@/features/booking-lookup/model/booking-lookup";
import {
  fetchIncidentalReceipts,
  type IncidentalReceiptResponse,
} from "@/features/incidental-receipt/lib/incidental-api";

const PAGE_SIZE = 10;
const DEFAULT_FILTERS: BookingLookupFilters = {
  groomName: "",
  brideName: "",
  hallId: "",
  celebrationDate: "",
  status: "",
};

type HallOption = { id: number; hallName: string };

type IncidentalListPanelProps = {
  halls: HallOption[];
};

type DetailState = {
  bookingId: number;
  bookingCode: string;
  coupleName: string;
  receipts: IncidentalReceiptResponse[];
};

function formatCurrency(value: number) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value);
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat("vi-VN", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(new Date(value));
}

function receiptCode(id: number) {
  return `PS${String(id).padStart(4, "0")}`;
}

export function IncidentalListPanel({ halls }: IncidentalListPanelProps) {
  const [draftFilters, setDraftFilters] = useState<BookingLookupFilters>(DEFAULT_FILTERS);
  const [appliedFilters, setAppliedFilters] = useState<BookingLookupFilters>(DEFAULT_FILTERS);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [detailLoadingId, setDetailLoadingId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [detailError, setDetailError] = useState<string | null>(null);
  const [detail, setDetail] = useState<DetailState | null>(null);
  const [result, setResult] = useState<WeddingBookingPage>({
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

    fetchWeddingBookings(appliedFilters, page, PAGE_SIZE)
      .then((data) => {
        if (!cancelled) setResult(data);
      })
      .catch((err) => {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : "Không thể tải danh sách tiệc cưới.");
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

  function updateDraft<K extends keyof BookingLookupFilters>(
    key: K,
    value: BookingLookupFilters[K],
  ) {
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

  async function openDetail(booking: WeddingBookingPage["items"][number]) {
    setDetailError(null);
    setDetailLoadingId(booking.id);
    try {
      const receipts = await fetchIncidentalReceipts(booking.id);
      setDetail({
        bookingId: booking.id,
        bookingCode: `TC${String(booking.id).padStart(4, "0")}`,
        coupleName: `${booking.brideName} - ${booking.groomName}`,
        receipts,
      });
    } catch (err) {
      setDetailError(
        err instanceof Error ? err.message : "Không thể tải chi tiết dịch vụ phát sinh.",
      );
    } finally {
      setDetailLoadingId(null);
    }
  }

  return (
    <>
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
          <div className="col-span-3">
            <select
              value={draftFilters.status}
              onChange={(event) =>
                updateDraft("status", event.target.value as BookingLookupFilters["status"])
              }
              className="w-full rounded-lg border border-gray-200 px-3 py-2 text-[13px] outline-none focus:border-rose-300"
            >
              {BOOKING_STATUS_OPTIONS.map((option) => (
                <option key={option.value || "all"} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
          <div className="col-span-6 flex items-center justify-end gap-2">
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
        {detailError && <p className="mt-3 text-[12px] text-rose-600">{detailError}</p>}

        <div className="mt-4 overflow-x-auto">
          <table className="w-full min-w-[900px] text-[13px]">
            <thead>
              <tr className="border-y border-gray-100 bg-gray-50">
                <th className="px-3 py-2 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">Mã phiếu</th>
                <th className="px-3 py-2 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">Mã tiệc</th>
                <th className="px-3 py-2 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">Cô dâu - chú rể</th>
                <th className="px-3 py-2 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">Ngày đãi</th>
                <th className="px-3 py-2 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">Trạng thái</th>
                <th className="px-3 py-2 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500">Thao tác</th>
              </tr>
            </thead>
            <tbody>
              {loading && (
                <tr>
                  <td colSpan={6} className="px-3 py-8 text-center text-gray-400">
                    Đang tải dữ liệu...
                  </td>
                </tr>
              )}
              {!loading && result.items.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-3 py-8 text-center text-gray-400">
                    Chưa có dữ liệu phù hợp.
                  </td>
                </tr>
              )}
              {!loading &&
                result.items.map((item) => (
                  <tr key={item.id} className="border-b border-gray-50">
                    <td className="px-3 py-2.5 font-medium text-gray-700">
                      PS{String(item.id).padStart(4, "0")}
                    </td>
                    <td className="px-3 py-2.5 font-medium text-gray-700">
                      TC{String(item.id).padStart(4, "0")}
                    </td>
                    <td className="px-3 py-2.5 text-gray-700">
                      {item.brideName} - {item.groomName}
                    </td>
                    <td className="px-3 py-2.5 text-gray-700">{item.celebrationDate}</td>
                    <td className="px-3 py-2.5">
                      <span
                        className={cn(
                          "inline-flex rounded px-2 py-0.5 text-[11px] font-medium",
                          BOOKING_STATUS_BADGE[item.status],
                        )}
                      >
                        {BOOKING_STATUS_OPTIONS.find((option) => option.value === item.status)?.label ?? item.status}
                      </span>
                    </td>
                    <td className="px-3 py-2.5">
                      <div className="flex items-center justify-end gap-2">
                        <button
                          type="button"
                          onClick={() => openDetail(item)}
                          disabled={detailLoadingId === item.id}
                          className="rounded p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-700 disabled:opacity-40"
                          title="Xem chi tiết"
                        >
                          <Eye className="h-4 w-4" />
                        </button>
                        <Link
                          href={`/dashboard/incidentals/new?bookingId=${item.id}`}
                          className="rounded p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-700"
                          title="Lập phiếu"
                        >
                          <Plus className="h-3.5 w-3.5" />
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
            Hiển thị {fromIndex}-{toIndex} trên tổng {result.totalElements} kết quả
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

      {detail && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 px-4">
          <div className="max-h-[88vh] w-full max-w-4xl overflow-hidden rounded-xl bg-white shadow-2xl">
            <div className="flex items-start justify-between border-b border-gray-100 px-5 py-4">
              <div>
                <h2 className="text-[16px] font-bold text-gray-800">Chi tiết dịch vụ phát sinh</h2>
                <p className="mt-1 text-[12px] text-gray-500">
                  {detail.bookingCode} - {detail.coupleName}
                </p>
              </div>
              <button
                type="button"
                onClick={() => setDetail(null)}
                className="rounded p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-700"
                title="Đóng"
              >
                <X className="h-4 w-4" />
              </button>
            </div>
            <div className="max-h-[72vh] overflow-y-auto px-5 py-4">
              {detail.receipts.length === 0 ? (
                <div className="rounded-lg border border-dashed border-gray-200 py-8 text-center text-[13px] text-gray-400">
                  Tiệc này chưa có phiếu dịch vụ phát sinh.
                </div>
              ) : (
                <div className="space-y-4">
                  {detail.receipts.map((receipt) => (
                    <section key={receipt.id} className="rounded-lg border border-gray-100">
                      <div className="flex items-center justify-between gap-3 border-b border-gray-100 bg-gray-50 px-4 py-3">
                        <div>
                          <p className="text-[13px] font-bold text-gray-700">{receiptCode(receipt.id)}</p>
                          <p className="mt-0.5 text-[12px] text-gray-500">
                            Ngày lập: {formatDateTime(receipt.recordedAt)}
                          </p>
                        </div>
                        <div className="text-right">
                          <p className="text-[12px] text-gray-500">Tổng tiền</p>
                          <p className="text-[14px] font-bold text-rose-600">
                            {formatCurrency(receipt.totalAmount)}
                          </p>
                        </div>
                      </div>
                      <div className="overflow-x-auto">
                        <table className="w-full min-w-[720px] text-[12px]">
                          <thead>
                            <tr className="border-b border-gray-100">
                              <th className="px-4 py-2 text-left font-semibold text-gray-500">Dịch vụ</th>
                              <th className="px-4 py-2 text-left font-semibold text-gray-500">Đơn vị</th>
                              <th className="px-4 py-2 text-right font-semibold text-gray-500">SL</th>
                              <th className="px-4 py-2 text-right font-semibold text-gray-500">Đơn giá</th>
                              <th className="px-4 py-2 text-right font-semibold text-gray-500">Thành tiền</th>
                              <th className="px-4 py-2 text-left font-semibold text-gray-500">Ghi chú</th>
                            </tr>
                          </thead>
                          <tbody>
                            {receipt.items.map((line) => (
                              <tr key={line.id} className="border-b border-gray-50 last:border-b-0">
                                <td className="px-4 py-2.5 font-medium text-gray-700">{line.serviceName}</td>
                                <td className="px-4 py-2.5 text-gray-600">{line.unitName}</td>
                                <td className="px-4 py-2.5 text-right text-gray-600">{line.quantity}</td>
                                <td className="px-4 py-2.5 text-right text-gray-600">
                                  {formatCurrency(line.appliedUnitPrice)}
                                </td>
                                <td className="px-4 py-2.5 text-right font-semibold text-gray-700">
                                  {formatCurrency(line.lineTotal)}
                                </td>
                                <td className="px-4 py-2.5 text-gray-600">{line.notes || "-"}</td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                      {receipt.notes && (
                        <p className="border-t border-gray-100 px-4 py-3 text-[12px] text-gray-500">
                          Ghi chú phiếu: {receipt.notes}
                        </p>
                      )}
                    </section>
                  ))}
                </div>
              )}
              <div className="mt-4 flex justify-end">
                <Link
                  href={`/dashboard/incidentals/new?bookingId=${detail.bookingId}`}
                  className="inline-flex items-center gap-1.5 rounded-lg bg-rose-500 px-3 py-2 text-[12px] font-semibold text-white hover:bg-rose-600"
                >
                  <Plus className="h-3.5 w-3.5" />
                  Lập phiếu
                </Link>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}
