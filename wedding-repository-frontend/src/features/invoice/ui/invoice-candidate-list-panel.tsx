"use client";

import Link from "next/link";
import { useEffect, useMemo, useState } from "react";
import { createPortal } from "react-dom";
import { Eye, Filter, Plus, RotateCcw } from "lucide-react";

import { cn } from "@/lib/utils";
import { fetchInvoiceBookingCandidates } from "@/features/invoice/lib/invoice-api";
import {
  BOOKING_STATUS_BADGE,
  BOOKING_STATUS_OPTIONS,
  type BookingLookupFilters,
  type WeddingBookingPage,
  type WeddingBookingStatus,
} from "@/features/booking-lookup/model/booking-lookup";

const PAGE_SIZE = 10;
const DEFAULT_FILTERS: BookingLookupFilters = {
  groomName: "",
  brideName: "",
  hallId: "",
  celebrationDate: "",
  status: "",
};

type HallOption = { id: number; hallName: string };

type BlockedTooltipTarget = {
  anchor: HTMLElement;
  message: string;
};

type BlockedTooltipPosition = {
  left: number;
  top: number;
  placement: "top" | "bottom";
  message: string;
};

type InvoiceCandidateListPanelProps = {
  halls: HallOption[];
};

function getInvoiceBlockedReason(status: WeddingBookingStatus) {
  return status === "DA_THANH_TOAN"
    ? "Tiệc cưới này đã được thanh toán đầy đủ nên không thể lập hóa đơn mới."
    : status === "DA_HUY"
      ? "Tiệc cưới này đã bị hủy nên không thể lập hóa đơn."
      : null;
}

export function InvoiceCandidateListPanel({ halls }: InvoiceCandidateListPanelProps) {
  const [draftFilters, setDraftFilters] = useState<BookingLookupFilters>(DEFAULT_FILTERS);
  const [appliedFilters, setAppliedFilters] = useState<BookingLookupFilters>(DEFAULT_FILTERS);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [blockedTooltipTarget, setBlockedTooltipTarget] = useState<BlockedTooltipTarget | null>(
    null,
  );
  const [blockedTooltipPosition, setBlockedTooltipPosition] =
    useState<BlockedTooltipPosition | null>(null);
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

    fetchInvoiceBookingCandidates(appliedFilters, page, PAGE_SIZE)
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

  useEffect(() => {
    if (!blockedTooltipTarget) {
      setBlockedTooltipPosition(null);
      return;
    }

    const updatePosition = () => {
      const rect = blockedTooltipTarget.anchor.getBoundingClientRect();
      const viewportPadding = 12;
      const tooltipWidth = 320;
      const gap = 10;
      const spaceAbove = rect.top - viewportPadding;
      const spaceBelow = window.innerHeight - rect.bottom - viewportPadding;
      const placement = spaceAbove >= 56 || spaceAbove >= spaceBelow ? "top" : "bottom";
      const maxLeft = Math.max(viewportPadding, window.innerWidth - tooltipWidth - viewportPadding);
      const left = Math.min(Math.max(viewportPadding, rect.right - tooltipWidth), maxLeft);

      setBlockedTooltipPosition({
        left,
        top: placement === "top" ? rect.top - gap : rect.bottom + gap,
        placement,
        message: blockedTooltipTarget.message,
      });
    };

    updatePosition();
    window.addEventListener("resize", updatePosition);
    window.addEventListener("scroll", updatePosition, true);

    return () => {
      window.removeEventListener("resize", updatePosition);
      window.removeEventListener("scroll", updatePosition, true);
    };
  }, [blockedTooltipTarget]);

  useEffect(() => {
    setBlockedTooltipTarget(null);
  }, [page, appliedFilters]);

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

  function showBlockedTooltip(event: { currentTarget: HTMLElement }, message: string) {
    setBlockedTooltipTarget({
      anchor: event.currentTarget,
      message,
    });
  }

  function hideBlockedTooltip() {
    setBlockedTooltipTarget(null);
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

      <div className="mt-4 overflow-x-auto">
        <table className="w-full min-w-[820px] text-[13px]">
          <thead>
            <tr className="border-y border-gray-100 bg-gray-50">
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
                Trạng thái
              </th>
              <th className="px-3 py-2 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Thao tác
              </th>
            </tr>
          </thead>
          <tbody>
            {loading && (
              <tr>
                <td colSpan={5} className="px-3 py-8 text-center text-gray-400">
                  Đang tải dữ liệu...
                </td>
              </tr>
            )}
            {!loading && result.items.length === 0 && (
              <tr>
                <td colSpan={5} className="px-3 py-8 text-center text-gray-400">
                  Chưa có dữ liệu phù hợp.
                </td>
              </tr>
            )}
            {!loading &&
              result.items.map((item) => {
                const blockedReason = getInvoiceBlockedReason(item.status);

                return (
                  <tr key={item.id} className="border-b border-gray-50">
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
                        {BOOKING_STATUS_OPTIONS.find((option) => option.value === item.status)?.label ??
                          item.status}
                      </span>
                    </td>
                    <td className="px-3 py-2.5">
                      <div className="flex items-center justify-end gap-2">
                        <Link
                          href={`/dashboard/bookings/${item.id}`}
                          className="rounded p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-700"
                          title="Xem chi tiết tiệc cưới"
                        >
                          <Eye className="h-4 w-4" />
                        </Link>
                        {blockedReason ? (
                          <div
                            className="relative z-10 inline-flex"
                            onMouseEnter={(event) => showBlockedTooltip(event, blockedReason)}
                            onMouseLeave={hideBlockedTooltip}
                          >
                            <span className="inline-flex cursor-not-allowed rounded p-1 text-gray-300">
                              <Plus className="h-3.5 w-3.5" />
                            </span>
                          </div>
                        ) : (
                          <Link
                            href={`/dashboard/invoices/new?bookingId=${item.id}`}
                            className="rounded p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-700"
                            title="Lập hóa đơn"
                          >
                            <Plus className="h-3.5 w-3.5" />
                          </Link>
                        )}
                      </div>
                    </td>
                  </tr>
                );
              })}
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

      {blockedTooltipPosition && typeof document !== "undefined"
        ? createPortal(
            <div
              className="pointer-events-none fixed z-[9999]"
              style={{
                left: `${blockedTooltipPosition.left}px`,
                top: `${blockedTooltipPosition.top}px`,
              }}
            >
              <div
                className={cn(
                  "max-w-[320px] rounded-lg bg-gray-900 px-3 py-2 text-[11px] leading-5 text-white shadow-2xl break-words",
                  blockedTooltipPosition.placement === "top" ? "-translate-y-full" : "translate-y-0",
                )}
              >
                {blockedTooltipPosition.message}
              </div>
            </div>,
            document.body,
          )
        : null}
    </div>
  );
}
