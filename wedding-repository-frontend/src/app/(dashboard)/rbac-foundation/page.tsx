import { redirect } from "next/navigation";

import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import {
  fetchGroupPermissions,
  fetchGroups,
  fetchPermissionCatalog,
} from "@/features/user-groups/lib/user-group-api";
import { PermissionManagementPanel } from "@/features/user-groups/ui/permission-management-panel";

export default async function RbacFoundationPage() {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/rbac-foundation");

  if (route && !canAccessRoute(session, route)) {
    redirect("/forbidden");
  }

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";
  const [groups, permissionCatalog] = await Promise.all([
    fetchGroups(accessToken).catch(() => []),
    fetchPermissionCatalog(accessToken).catch(() => []),
  ]);
  const initialGroup = groups[0] ?? null;
  const initialGroupPermissions = initialGroup
    ? await fetchGroupPermissions(initialGroup.id, accessToken).catch(() => ({
        groupId: initialGroup.id,
        permissionCodes: [] as string[],
      }))
    : null;

  return (
    <PermissionManagementPanel
      groups={groups}
      permissionCatalog={permissionCatalog}
      initialGroupId={initialGroup?.id ?? null}
      initialGrantedCodes={initialGroupPermissions?.permissionCodes ?? []}
    />
  );
}
