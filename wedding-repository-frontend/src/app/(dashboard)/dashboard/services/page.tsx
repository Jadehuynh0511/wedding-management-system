import { redirect } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchServices } from "@/features/service-catalog/lib/service-api";
import { ServiceTable } from "@/features/service-catalog/ui/service-table";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";

export default async function ServicesPage() {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/services");
  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";
  const services = await fetchServices(accessToken);

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-[20px] font-bold tracking-wide text-gray-800">QUẢN LÝ DỊCH VỤ</h1>
        <p className="mt-1 text-[13px] text-gray-500">Danh sách các dịch vụ đi kèm tiệc cưới</p>
      </div>
      <ServiceTable initialData={services} />
    </div>
  );
}