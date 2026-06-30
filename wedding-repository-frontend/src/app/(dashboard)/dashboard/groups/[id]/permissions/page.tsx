import { redirect, notFound } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchGroups, fetchGroupPermissions, fetchPermissionCatalog } from "@/features/user-groups/lib/user-group-api";
import { GroupPermissionsForm } from "@/features/user-groups/ui/group-permissions-form";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";

type GroupPermissionsPageProps = { params: { id: string } };

export default async function GroupPermissionsPage({ params }: GroupPermissionsPageProps) {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/groups");
  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  const id = Number(params.id);
  if (isNaN(id) || id <= 0) notFound();

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";

  const [groups, groupPermission, permissionCatalog] = await Promise.all([
    fetchGroups(accessToken),
    fetchGroupPermissions(id, accessToken).catch(() => ({ groupId: id, permissionCodes: [] as string[] })),
    fetchPermissionCatalog(accessToken)
  ]);

  const group = groups.find((g) => g.id === id);
  if (!group) notFound();

  const groupIndex = groups.findIndex((g) => g.id === id) + 1;
  const isAdmin = session.groupName === "ADMIN";

  return (
    <GroupPermissionsForm
      group={group}
      groupIndex={groupIndex}
      initialGrantedCodes={Array.from(groupPermission.permissionCodes)}
      permissionCatalog={permissionCatalog}
      isAdmin={isAdmin}
    />
  );
}