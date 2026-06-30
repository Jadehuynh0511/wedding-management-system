import { redirect } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchMenuItems } from "@/features/menu-item-catalog/lib/menu-item-api";
import { MenuItemTable } from "@/features/menu-item-catalog/ui/menu-item-table";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";

export default async function MenuItemsPage() {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/menu-items");

  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";
  const menuItems = await fetchMenuItems(accessToken);

  return (
    <div>
      <div className="mb-6 flex items-start justify-between">
        <div>
          <h1 className="text-[20px] font-bold tracking-wide text-gray-800">QUẢN LÝ MÓN ĂN</h1>
          <p className="mt-1 text-[13px] text-gray-500">Danh sách thực đơn món ăn của trung tâm tiệc cưới</p>
        </div>
      </div>
      <MenuItemTable initialData={menuItems} />
    </div>
  );
}
