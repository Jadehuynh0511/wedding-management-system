import Link from "next/link";
import { redirect } from "next/navigation";
import { Plus } from "lucide-react";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchHalls } from "@/features/hall-catalog/lib/hall-api";
import { InvoiceListPanel } from "@/features/invoice/ui/invoice-list-panel";

export default async function InvoiceListPage() {
  const session = await requireAuthenticatedSession();
  const listRoute = findDashboardRouteByHref("/dashboard/invoices");
  const createRoute = findDashboardRouteByHref("/dashboard/invoices/new");
  const canViewList = !!listRoute && canAccessRoute(session, listRoute);
  const canCreate = !!createRoute && canAccessRoute(session, createRoute);

  if (!canViewList && canCreate) redirect("/dashboard/invoices/new");
  if (!canViewList && !canCreate) redirect("/forbidden");

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";
  const halls = await fetchHalls(accessToken).catch(() => []);

  return (
    <div>
      <div className="mb-2 flex items-start justify-between gap-3">
        <div>
          <h1 className="mb-6 text-[20px] font-bold tracking-wide text-gray-800">
            DANH SÁCH HÓA ĐƠN
          </h1>
          <p className="-mt-5 mb-6 text-[13px] text-gray-500">
            Danh sách hóa đơn thanh toán đã được lập trong hệ thống.
          </p>
        </div>
        <div className="flex items-center gap-2">
          {canCreate && (
            <Link
              href="/dashboard/invoices/new"
              className="inline-flex items-center gap-1.5 rounded-lg bg-rose-500 px-3 py-2 text-[12px] font-semibold text-white transition-colors hover:bg-rose-600"
            >
              <Plus className="h-3.5 w-3.5" />
              Lập hóa đơn
            </Link>
          )}
        </div>
      </div>

      <InvoiceListPanel
        halls={halls.map((hall) => ({ id: hall.id, hallName: hall.hallName }))}
      />
    </div>
  );
}
