import { redirect } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchGroups } from "@/features/user-groups/lib/user-group-api";
import { GroupTable } from "@/features/user-groups/ui/group-table";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";

export default async function GroupsPage() {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/groups");
  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";
  const groups = await fetchGroups(accessToken);

  return (
    <div>
      <div className="mb-6 flex items-start justify-between">
        <div>
          <h1 className="text-[20px] font-bold tracking-wide text-gray-800">QUẢN LÝ NHÓM NGƯỜI DÙNG</h1>
          <p className="mt-1 text-[13px] text-gray-500">Quản lý các nhóm phân quyền trong hệ thống</p>
        </div>
      </div>
      <GroupTable groups={groups} />
    </div>
  );
}