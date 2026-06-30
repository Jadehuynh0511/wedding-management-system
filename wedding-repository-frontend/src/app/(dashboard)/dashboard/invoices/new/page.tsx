import { redirect } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";
import { fetchWeddingBookingDetail } from "@/features/booking-lookup/lib/booking-lookup-api";
import { fetchHalls } from "@/features/hall-catalog/lib/hall-api";
import { fetchIncidentalReceipts } from "@/features/incidental-receipt/lib/incidental-api";
import { fetchInvoicePreview } from "@/features/invoice/lib/invoice-api";
import { InvoiceCreateForm } from "@/features/invoice/ui/invoice-create-form";
import { InvoiceCandidateListPanel } from "@/features/invoice/ui/invoice-candidate-list-panel";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";

type InvoiceNewPageProps = {
  searchParams?: {
    bookingId?: string;
  };
};

export default async function InvoiceNewPage({ searchParams }: InvoiceNewPageProps) {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/invoices/new");
  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";

  const bookingId = Number(searchParams?.bookingId ?? "");
  if (!Number.isFinite(bookingId) || bookingId <= 0) {
    const halls = await fetchHalls(accessToken).catch(() => []);
    return (
      <div>
        <div className="mb-2">
          <h1 className="text-[20px] font-bold tracking-wide text-gray-800">LẬP HÓA ĐƠN</h1>
          <p className="mt-1 text-[13px] text-gray-500">Chọn tiệc cưới để lập hóa đơn thanh toán.</p>
        </div>
        <InvoiceCandidateListPanel
          halls={halls.map((hall) => ({ id: hall.id, hallName: hall.hallName }))}
        />
      </div>
    );
  }

  const [bookingDetail, preview, incidentalReceipts] = await Promise.all([
    fetchWeddingBookingDetail(bookingId, accessToken).catch(() => null),
    fetchInvoicePreview(bookingId, accessToken).catch(() => null),
    fetchIncidentalReceipts(bookingId, accessToken).catch(() => []),
  ]);

  if (!bookingDetail) redirect(`/dashboard/bookings/${bookingId}`);
  if (bookingDetail.status === "DA_THANH_TOAN" || bookingDetail.status === "DA_HUY") {
    redirect(`/dashboard/bookings/${bookingId}`);
  }
  if (!preview) {
    return (
      <div className="rounded-xl border border-rose-200 bg-rose-50 p-4">
        <h1 className="text-[18px] font-bold text-rose-700">Không thể lập hóa đơn cho tiệc này</h1>
        <p className="mt-1 text-[13px] text-rose-600">
          Không tải được dữ liệu tổng kết hóa đơn (invoice preview). Vui lòng kiểm tra quyền
          `INVOICE_CREATE` hoặc dữ liệu nghiệp vụ của tiệc rồi thử lại.
        </p>
      </div>
    );
  }

  return (
    <InvoiceCreateForm
      bookingDetail={bookingDetail}
      preview={preview}
      incidentalReceipts={incidentalReceipts}
    />
  );
}
