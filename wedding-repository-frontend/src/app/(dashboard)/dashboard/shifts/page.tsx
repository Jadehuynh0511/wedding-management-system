import { redirect } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchShifts } from "@/features/shift-catalog/lib/shift-api";
import { ShiftTable } from "@/features/shift-catalog/ui/shift-table";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";

export default async function ShiftsPage() {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/shifts");

  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";
  const shifts = await fetchShifts(accessToken);

  return (
    <div>
      <div className="mb-6 flex items-start justify-between">
        <div>
          <h1 className="text-[20px] font-bold tracking-wide text-gray-800">CA TIỆC</h1>
          <p className="mt-1 text-[13px] text-gray-500">Cấu hình các ca tiệc trong ngày</p>
        </div>
      </div>
      <ShiftTable initialData={shifts} />
    </div>
  );
}
