
import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import {
  fetchDashboardMonthlyStats,
  fetchDashboardWeddingBookings,
} from "@/features/dashboard-overview/lib/overview-api";
import { StatCards } from "@/features/dashboard-overview/ui/stat-cards";
import { HallSchedule } from "@/features/dashboard-overview/ui/hall-schedule";
import { RevenueChart } from "@/features/dashboard-overview/ui/revenue-chart";
import { UpcomingWeddings } from "@/features/dashboard-overview/ui/upcoming-weddings";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";

export default async function DashboardHomePage() {
  await requireAuthenticatedSession();

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";

  const [{ stats, revenuePoints }, weddingBookings] = await Promise.all([
    fetchDashboardMonthlyStats(accessToken),
    fetchDashboardWeddingBookings(accessToken),
  ]);

  return (
    <div className="space-y-5">
      {/* Header */}
      <div>
        <h1 className="text-[20px] font-bold tracking-wide text-gray-800">TỔNG QUAN</h1>
        <p className="mt-1 text-[13px] text-gray-500">
          Theo dõi hoạt động kinh doanh và lịch tiệc trong tháng
        </p>
      </div>

      {/* Stat cards */}
      <StatCards stats={stats} />

      {/* Lịch sảnh + Doanh thu */}
      <div className="grid grid-cols-3 gap-5">
        <div className="col-span-2">
          <HallSchedule bookings={weddingBookings} />
        </div>
        <div>
          <RevenueChart points={revenuePoints} />
        </div>
      </div>

      {/* Tiệc cưới sắp diễn ra */}
      <UpcomingWeddings bookings={weddingBookings} />
    </div>
  );
}
