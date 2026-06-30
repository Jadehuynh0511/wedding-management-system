import { Building2, Calendar, Clock, DollarSign } from "lucide-react";

import type { MonthlyStats } from "@/features/dashboard-overview/model/overview";

type StatCardsProps = {
  stats: MonthlyStats;
};

function formatCurrency(value: number) {
  return new Intl.NumberFormat("vi-VN", {
    style: "currency",
    currency: "VND",
    maximumFractionDigits: 0,
  }).format(value);
}

export function StatCards({ stats }: StatCardsProps) {
  const { hallAvailability } = stats;

  return (
    <div className="grid grid-cols-4 gap-4">
      <StatCard
        icon={<Calendar className="h-4 w-4 text-gray-400" />}
        label="TỔNG SỐ TIỆC TRONG THÁNG"
        value={String(stats.totalWeddingsThisMonth)}
        sub={<span className="text-[11px] text-gray-400">Tiệc đã thanh toán</span>}
      />

      <StatCard
        icon={<DollarSign className="h-4 w-4 text-gray-400" />}
        label="DOANH THU THÁNG"
        value={formatCurrency(stats.revenueThisMonth)}
        sub={<span className="text-[11px] text-gray-400">Từ báo cáo doanh số</span>}
      />

      <StatCard
        icon={<Building2 className="h-4 w-4 text-gray-400" />}
        label="SẢNH ĐANG TRỐNG"
        value={
          hallAvailability.total > 0
            ? `${hallAvailability.available} / ${hallAvailability.total}`
            : "—"
        }
        sub={
          hallAvailability.total > 0 ? (
            <span className="text-[11px] text-gray-400">
              {hallAvailability.total - hallAvailability.available} sảnh đang sử dụng
            </span>
          ) : null
        }
      />

      <StatCard
        icon={<Clock className="h-4 w-4 text-gray-400" />}
        label="TIỆC CHỜ THANH TOÁN"
        value={String(stats.pendingPaymentCount)}
        sub={<span className="text-[11px] text-gray-400">Đã xác nhận hoặc đang diễn ra</span>}
      />
    </div>
  );
}

function StatCard({
  icon,
  label,
  value,
  sub,
}: {
  icon: React.ReactNode;
  label: string;
  value: string;
  sub?: React.ReactNode;
}) {
  return (
    <div className="rounded-xl border border-gray-100 bg-white p-5">
      <div className="mb-3 flex items-center justify-between">
        <p className="text-[10px] font-semibold uppercase tracking-wide text-gray-400">{label}</p>
        {icon}
      </div>
      <p className="text-[28px] font-bold text-gray-800">{value}</p>
      {sub && <div className="mt-1">{sub}</div>}
    </div>
  );
}
