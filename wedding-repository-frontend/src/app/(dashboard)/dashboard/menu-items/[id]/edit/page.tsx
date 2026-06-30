import { redirect, notFound } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchMenuItem, fetchMenuItems } from "@/features/menu-item-catalog/lib/menu-item-api";
import { MenuItemPageForm } from "@/features/menu-item-catalog/ui/menu-item-page-form";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";

type EditMenuItemPageProps = {
  params: { id: string };
};

export default async function EditMenuItemPage({ params }: EditMenuItemPageProps) {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/menu-items");

  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  const id = Number(params.id);
  if (isNaN(id) || id <= 0) notFound();

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";

  const [menuItem, menuItems] = await Promise.all([
    fetchMenuItem(id, accessToken).catch(() => null),
    fetchMenuItems(accessToken)
  ]);

  if (!menuItem) notFound();

  return (
    <MenuItemPageForm
      mode="edit"
      initial={menuItem}
      totalItems={menuItems.length}
    />
  );
}
