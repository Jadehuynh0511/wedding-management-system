import { redirect } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { MonthlyRevenueReportPanel } from "@/features/monthly-revenue-report/ui/monthly-revenue-report-panel";

export default async function MonthlyRevenueReportPage() {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/reports/monthly");

  if (!route || !canAccessRoute(session, route)) redirect("/forbidden");

  return (
    <div className="space-y-4">
      <div className="flex items-start justify-between gap-3">
        <div>
          <h1 className="text-[20px] font-bold tracking-wide text-gray-800">
            BÁO CÁO DOANH SỐ THÁNG
          </h1>
          <p className="mt-1 text-[13px] text-gray-500">
            Thống kê doanh thu theo từng ngày trong tháng
          </p>
        </div>
      </div>

      <MonthlyRevenueReportPanel />
    </div>
  );
}
