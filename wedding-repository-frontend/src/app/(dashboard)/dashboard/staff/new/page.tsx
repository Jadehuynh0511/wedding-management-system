import { redirect } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchGroups } from "@/features/user-groups/lib/user-group-api";
import { StaffAccountForm } from "@/features/staff-accounts/ui/staff-account-form";

export default async function NewStaffPage() {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/staff");
  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";
  const groups = await fetchGroups(accessToken);

  return <StaffAccountForm mode="create" groups={groups} />;
}
