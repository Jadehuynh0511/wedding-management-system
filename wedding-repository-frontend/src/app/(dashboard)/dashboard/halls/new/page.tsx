import { redirect } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchHalls } from "@/features/hall-catalog/lib/hall-api";
import { fetchHallTypes } from "@/features/hall-type-catalog/lib/hall-type-api";
import { HallPageForm } from "@/features/hall-catalog/ui/hall-page-form";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";

export default async function NewHallPage() {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/halls");
  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";

  const [halls, hallTypes] = await Promise.all([
    fetchHalls(accessToken),
    fetchHallTypes(accessToken)
  ]);

  return (
    <HallPageForm
      mode="create"
      hallTypes={hallTypes}
      totalHalls={halls.length}
    />
  );
}