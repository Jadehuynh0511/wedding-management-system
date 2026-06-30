import { redirect } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchHalls } from "@/features/hall-catalog/lib/hall-api";
import { fetchHallTypes } from "@/features/hall-type-catalog/lib/hall-type-api";
import { HallTable } from "@/features/hall-catalog/ui/hall-table";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";

export default async function HallsPage() {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/halls");
  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";

  const [halls, hallTypes] = await Promise.all([
    fetchHalls(accessToken),
    fetchHallTypes(accessToken)
  ]);

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-[20px] font-bold tracking-wide text-gray-800">QUẢN LÝ SẢNH</h1>
        <p className="mt-1 text-[13px] text-gray-500">Danh sách các sảnh tiệc đang được quản lý trong hệ thống</p>
      </div>
      <HallTable initialData={halls} hallTypes={hallTypes} />
    </div>
  );
}