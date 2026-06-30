import { backendRequest } from "@/shared/api/backend-client";
import type { MonthlyRevenueReport } from "@/features/monthly-revenue-report/model/monthly-revenue-report";

export async function fetchMonthlyRevenueReport(
  month: number,
  year: number,
  accessToken?: string,
): Promise<MonthlyRevenueReport> {
  const params = new URLSearchParams({
    month: String(month),
    year: String(year),
  });

  return backendRequest<MonthlyRevenueReport>(
    `/reports/monthly?${params.toString()}`,
    { accessToken },
    "Không thể tải báo cáo doanh số tháng.",
  );
}
