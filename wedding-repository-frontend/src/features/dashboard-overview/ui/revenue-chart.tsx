import type { DailyRevenuePoint } from "@/features/dashboard-overview/model/overview";

type RevenueChartProps = {
  points: DailyRevenuePoint[];
};

function formatCompactCurrency(value: number) {
  if (value >= 1_000_000_000) return `${Math.round(value / 1_000_000_000)}B`;
  if (value >= 1_000_000) return `${Math.round(value / 1_000_000)}M`;
  if (value >= 1_000) return `${Math.round(value / 1_000)}K`;
  return String(Math.round(value));
}

function formatDayMonth(value: string) {
  const [, month, day] = value.split("-");
  return `${day}/${month}`;
}

export function RevenueChart({ points }: RevenueChartProps) {
  const maxRevenue = Math.max(0, ...points.map((point) => point.revenue));
  const averageRevenue =
    points.length > 0
      ? points.reduce((total, point) => total + point.revenue, 0) / points.length
      : 0;

  return (
    <div className="rounded-xl border border-gray-100 bg-white p-5">
      <h2 className="mb-4 text-[13px] font-bold uppercase tracking-wide text-gray-700">
        Doanh thu 7 ngày
      </h2>

      <div className="flex h-32 items-end justify-between gap-1 px-1">
        {points.length === 0 &&
          Array.from({ length: 7 }, (_, index) => (
            <div key={index} className="flex flex-1 flex-col items-center gap-1">
              <div className="w-full rounded-t bg-gray-100" style={{ height: "12%" }} />
              <span className="text-[10px] text-gray-300">—</span>
            </div>
          ))}

        {points.map((point) => {
          const height = maxRevenue > 0 ? Math.max(10, (point.revenue / maxRevenue) * 100) : 8;

          return (
            <div key={point.reportDate} className="flex flex-1 flex-col items-center gap-1">
              <span className="text-[10px] text-gray-300">
                {point.revenue > 0 ? formatCompactCurrency(point.revenue) : "—"}
              </span>
              <div
                className="w-full rounded-t bg-rose-300"
                style={{ height: `${height}%` }}
                title={`${formatDayMonth(point.reportDate)} · ${formatCompactCurrency(point.revenue)}`}
              />
              <span className="text-[10px] text-gray-400">{formatDayMonth(point.reportDate)}</span>
            </div>
          );
        })}
      </div>

      <div className="mt-3 flex items-center justify-between border-t border-gray-100 pt-3">
        <span className="text-[11px] text-gray-400">Trung bình</span>
        <span className="text-[12px] font-semibold text-gray-500">
          {formatCompactCurrency(averageRevenue)}
        </span>
      </div>

      <div className="mt-2 flex items-center justify-between text-[11px] text-gray-300">
        <span>Trong tháng hiện tại</span>
        <span>{points.length} ngày</span>
      </div>
    </div>
  );
}
