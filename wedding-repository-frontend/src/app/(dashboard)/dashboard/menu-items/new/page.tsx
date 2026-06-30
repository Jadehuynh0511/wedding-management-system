import { redirect } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchMenuItems } from "@/features/menu-item-catalog/lib/menu-item-api";
import { MenuItemPageForm } from "@/features/menu-item-catalog/ui/menu-item-page-form";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";

export default async function NewMenuItemPage() {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/menu-items");

  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";
  const menuItems = await fetchMenuItems(accessToken);

  return (
    <MenuItemPageForm
      mode="create"
      totalItems={menuItems.length}
    />
  );
}
