import Link from "next/link";
import { notFound, redirect } from "next/navigation";
import { ArrowLeft } from "lucide-react";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";
import { fetchInvoicePreview, fetchWeddingBookingDetail } from "@/features/booking-lookup/lib/booking-lookup-api";
import { BOOKING_STATUS_OPTIONS } from "@/features/booking-lookup/model/booking-lookup";
import { fetchIncidentalReceipts } from "@/features/incidental-receipt/lib/incidental-api";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";

type BookingDetailPageProps = {
  params: { id: string };
  searchParams?: { incidentalSaved?: string; incidentalTotal?: string; incidentalItems?: string };
};

function formatCurrency(value: number) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value ?? 0);
}

function calcBookingMenuLineTotal(lineTotalPerTable: number, tableCount: number) {
  return lineTotalPerTable * tableCount;
}

export default async function BookingDetailPage({ params, searchParams }: BookingDetailPageProps) {
  const bookingId = Number(params.id);
  if (!Number.isFinite(bookingId) || bookingId <= 0) notFound();

  const session = await requireAuthenticatedSession();
  const listRoute = findDashboardRouteByHref("/dashboard/bookings");
  const createRoute = findDashboardRouteByHref("/dashboard/bookings/new");
  const canViewList = !!listRoute && canAccessRoute(session, listRoute);
  const canCreate = !!createRoute && canAccessRoute(session, createRoute);

  if (!canViewList) {
    if (canCreate) redirect("/dashboard/bookings/new?mode=new");
    redirect("/forbidden");
  }

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";

  let detail;
  try {
    detail = await fetchWeddingBookingDetail(bookingId, accessToken);
  } catch {
    notFound();
  }

  let preview: Awaited<ReturnType<typeof fetchInvoicePreview>> | null = null;
  try {
    preview = await fetchInvoicePreview(bookingId, accessToken);
  } catch {
    preview = null;
  }

  let incidentalsTotal: number | null = null;
  if (!preview) {
    try {
      const incidentalReceipts = await fetchIncidentalReceipts(bookingId, accessToken);
      incidentalsTotal = incidentalReceipts.reduce((sum, receipt) => sum + receipt.totalAmount, 0);
    } catch {
      incidentalsTotal = null;
    }
  }

  const savedIncidentalId = Number(searchParams?.incidentalSaved ?? "");
  const showIncidentalSuccess = Number.isFinite(savedIncidentalId) && savedIncidentalId > 0;
  const savedIncidentalTotal = Number(searchParams?.incidentalTotal ?? "");
  const hasIncidentalTotal = Number.isFinite(savedIncidentalTotal) && savedIncidentalTotal >= 0;
  const savedIncidentalItemsRaw = (searchParams?.incidentalItems ?? "").trim();
  const savedIncidentalItems = savedIncidentalItemsRaw
    ? savedIncidentalItemsRaw
        .split("|")
        .map((item) => item.trim())
        .filter(Boolean)
    : [];
  const incidentalBlockedReason =
    detail.status === "DA_THANH_TOAN"
      ? "Tiệc cưới này đã được thanh toán đầy đủ nên không thể lập phiếu dịch vụ phát sinh."
      : detail.status === "DA_HUY"
        ? "Tiệc cưới này đã bị hủy nên không thể lập phiếu dịch vụ phát sinh."
        : null;
  const cancellationBlockedReason =
    detail.status === "DA_THANH_TOAN"
      ? "Tiệc cưới này đã được thanh toán đầy đủ nên không thể lập phiếu hủy."
      : detail.status === "DA_HUY"
        ? "Tiệc cưới này đã bị hủy nên không thể lập phiếu hủy mới."
        : null;

  const statusLabel =
    BOOKING_STATUS_OPTIONS.find((item) => item.value === detail.status)?.label ?? detail.status;
  const isFullyPaid = detail.status === "DA_THANH_TOAN";
  const incidentalAmount =
    preview?.incidentalsTotalAmount ?? incidentalsTotal ?? (hasIncidentalTotal ? savedIncidentalTotal : 0);
  const menuItemsTotalAmount = detail.menuItems.reduce(
    (sum, item) => sum + calcBookingMenuLineTotal(item.lineTotal, detail.tableCount),
    0,
  );
  const provisionalAmount =
    preview?.subtotalAmount ??
    detail.hallTotalAmount +
      menuItemsTotalAmount +
      detail.services.reduce((sum, item) => sum + item.lineTotal, 0) +
      incidentalAmount;
  const fallbackOutstandingAmount = provisionalAmount - detail.depositReceipt.amount;
  const outstandingAmount = isFullyPaid ? 0 : (preview?.finalAmount ?? fallbackOutstandingAmount);

  return (
    <div className="rounded-3xl bg-white/10 p-6">
      {showIncidentalSuccess && (
        <div className="mb-4 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3">
          <p className="text-[13px] font-semibold text-emerald-700">
            Đã lưu phiếu dịch vụ phát sinh thành công.
          </p>
          {hasIncidentalTotal && (
            <p className="mt-1 text-[12px] text-emerald-700">
              Tổng tiền phát sinh:{" "}
              <span className="font-semibold">{formatCurrency(savedIncidentalTotal)}</span>
            </p>
          )}
          {savedIncidentalItems.length > 0 && (
            <p className="mt-1 text-[12px] text-emerald-700">
              Dịch vụ đã thêm:{" "}
              <span className="font-semibold">{savedIncidentalItems.join(", ")}</span>
            </p>
          )}
        </div>
      )}

      <div className="mb-5 flex items-center justify-between">
        <div>
          <h1 className="text-[22px] font-extrabold tracking-wide text-rose-700">
            THÔNG TIN TIỆC CƯỚI
          </h1>
          <p className="mt-1 text-[13px] font-medium text-rose-400">
            Mã tiệc TC{String(detail.id).padStart(4, "0")}
          </p>
        </div>

        <div className="flex items-center gap-4">
          <Link
            href="/dashboard/bookings"
            className="inline-flex items-center justify-center gap-1.5 rounded-xl border border-gray-200 px-4 py-2 text-[13px] font-semibold text-gray-600 transition-colors hover:bg-gray-50 hover:text-gray-800"
          >
            <ArrowLeft className="h-4 w-4" />
            <span>Quay lại</span>
          </Link>
          {cancellationBlockedReason ? (
            <div className="group relative">
              <span className="inline-flex cursor-not-allowed items-center rounded-xl bg-gray-200 px-4 py-2 text-[12px] font-bold text-white/80 shadow-sm">
                Lập phiếu hủy
              </span>
              <div className="pointer-events-none absolute right-0 top-full z-10 mt-2 w-[320px] rounded-lg bg-gray-900 px-3 py-2 text-[11px] text-white opacity-0 shadow-lg transition-opacity group-hover:opacity-100">
                {cancellationBlockedReason}
              </div>
            </div>
          ) : (
            <Link
              href={`/dashboard/cancellations/new?bookingId=${detail.id}`}
              className="rounded-xl border border-rose-200 px-4 py-2 text-[12px] font-bold text-rose-600 shadow-sm transition hover:bg-rose-50"
            >
              Lập phiếu hủy
            </Link>
          )}
          {incidentalBlockedReason ? (
            <div className="group relative">
              <span className="inline-flex cursor-not-allowed items-center rounded-xl bg-rose-200 px-4 py-2 text-[12px] font-bold text-white/80 shadow-sm">
                Lập phiếu DV phát sinh
              </span>
              <div className="pointer-events-none absolute right-0 top-full z-10 mt-2 w-[320px] rounded-lg bg-gray-900 px-3 py-2 text-[11px] text-white opacity-0 shadow-lg transition-opacity group-hover:opacity-100">
                {incidentalBlockedReason}
              </div>
            </div>
          ) : (
            <Link
              href={`/dashboard/incidentals/new?bookingId=${detail.id}`}
              className="rounded-xl bg-rose-500 px-4 py-2 text-[12px] font-bold text-white shadow-sm transition hover:bg-rose-600"
            >
              Lập phiếu DV phát sinh
            </Link>
          )}
        </div>
      </div>

      <div className="grid grid-cols-3 gap-4">
        <div className="col-span-2 rounded-2xl border border-rose-100 bg-white/85 p-5 shadow-sm">
          <h2 className="mb-4 text-[13px] font-bold uppercase tracking-wide text-rose-700">
            Thông tin chung
          </h2>
          <div className="grid grid-cols-3 gap-4 text-[13px]">
            <Row label="Cô dâu" value={detail.brideName} />
            <Row label="Chú rể" value={detail.groomName} />
            <Row label="Số điện thoại" value={detail.bridePhoneNumber} />
            <Row label="Sảnh" value={detail.hallName} />
            <Row label="Ca" value={detail.shiftName} />
            <Row label="Ngày đãi tiệc" value={detail.celebrationDate} />
            <Row
              label="Số bàn / dự trữ"
              value={`${detail.tableCount} / ${detail.reservedTableCount}`}
            />
            <Row label="Đơn giá bàn" value={formatCurrency(detail.tablePrice)} />
            <Row label="Ghi chú" value={detail.notes || "-"} />
          </div>
        </div>

        <div className="rounded-2xl border border-rose-100 bg-white/85 p-5 shadow-sm">
          <h2 className="mb-4 text-[13px] font-bold uppercase tracking-wide text-rose-700">
            Trạng thái
          </h2>
          <div className="space-y-3 text-[13px]">
            <Row label="Tình trạng" value={statusLabel} />
            <Row label="Đã cọc" value={formatCurrency(detail.depositReceipt.amount)} />
            <Row label="Tổng tiền bàn" value={formatCurrency(detail.hallTotalAmount)} />
            <Row label="Tiền phát sinh" value={formatCurrency(incidentalAmount)} />
            <Row label="Tổng tạm tính" value={formatCurrency(provisionalAmount)} />
            <Row label="Còn lại phải thu" value={formatCurrency(outstandingAmount)} />
          </div>
        </div>
      </div>

      <div className="mt-4 grid grid-cols-2 gap-4">
        <div className="rounded-2xl border border-rose-100 bg-white/85 p-5 shadow-sm">
          <h3 className="mb-4 text-[13px] font-bold uppercase tracking-wide text-rose-700">
            Danh sách món ăn
          </h3>
          <table className="w-full overflow-hidden rounded-xl text-[13px]">
            <thead>
              <tr className="border-y border-rose-100 bg-gradient-to-r from-rose-100 via-pink-100 to-rose-50">
                <th className="px-3 py-3 text-left text-[11px] font-bold uppercase tracking-wide text-rose-700">
                  Tên món
                </th>
                <th className="px-3 py-3 text-right text-[11px] font-bold uppercase tracking-wide text-rose-700">
                  Đơn giá
                </th>
                <th className="px-3 py-3 text-right text-[11px] font-bold uppercase tracking-wide text-rose-700">
                  SL
                </th>
                <th className="px-3 py-3 text-right text-[11px] font-bold uppercase tracking-wide text-rose-700">
                  Thành tiền
                </th>
              </tr>
            </thead>
            <tbody>
              {detail.menuItems.map((item) => (
                <tr
                  key={item.id}
                  className="border-b border-rose-100 bg-white/80 transition hover:bg-rose-50"
                >
                  <td className="px-3 py-3 font-medium text-gray-700">{item.menuItemName}</td>
                  <td className="px-3 py-3 text-right text-gray-600">
                    {formatCurrency(item.priceSnapshot)}
                  </td>
                  <td className="px-3 py-3 text-right text-gray-600">{item.quantity}</td>
                  <td className="px-3 py-3 text-right font-bold text-rose-600">
                    {formatCurrency(calcBookingMenuLineTotal(item.lineTotal, detail.tableCount))}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="rounded-2xl border border-rose-100 bg-white/85 p-5 shadow-sm">
          <h3 className="mb-4 text-[13px] font-bold uppercase tracking-wide text-rose-700">
            Dịch vụ kèm theo
          </h3>
          <table className="w-full overflow-hidden rounded-xl text-[13px]">
            <thead>
              <tr className="border-y border-rose-100 bg-gradient-to-r from-rose-100 via-pink-100 to-rose-50">
                <th className="px-3 py-3 text-left text-[11px] font-bold uppercase tracking-wide text-rose-700">
                  Tên dịch vụ
                </th>
                <th className="px-3 py-3 text-right text-[11px] font-bold uppercase tracking-wide text-rose-700">
                  Đơn giá
                </th>
                <th className="px-3 py-3 text-right text-[11px] font-bold uppercase tracking-wide text-rose-700">
                  SL
                </th>
                <th className="px-3 py-3 text-right text-[11px] font-bold uppercase tracking-wide text-rose-700">
                  Thành tiền
                </th>
              </tr>
            </thead>
            <tbody>
              {detail.services.length === 0 && (
                <tr>
                  <td colSpan={4} className="px-3 py-5 text-center text-rose-300">
                    Chưa có dịch vụ đi kèm.
                  </td>
                </tr>
              )}
              {detail.services.map((item) => (
                <tr
                  key={item.id}
                  className="border-b border-rose-100 bg-white/80 transition hover:bg-rose-50"
                >
                  <td className="px-3 py-3 font-medium text-gray-700">{item.serviceName}</td>
                  <td className="px-3 py-3 text-right text-gray-600">
                    {formatCurrency(item.priceSnapshot)}
                  </td>
                  <td className="px-3 py-3 text-right text-gray-600">{item.quantity}</td>
                  <td className="px-3 py-3 text-right font-bold text-rose-600">
                    {formatCurrency(item.lineTotal)}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-xl border border-rose-100 bg-white/70 px-3 py-2">
      <p className="text-[11px] font-bold uppercase tracking-wide text-rose-500">{label}</p>
      <p className="mt-1 text-[13px] font-semibold text-gray-700">{value}</p>
    </div>
  );
}
