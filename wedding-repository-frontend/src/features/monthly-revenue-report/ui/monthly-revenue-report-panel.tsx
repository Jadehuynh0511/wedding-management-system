"use client";

import { FormEvent, useEffect, useMemo, useState } from "react";
import { Download, FileBarChart, Printer } from "lucide-react";

import { cn } from "@/lib/utils";
import { fetchMonthlyRevenueReport } from "@/features/monthly-revenue-report/lib/monthly-revenue-report-api";
import type {
  MonthlyRevenueReport,
  MonthlyRevenueReportItem,
} from "@/features/monthly-revenue-report/model/monthly-revenue-report";

const MONTHS = Array.from({ length: 12 }, (_, index) => index + 1);
const YEAR_SPAN = 6;

function normalizeNumber(value: number | string | null | undefined) {
  if (value === null || value === undefined) return 0;
  const parsed = typeof value === "number" ? value : Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
}

function formatCurrency(value: number) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(value);
}

function formatCompactCurrency(value: number) {
  if (value >= 1_000_000_000) return `${Math.round(value / 1_000_000_000)}B`;
  if (value >= 1_000_000) return `${Math.round(value / 1_000_000)}M`;
  if (value >= 1_000) return `${Math.round(value / 1_000)}K`;
  return String(Math.round(value));
}

function formatDate(value: string) {
  const [year, month, day] = value.split("-");
  return `${day}/${month}/${year}`;
}

function formatDayMonth(value: string) {
  const [, month, day] = value.split("-");
  return `${day}/${month}`;
}

function getCurrentMonthYear() {
  const now = new Date();
  return {
    month: now.getMonth() + 1,
    year: now.getFullYear(),
  };
}

function getYearOptions(selectedYear: number) {
  const currentYear = new Date().getFullYear();
  const startYear = Math.min(selectedYear, currentYear) - 1;
  return Array.from({ length: YEAR_SPAN }, (_, index) => startYear + index);
}

function toCsv(report: MonthlyRevenueReport) {
  const lines = [
    ["Ngay", "So luong tiec cuoi", "Doanh thu", "Ti le"].join(","),
    ...report.items.map((item) =>
      [
        item.reportDate,
        item.weddingBookingCount,
        normalizeNumber(item.revenue),
        normalizeNumber(item.revenueRatio).toFixed(2),
      ].join(","),
    ),
    ["Tong", report.totalWeddingBookings, normalizeNumber(report.totalRevenue), "100.00"].join(","),
  ];

  return lines.join("\n");
}

function normalizeReport(report: MonthlyRevenueReport): MonthlyRevenueReport {
  return {
    ...report,
    totalRevenue: normalizeNumber(report.totalRevenue),
    totalWeddingBookings: normalizeNumber(report.totalWeddingBookings),
    items: report.items.map((item) => ({
      ...item,
      weddingBookingCount: normalizeNumber(item.weddingBookingCount),
      revenue: normalizeNumber(item.revenue),
      revenueRatio: normalizeNumber(item.revenueRatio),
    })),
  };
}

function SummaryCard({
  label,
  value,
  sub,
  featured,
}: {
  label: string;
  value: string;
  sub?: string;
  featured?: boolean;
}) {
  return (
    <div
      className={cn(
        "rounded-xl border bg-white p-5",
        featured ? "border-rose-200 shadow-[inset_4px_0_0_#be7280]" : "border-gray-100",
      )}
    >
      <p className="text-[11px] font-semibold uppercase tracking-wide text-gray-400">{label}</p>
      <p className={cn("mt-2 text-[20px] font-bold", featured ? "text-rose-500" : "text-gray-800")}>
        {value}
      </p>
      {sub && <p className="mt-2 text-[12px] text-gray-400">{sub}</p>}
    </div>
  );
}

export function MonthlyRevenueReportPanel() {
  const initial = useMemo(() => getCurrentMonthYear(), []);
  const [month, setMonth] = useState(initial.month);
  const [year, setYear] = useState(initial.year);
  const [report, setReport] = useState<MonthlyRevenueReport | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const yearOptions = useMemo(() => getYearOptions(year), [year]);
  const maxRevenue = useMemo(
    () => Math.max(0, ...(report?.items.map((item) => item.revenue) ?? [])),
    [report],
  );
  const averageRevenue = report?.totalWeddingBookings
    ? report.totalRevenue / report.totalWeddingBookings
    : 0;
  const highestDay = useMemo(() => {
    if (!report?.items.length) return null;
    return report.items.reduce<MonthlyRevenueReportItem | null>((best, item) => {
      if (!best) return item;
      return item.revenue > best.revenue ? item : best;
    }, null);
  }, [report]);

  async function loadReport(nextMonth = month, nextYear = year) {
    setLoading(true);
    setError(null);

    try {
      const data = await fetchMonthlyRevenueReport(nextMonth, nextYear);
      setReport(normalizeReport(data));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Không thể tải báo cáo doanh số tháng.");
      setReport(null);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadReport(initial.month, initial.year);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  function submitReport(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    loadReport();
  }

  function printReport() {
    window.print();
  }

  function exportCsv() {
    if (!report) return;

    const blob = new Blob([`\uFEFF${toCsv(report)}`], { type: "text/csv;charset=utf-8" });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = `bao-cao-doanh-so-${report.reportMonth}-${report.reportYear}.csv`;
    link.click();
    URL.revokeObjectURL(url);
  }

  return (
    <div className="space-y-4">
      <div className="print:hidden rounded-xl border border-gray-100 bg-white p-5">
        <div className="mb-4 flex items-center justify-end gap-2">
          <button
            type="button"
            onClick={printReport}
            disabled={!report}
            className="inline-flex items-center gap-1.5 rounded-lg border border-gray-200 px-3 py-2 text-[12px] font-semibold text-gray-600 transition-colors hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
          >
            <Printer className="h-3.5 w-3.5" />
            In báo cáo
          </button>
          <button
            type="button"
            onClick={exportCsv}
            disabled={!report}
            className="inline-flex items-center gap-1.5 rounded-lg border border-gray-200 px-3 py-2 text-[12px] font-semibold text-gray-600 transition-colors hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
          >
            <Download className="h-3.5 w-3.5" />
            Xuất CSV
          </button>
        </div>

        <form onSubmit={submitReport} className="grid grid-cols-12 gap-3">
          <label className="col-span-12 md:col-span-4">
            <span className="mb-1 block text-[12px] font-semibold text-gray-500">Tháng</span>
            <select
              value={month}
              onChange={(event) => setMonth(Number(event.target.value))}
              className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2.5 text-[13px] font-semibold text-gray-700 outline-none transition-colors focus:border-rose-300 focus:bg-white"
            >
              {MONTHS.map((item) => (
                <option key={item} value={item}>
                  Tháng {item}
                </option>
              ))}
            </select>
          </label>
          <label className="col-span-12 md:col-span-4">
            <span className="mb-1 block text-[12px] font-semibold text-gray-500">Năm</span>
            <select
              value={year}
              onChange={(event) => setYear(Number(event.target.value))}
              className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2.5 text-[13px] font-semibold text-gray-700 outline-none transition-colors focus:border-rose-300 focus:bg-white"
            >
              {yearOptions.map((item) => (
                <option key={item} value={item}>
                  {item}
                </option>
              ))}
            </select>
          </label>
          <div className="col-span-12 flex items-end md:col-span-4">
            <button
              type="submit"
              disabled={loading}
              className="inline-flex w-full items-center justify-center gap-1.5 rounded-lg bg-[#be7280] px-3 py-2.5 text-[13px] font-semibold text-white transition-colors hover:bg-rose-600 disabled:cursor-wait disabled:opacity-70"
            >
              <FileBarChart className="h-4 w-4" />
              {loading ? "Đang lập báo cáo..." : "Lập báo cáo"}
            </button>
          </div>
        </form>
      </div>

      {error && (
        <div className="rounded-xl border border-rose-100 bg-rose-50 px-4 py-3 text-[13px] font-medium text-rose-700">
          {error}
        </div>
      )}

      <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
        <SummaryCard
          featured
          label={`Tổng doanh thu tháng ${report?.reportMonth ?? month}/${report?.reportYear ?? year}`}
          value={formatCurrency(report?.totalRevenue ?? 0)}
          sub={`${report?.totalWeddingBookings ?? 0} tiệc đã thanh toán`}
        />
        <SummaryCard label="Trung bình / tiệc" value={formatCurrency(averageRevenue)} />
        <SummaryCard
          label="Ngày cao nhất"
          value={
            highestDay
              ? `${formatDayMonth(highestDay.reportDate)} · ${formatCurrency(highestDay.revenue)}`
              : "—"
          }
        />
      </div>

      <section className="rounded-xl border border-gray-100 bg-white">
        <div className="border-b border-gray-100 px-5 py-4">
          <h2 className="text-[14px] font-bold uppercase tracking-wide text-gray-700">
            Doanh thu theo ngày
          </h2>
        </div>
        <div className="h-64 px-5 py-5">
          {loading && (
            <div className="flex h-full items-center justify-center text-[13px] text-gray-400">
              Đang tải dữ liệu...
            </div>
          )}
          {!loading && !report?.items.length && (
            <div className="flex h-full items-center justify-center text-[13px] text-gray-400">
              Chưa có dữ liệu phù hợp.
            </div>
          )}
          {!loading && !!report?.items.length && (
            <div className="flex h-full items-end gap-3 overflow-x-auto border-b border-gray-100 pb-3">
              {report.items.map((item) => {
                const height = maxRevenue > 0 ? Math.max(7, (item.revenue / maxRevenue) * 100) : 0;

                return (
                  <div
                    key={item.reportDate}
                    className="flex h-full min-w-16 flex-1 flex-col justify-end"
                  >
                    <div className="mb-2 text-center text-[11px] font-medium text-gray-400">
                      {formatCompactCurrency(item.revenue)}
                    </div>
                    <div
                      title={`${formatDate(item.reportDate)} · ${formatCurrency(item.revenue)}`}
                      className="mx-auto w-full max-w-14 rounded-t bg-[#be7280] transition-colors hover:bg-rose-600"
                      style={{ height: `${height}%` }}
                    />
                    <div className="mt-2 text-center text-[11px] text-gray-400">
                      {formatDayMonth(item.reportDate)}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>
      </section>

      <section className="overflow-hidden rounded-xl border border-gray-100 bg-white">
        <div className="overflow-x-auto">
          <table className="w-full min-w-[760px] text-[13px]">
            <thead>
              <tr className="border-b border-gray-100 bg-gray-50">
                <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                  Ngày
                </th>
                <th className="px-4 py-3 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                  Số lượng tiệc cưới
                </th>
                <th className="px-4 py-3 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                  Doanh thu
                </th>
                <th className="px-4 py-3 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                  Tỉ lệ
                </th>
              </tr>
            </thead>
            <tbody>
              {loading && (
                <tr>
                  <td colSpan={4} className="px-4 py-8 text-center text-gray-400">
                    Đang tải dữ liệu...
                  </td>
                </tr>
              )}
              {!loading && !report?.items.length && (
                <tr>
                  <td colSpan={4} className="px-4 py-8 text-center text-gray-400">
                    Chưa có dữ liệu phù hợp.
                  </td>
                </tr>
              )}
              {!loading &&
                report?.items.map((item) => (
                  <tr key={item.reportDate} className="border-b border-gray-50 last:border-b-0">
                    <td className="px-4 py-3 font-semibold text-gray-700">
                      {formatDate(item.reportDate)}
                    </td>
                    <td className="px-4 py-3 text-right text-gray-600">
                      {item.weddingBookingCount}
                    </td>
                    <td className="px-4 py-3 text-right font-semibold text-gray-700">
                      {formatCurrency(item.revenue)}
                    </td>
                    <td className="px-4 py-3 text-right text-gray-500">
                      {item.revenueRatio.toFixed(1)}%
                    </td>
                  </tr>
                ))}
            </tbody>
            {!!report && (
              <tfoot>
                <tr className="border-t border-gray-100 bg-gray-50">
                  <td className="px-4 py-3 text-[12px] font-bold uppercase text-gray-600">Tổng</td>
                  <td className="px-4 py-3 text-right font-bold text-gray-700">
                    {report.totalWeddingBookings}
                  </td>
                  <td className="px-4 py-3 text-right font-bold text-rose-600">
                    {formatCurrency(report.totalRevenue)}
                  </td>
                  <td className="px-4 py-3 text-right font-bold text-gray-700">
                    {report.totalRevenue > 0 ? "100.0%" : "0.0%"}
                  </td>
                </tr>
              </tfoot>
            )}
          </table>
        </div>
      </section>
    </div>
  );
}
