import { notFound, redirect } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchGroups } from "@/features/user-groups/lib/user-group-api";
import { fetchStaffAccount } from "@/features/staff-accounts/lib/staff-account-api";
import { StaffAccountForm } from "@/features/staff-accounts/ui/staff-account-form";

type EditStaffPageProps = {
  params: { id: string };
};

export default async function EditStaffPage({ params }: EditStaffPageProps) {
  const staffId = Number(params.id);
  if (!Number.isFinite(staffId) || staffId <= 0) notFound();

  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/staff");
  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";
  const [staffAccount, groups] = await Promise.all([
    fetchStaffAccount(staffId, accessToken).catch(() => null),
    fetchGroups(accessToken),
  ]);

  if (!staffAccount) notFound();

  return <StaffAccountForm mode="edit" staffAccount={staffAccount} groups={groups} />;
}
