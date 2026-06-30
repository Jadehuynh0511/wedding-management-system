import { redirect, notFound } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchHall } from "@/features/hall-catalog/lib/hall-api";
import { HallDetail } from "@/features/hall-catalog/ui/hall-detail";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";

type HallDetailPageProps = { params: { id: string } };

export default async function HallDetailPage({ params }: HallDetailPageProps) {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/halls");
  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  const id = Number(params.id);
  if (isNaN(id) || id <= 0) notFound();

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";
  const hall = await fetchHall(id, accessToken).catch(() => null);
  if (!hall) notFound();

  return <HallDetail hall={hall} />;
}