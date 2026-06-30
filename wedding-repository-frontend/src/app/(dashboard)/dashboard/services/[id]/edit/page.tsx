import { redirect, notFound } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchService, fetchServices } from "@/features/service-catalog/lib/service-api";
import { ServicePageForm } from "@/features/service-catalog/ui/service-page-form";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";

type EditServicePageProps = { params: { id: string } };

export default async function EditServicePage({ params }: EditServicePageProps) {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/services");
  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  const id = Number(params.id);
  if (isNaN(id) || id <= 0) notFound();

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";

  const [service, services] = await Promise.all([
    fetchService(id, accessToken).catch(() => null),
    fetchServices(accessToken)
  ]);

  if (!service) notFound();

  return (
    <ServicePageForm
      mode="edit"
      initial={service}
      totalServices={services.length}
    />
  );
}