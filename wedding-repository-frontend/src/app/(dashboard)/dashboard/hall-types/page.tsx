import { redirect } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchHallTypes } from "@/features/hall-type-catalog/lib/hall-type-api";
import { fetchHalls } from "@/features/hall-catalog/lib/hall-api";
import { HallTypeTable } from "@/features/hall-type-catalog/ui/hall-type-table";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";

export default async function HallTypesPage() {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/hall-types");

  if (route && !canAccessRoute(session, route)) {
    redirect("/forbidden");
  }

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";

  const [hallTypes, halls] = await Promise.all([
    fetchHallTypes(accessToken),
    fetchHalls(accessToken).catch(() => []),
  ]);

  return (
    <div>
      {/* Page header */}
      <div className="mb-6 flex items-start justify-between">
        <div>
          <h1 className="text-[20px] font-bold tracking-wide text-gray-800">LOẠI SẢNH</h1>
          <p className="mt-1 text-[13px] text-gray-500">
            Quản lý các phân loại sảnh tiệc và đơn giá tối thiểu
          </p>
        </div>
      </div>

      <HallTypeTable initialData={hallTypes} halls={halls} />
    </div>
  );
}
