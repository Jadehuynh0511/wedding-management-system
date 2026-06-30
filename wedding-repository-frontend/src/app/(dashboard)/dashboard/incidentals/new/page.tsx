import { redirect } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";
import { fetchWeddingBookingDetail } from "@/features/booking-lookup/lib/booking-lookup-api";
import type { IncidentalReceiptFormState } from "@/features/incidental-receipt/model/incidental";
import { IncidentalReceiptForm } from "@/features/incidental-receipt/ui/incidental-receipt-form";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchServices } from "@/features/service-catalog/lib/service-api";

type NewIncidentalPageProps = {
  searchParams?: {
    bookingId?: string;
  };
};

export default async function NewIncidentalPage({ searchParams }: NewIncidentalPageProps) {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/incidentals/new");
  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  const bookingId = Number(searchParams?.bookingId ?? "");
  if (!Number.isFinite(bookingId) || bookingId <= 0) {
    redirect("/dashboard/bookings");
  }

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";

  const [bookingDetail, services] = await Promise.all([
    fetchWeddingBookingDetail(bookingId, accessToken).catch(() => null),
    fetchServices(accessToken).catch(() => [])
  ]);

  if (!bookingDetail) {
    redirect("/dashboard/bookings");
  }

  const blockedReason =
    bookingDetail.status === "DA_THANH_TOAN"
      ? "Tiệc cưới này đã được thanh toán đầy đủ nên không thể lập phiếu dịch vụ phát sinh mới."
      : bookingDetail.status === "DA_HUY"
        ? "Tiệc cưới này đã bị hủy nên không thể lập phiếu dịch vụ phát sinh."
        : null;

  if (blockedReason) {
    redirect(`/dashboard/bookings/${bookingDetail.id}`);
  }

  const activeServices = services
    .filter((s) => s.active)
    .map((s) => ({
      id: s.id,
      serviceName: s.serviceName,
      unitName: s.unitName,
      currentPrice: s.currentPrice
    }));

  if (activeServices.length === 0) {
    return (
      <div className="rounded-xl border border-amber-200 bg-amber-50 p-4">
        <h1 className="text-[18px] font-bold text-amber-700">Chưa có dịch vụ khả dụng</h1>
        <p className="mt-1 text-[13px] text-amber-700">
          Danh mục dịch vụ hiện không có mục nào đang hoạt động, nên chưa thể lập phiếu phát sinh.
        </p>
      </div>
    );
  }

  const initialState: IncidentalReceiptFormState = {
    bookingId: bookingDetail.id,
    bookingCode: `TC${String(bookingDetail.id).padStart(4, "0")}`,
    brideName: bookingDetail.brideName,
    groomName: bookingDetail.groomName,
    createdDate: new Date().toISOString().split("T")[0],
    creatorName: session.username,
    notes: "",
    lines: []
  };

  return (
    <IncidentalReceiptForm
      initialState={initialState}
      availableServices={activeServices}
    />
  );
}
