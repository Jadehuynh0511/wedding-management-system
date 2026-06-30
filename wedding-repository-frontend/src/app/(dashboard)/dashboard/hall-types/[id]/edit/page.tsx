import { redirect, notFound } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchHallType, fetchHallTypes, fetchHallsByType } from "@/features/hall-type-catalog/lib/hall-type-api";
import { HallTypePageForm } from "@/features/hall-type-catalog/ui/hall-type-page-form";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";

type EditHallTypePageProps = {
  params: { id: string };
};

export default async function EditHallTypePage({ params }: EditHallTypePageProps) {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/hall-types");

  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  const id = Number(params.id);
  if (isNaN(id) || id <= 0) notFound();

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";

  const [hallType, hallTypes, hallsUsingType] = await Promise.all([
    fetchHallType(id, accessToken).catch(() => null),
    fetchHallTypes(accessToken),
    fetchHallsByType(id, accessToken).catch(() => [])
  ]);

  if (!hallType) notFound();

  return (
    <HallTypePageForm
      mode="edit"
      initial={hallType}
      hallsUsingType={hallsUsingType}
      totalHallTypes={hallTypes.length}
    />
  );
}
