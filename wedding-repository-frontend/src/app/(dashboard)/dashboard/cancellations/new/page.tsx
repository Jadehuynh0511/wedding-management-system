import Link from "next/link";
import { redirect } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";
import { fetchWeddingBookingDetail } from "@/features/booking-lookup/lib/booking-lookup-api";
import { CancellationReceiptForm } from "@/features/cancellation-receipt/ui/cancellation-receipt-form";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchSystemSettings } from "@/features/system-rules/lib/system-rules-api";

type NewCancellationPageProps = {
  searchParams?: {
    bookingId?: string;
  };
};

export default async function NewCancellationPage({ searchParams }: NewCancellationPageProps) {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/cancellations/new");
  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  const bookingId = Number(searchParams?.bookingId ?? "");
  if (!Number.isFinite(bookingId) || bookingId <= 0) {
    return (
      <div className="rounded-xl border border-amber-200 bg-amber-50 p-4">
        <h1 className="text-[18px] font-bold text-amber-700">Chưa chọn tiệc cưới</h1>
        <p className="mt-1 text-[13px] text-amber-700">
          Vui lòng mở trang tra cứu tiệc cưới, chọn chi tiết tiệc cần hủy rồi lập phiếu hủy.
        </p>
        <Link
          href="/dashboard/bookings"
          className="mt-3 inline-flex rounded-lg bg-rose-500 px-3 py-2 text-[12px] font-semibold text-white hover:bg-rose-600"
        >
          Đến tra cứu tiệc cưới
        </Link>
      </div>
    );
  }

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";

  const [bookingDetail, settings] = await Promise.all([
    fetchWeddingBookingDetail(bookingId, accessToken).catch(() => null),
    fetchSystemSettings(accessToken).catch(() => null),
  ]);

  if (!bookingDetail) redirect("/dashboard/bookings");

  if (bookingDetail.status === "DA_THANH_TOAN" || bookingDetail.status === "DA_HUY") {
    redirect(`/dashboard/bookings/${bookingDetail.id}`);
  }

  if (!settings) {
    return (
      <div className="rounded-xl border border-rose-200 bg-rose-50 p-4">
        <h1 className="text-[18px] font-bold text-rose-700">Không thể lập phiếu hủy tiệc</h1>
        <p className="mt-1 text-[13px] text-rose-600">
          Không tải được quy định hủy tiệc QĐ12 nên chưa thể tính trước số tiền hoàn và giữ lại.
          Vui lòng kiểm tra quyền đọc cấu hình hệ thống hoặc thử lại sau.
        </p>
      </div>
    );
  }

  return (
    <CancellationReceiptForm
      bookingDetail={bookingDetail}
      settings={settings}
    />
  );
}
