import { redirect } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchGroups } from "@/features/user-groups/lib/user-group-api";
import { fetchStaffAccounts } from "@/features/staff-accounts/lib/staff-account-api";
import { StaffAccountTable } from "@/features/staff-accounts/ui/staff-account-table";

export default async function StaffPage() {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/staff");
  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";
  const [staffAccounts, groups] = await Promise.all([
    fetchStaffAccounts(accessToken),
    fetchGroups(accessToken),
  ]);

  return (
    <div>
      <div className="mb-6 flex items-start justify-between">
        <div>
          <h1 className="text-[20px] font-bold tracking-wide text-gray-800">
            QUẢN LÝ TÀI KHOẢN NHÂN VIÊN
          </h1>
          <p className="mt-1 text-[13px] text-gray-500">
            Danh sách và quản lý tài khoản đăng nhập của nhân viên
          </p>
        </div>
      </div>
      <StaffAccountTable staffAccounts={staffAccounts} groups={groups} currentUserId={session.id} />
    </div>
  );
}
