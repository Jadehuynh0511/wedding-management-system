import { redirect, notFound } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchShift, fetchShifts } from "@/features/shift-catalog/lib/shift-api";
import { ShiftForm } from "@/features/shift-catalog/ui/shift-form";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";

type EditShiftPageProps = {
  params: { id: string };
};

export default async function EditShiftPage({ params }: EditShiftPageProps) {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/shifts");

  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  const id = Number(params.id);
  if (isNaN(id) || id <= 0) notFound();

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";

  const [shift, existingShifts] = await Promise.all([
    fetchShift(id, accessToken).catch(() => null),
    fetchShifts(accessToken)
  ]);

  if (!shift) notFound();

  return (
    <ShiftForm
      mode="edit"
      initial={shift}
      existingShifts={existingShifts}
    />
  );
}
