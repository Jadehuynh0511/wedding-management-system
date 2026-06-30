import { redirect } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchHallTypes } from "@/features/hall-type-catalog/lib/hall-type-api";
import { HallTypePageForm } from "@/features/hall-type-catalog/ui/hall-type-page-form";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";

export default async function NewHallTypePage() {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/hall-types");

  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";
  const hallTypes = await fetchHallTypes(accessToken);

  return (
    <HallTypePageForm
      mode="create"
      totalHallTypes={hallTypes.length}
    />
  );
}
