import { redirect, notFound } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchGroups } from "@/features/user-groups/lib/user-group-api";
import { GroupEditForm } from "@/features/user-groups/ui/group-edit-form";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";

type EditGroupPageProps = { params: { id: string } };

export default async function EditGroupPage({ params }: EditGroupPageProps) {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/groups");
  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  const id = Number(params.id);
  if (isNaN(id) || id <= 0) notFound();

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";
  const groups = await fetchGroups(accessToken);

  const group = groups.find((g) => g.id === id);
  if (!group) notFound();

  const groupIndex = groups.findIndex((g) => g.id === id) + 1;

  return <GroupEditForm group={group} groupIndex={groupIndex} />;
}