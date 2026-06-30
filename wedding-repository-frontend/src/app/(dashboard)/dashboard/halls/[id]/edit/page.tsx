import { redirect, notFound } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchHall, fetchHalls } from "@/features/hall-catalog/lib/hall-api";
import { fetchHallTypes } from "@/features/hall-type-catalog/lib/hall-type-api";
import { HallPageForm } from "@/features/hall-catalog/ui/hall-page-form";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";

type EditHallPageProps = { params: { id: string } };

export default async function EditHallPage({ params }: EditHallPageProps) {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/halls");
  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  const id = Number(params.id);
  if (isNaN(id) || id <= 0) notFound();

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";

  const [hall, halls, hallTypes] = await Promise.all([
    fetchHall(id, accessToken).catch(() => null),
    fetchHalls(accessToken),
    fetchHallTypes(accessToken)
  ]);

  if (!hall) notFound();

  return (
    <HallPageForm
      mode="edit"
      initial={hall}
      hallTypes={hallTypes}
      totalHalls={halls.length}
    />
  );
}