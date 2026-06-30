import { redirect } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchServices } from "@/features/service-catalog/lib/service-api";
import { ServicePageForm } from "@/features/service-catalog/ui/service-page-form";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";

export default async function NewServicePage() {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/services");
  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";
  const services = await fetchServices(accessToken);

  return (
    <ServicePageForm
      mode="create"
      totalServices={services.length}
    />
  );
}