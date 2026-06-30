import { redirect } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchShifts } from "@/features/shift-catalog/lib/shift-api";
import { fetchMenuItems } from "@/features/menu-item-catalog/lib/menu-item-api";
import { fetchServices } from "@/features/service-catalog/lib/service-api";
import { fetchSystemSettings } from "@/features/system-rules/lib/system-rules-api";
import { BookingForm } from "@/features/booking-new/ui/booking-form";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";

export default async function NewBookingPage() {
  const session = await requireAuthenticatedSession();
  const createRoute = findDashboardRouteByHref("/dashboard/bookings/new");
  const canCreate = !!createRoute && canAccessRoute(session, createRoute);
  if (!canCreate) redirect("/forbidden");

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";

  const [shifts, menuItems, services, settings] = await Promise.all([
    fetchShifts(accessToken),
    fetchMenuItems(accessToken).catch(() => []),
    fetchServices(accessToken).catch(() => []),
    fetchSystemSettings(accessToken).catch(() => null)
  ]);

  const minimumDepositPct = settings?.depositRule?.minimumDepositPercentage ?? 30;

  // Chỉ lấy món ăn còn (available)
  const availableMenuItems = menuItems
    .filter((m) => m.available)
    .map((m) => ({ id: m.id, itemName: m.itemName, itemCategory: m.itemCategory, currentPrice: m.currentPrice }));

  // Chỉ lấy dịch vụ đang hoạt động
  const activeServices = services
    .filter((s) => s.active)
    .map((s) => ({ id: s.id, serviceName: s.serviceName, unitName: s.unitName, currentPrice: s.currentPrice }));

  return (
    <div>
      <BookingForm
        shifts={shifts.map((s) => ({ id: s.id, shiftName: s.shiftName, startTime: s.startTime, endTime: s.endTime }))}
        menuItems={availableMenuItems}
        services={activeServices}
        minimumDepositPct={Number(minimumDepositPct)}
        currentUserName={session.username}
      />
    </div>
  );
}
