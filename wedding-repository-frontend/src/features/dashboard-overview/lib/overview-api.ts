import { backendRequest } from "@/shared/api/backend-client";
import type {
  DailyRevenuePoint,
  HallAvailability,
  MonthlyStats,
} from "@/features/dashboard-overview/model/overview";
import type {
  WeddingBookingPage,
  WeddingBookingSummary,
} from "@/features/booking-lookup/model/booking-lookup";
import type { MonthlyRevenueReport } from "@/features/monthly-revenue-report/model/monthly-revenue-report";

type HallResponse = { id: number; status: string };

export async function fetchHallAvailability(accessToken?: string): Promise<HallAvailability> {
  try {
    const halls = await backendRequest<HallResponse[]>(
      "/halls",
      { accessToken },
      "Không thể tải danh sách sảnh.",
    );

    return {
      total: halls.length,
      available: halls.filter((hall) => hall.status === "TRONG").length,
    };
  } catch {
    return { total: 0, available: 0 };
  }
}

export async function fetchDashboardMonthlyStats(
  accessToken?: string,
): Promise<{ stats: MonthlyStats; revenuePoints: DailyRevenuePoint[] }> {
  const today = new Date();
  const month = today.getMonth() + 1;
  const year = today.getFullYear();

  const [hallAvailability, monthlyReport, pendingPaymentCount] = await Promise.all([
    fetchHallAvailability(accessToken),
    fetchMonthlyRevenueReport(month, year, accessToken),
    fetchPendingPaymentCount(accessToken),
  ]);

  return {
    stats: {
      totalWeddingsThisMonth: monthlyReport.totalWeddingBookings,
      revenueThisMonth: monthlyReport.totalRevenue,
      pendingPaymentCount,
      hallAvailability,
    },
    revenuePoints: selectRecentRevenuePoints(monthlyReport),
  };
}

export async function fetchDashboardWeddingBookings(
  accessToken?: string,
): Promise<WeddingBookingSummary[]> {
  try {
    const params = new URLSearchParams({
      page: "0",
      size: "100",
    });

    const page = await backendRequest<WeddingBookingPage>(
      `/bookings?${params.toString()}`,
      { accessToken },
      "Không thể tải danh sách tiệc cưới.",
    );

    return [...page.items].sort((left, right) =>
      left.celebrationDate.localeCompare(right.celebrationDate),
    );
  } catch {
    return [];
  }
}

async function fetchMonthlyRevenueReport(
  month: number,
  year: number,
  accessToken?: string,
): Promise<MonthlyRevenueReport> {
  try {
    const params = new URLSearchParams({
      month: String(month),
      year: String(year),
    });

    const report = await backendRequest<MonthlyRevenueReport>(
      `/reports/monthly?${params.toString()}`,
      { accessToken },
      "Không thể tải báo cáo doanh số tháng.",
    );

    return normalizeMonthlyReport(report, month, year);
  } catch {
    return emptyMonthlyReport(month, year);
  }
}

async function fetchPendingPaymentCount(accessToken?: string) {
  const confirmedCount = await fetchBookingCountByStatus("DA_XAC_NHAN", accessToken);
  const inProgressCount = await fetchBookingCountByStatus("DANG_DIEN_RA", accessToken);

  return confirmedCount + inProgressCount;
}

async function fetchBookingCountByStatus(status: string, accessToken?: string) {
  try {
    const params = new URLSearchParams({
      page: "0",
      size: "1",
      status,
    });

    const page = await backendRequest<WeddingBookingPage>(
      `/bookings?${params.toString()}`,
      { accessToken },
      "Không thể tải danh sách tiệc cưới.",
    );

    return page.totalElements;
  } catch {
    return 0;
  }
}

function normalizeMonthlyReport(
  report: MonthlyRevenueReport,
  month: number,
  year: number,
): MonthlyRevenueReport {
  return {
    ...report,
    reportMonth: report.reportMonth || month,
    reportYear: report.reportYear || year,
    totalRevenue: normalizeNumber(report.totalRevenue),
    totalWeddingBookings: normalizeNumber(report.totalWeddingBookings),
    items: report.items.map((item) => ({
      ...item,
      revenue: normalizeNumber(item.revenue),
      weddingBookingCount: normalizeNumber(item.weddingBookingCount),
      revenueRatio: normalizeNumber(item.revenueRatio),
    })),
  };
}

function emptyMonthlyReport(month: number, year: number): MonthlyRevenueReport {
  return {
    reportMonth: month,
    reportYear: year,
    generatedAt: new Date().toISOString(),
    totalRevenue: 0,
    totalWeddingBookings: 0,
    items: [],
  };
}

function selectRecentRevenuePoints(report: MonthlyRevenueReport): DailyRevenuePoint[] {
  const today = new Date();
  const currentMonth = today.getMonth() + 1;
  const currentYear = today.getFullYear();
  const endDay =
    report.reportMonth === currentMonth && report.reportYear === currentYear
      ? today.getDate()
      : report.items.length;
  const startDay = Math.max(1, endDay - 6);

  return report.items
    .filter((item) => {
      const [, , day] = item.reportDate.split("-").map(Number);
      return day >= startDay && day <= endDay;
    })
    .slice(-7)
    .map((item) => ({
      reportDate: item.reportDate,
      revenue: normalizeNumber(item.revenue),
    }));
}

function normalizeNumber(value: number | string | null | undefined) {
  if (value === null || value === undefined) return 0;
  const parsed = typeof value === "number" ? value : Number(value);
  return Number.isFinite(parsed) ? parsed : 0;
}
