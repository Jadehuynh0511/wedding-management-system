import { redirect } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";
import { fetchHalls } from "@/features/hall-catalog/lib/hall-api";
import { IncidentalListPanel } from "@/features/incidental-receipt/ui/incidental-list-panel";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";

export default async function IncidentalListPage() {
  const session = await requireAuthenticatedSession();
  const listRoute = findDashboardRouteByHref("/dashboard/incidentals");
  const createRoute = findDashboardRouteByHref("/dashboard/incidentals/new");
  const canViewList = !!listRoute && canAccessRoute(session, listRoute);
  const canCreate = !!createRoute && canAccessRoute(session, createRoute);

  if (!canViewList && canCreate) redirect("/dashboard/incidentals/new");
  if (!canViewList && !canCreate) redirect("/forbidden");

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";
  const halls = await fetchHalls(accessToken).catch(() => []);

  return (
    <div>
      <div className="mb-2 flex items-start justify-between gap-3">
        <div>
          <h1 className="text-[20px] font-bold tracking-wide text-gray-800">DỊCH VỤ PHÁT SINH</h1>
          <p className="mt-1 text-[13px] text-gray-500">
            Phiếu ghi nhận các dịch vụ phát sinh trong ngày tiệc
          </p>
        </div>
      </div>

      <IncidentalListPanel
        halls={halls.map((hall) => ({ id: hall.id, hallName: hall.hallName }))}
      />
    </div>
  );
}
