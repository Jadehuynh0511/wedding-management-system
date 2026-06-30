import { notFound, redirect } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import {
  fetchGroupPermissions,
  fetchPermissionCatalog,
} from "@/features/user-groups/lib/user-group-api";
import { fetchStaffAccount } from "@/features/staff-accounts/lib/staff-account-api";
import { StaffAccountDetail } from "@/features/staff-accounts/ui/staff-account-detail";

type StaffDetailPageProps = {
  params: { id: string };
};

export default async function StaffDetailPage({ params }: StaffDetailPageProps) {
  const staffId = Number(params.id);
  if (!Number.isFinite(staffId) || staffId <= 0) notFound();

  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/staff");
  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";
  const staffAccount = await fetchStaffAccount(staffId, accessToken).catch(() => null);
  if (!staffAccount) notFound();

  const [permissionCatalog, groupPermission] = await Promise.all([
    fetchPermissionCatalog(accessToken).catch(() => []),
    fetchGroupPermissions(staffAccount.groupId, accessToken).catch(() => ({
      groupId: staffAccount.groupId,
      permissionCodes: [] as string[],
    })),
  ]);

  return (
    <StaffAccountDetail
      staffAccount={staffAccount}
      permissionCatalog={permissionCatalog}
      permissionCodes={groupPermission.permissionCodes}
    />
  );
}
