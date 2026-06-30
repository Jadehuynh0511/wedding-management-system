import Link from "next/link";
import { redirect } from "next/navigation";
import { Plus } from "lucide-react";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";
import { BookingLookupPanel } from "@/features/booking-lookup/ui/booking-lookup-panel";
import { fetchHalls } from "@/features/hall-catalog/lib/hall-api";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";

type BookingListPageProps = {
  searchParams?: {
    mode?: string;
  };
};

export default async function BookingListPage({ searchParams }: BookingListPageProps) {
  const session = await requireAuthenticatedSession();
  const listRoute = findDashboardRouteByHref("/dashboard/bookings");
  const createRoute = findDashboardRouteByHref("/dashboard/bookings/new");
  const canViewList = !!listRoute && canAccessRoute(session, listRoute);
  const canCreate = !!createRoute && canAccessRoute(session, createRoute);

  if (!canViewList && canCreate) {
    redirect("/dashboard/bookings/new?mode=new");
  }

  if (!canViewList && !canCreate) {
    redirect("/forbidden");
  }

  if (searchParams?.mode === "new" && canCreate) {
    redirect("/dashboard/bookings/new?mode=new");
  }

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";
  const halls = await fetchHalls(accessToken).catch(() => []);

  return (
    <div>
      <div className="mb-2 flex items-start justify-between gap-3">
        <div>
          <h1 className="text-[20px] font-bold tracking-wide text-gray-800">DANH SÁCH TIỆC CƯỚI</h1>
          <p className="mt-1 text-[13px] text-gray-500">
            Danh sách các tiệc cưới đã được tiếp nhận tại trung tâm.
          </p>
        </div>

        {canCreate && (
          <Link
            href="/dashboard/bookings/new?mode=new"
            className="inline-flex items-center gap-1.5 rounded-lg bg-rose-500 px-3 py-2 text-[12px] font-semibold text-white transition-colors hover:bg-rose-600"
          >
            <Plus className="h-3.5 w-3.5" />
            Nhận đặt tiệc
          </Link>
        )}
      </div>

      <BookingLookupPanel
        halls={halls.map((hall) => ({ id: hall.id, hallName: hall.hallName }))}
      />
    </div>
  );
}
