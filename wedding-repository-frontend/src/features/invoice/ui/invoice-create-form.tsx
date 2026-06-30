"use client";

import { useMemo, useState } from "react";
import { ArrowLeft, Printer, Save } from "lucide-react";

import { cn } from "@/lib/utils";
import { useAuth } from "@/features/auth/ui/auth-provider";
import type { WeddingBookingDetail } from "@/features/booking-lookup/model/booking-lookup";
import type { IncidentalReceiptResponse } from "@/features/incidental-receipt/lib/incidental-api";
import type { InvoicePreview } from "@/features/invoice/lib/invoice-api";
import { createInvoice } from "@/features/invoice/lib/invoice-api";
import { hasPermission } from "@/features/rbac/lib/authorization";

type InvoiceCreateFormProps = {
  bookingDetail: WeddingBookingDetail;
  preview: InvoicePreview;
  incidentalReceipts: IncidentalReceiptResponse[];
};

function formatCurrency(value: number) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value);
}

export function InvoiceCreateForm({
  bookingDetail,
  preview,
  incidentalReceipts,
}: InvoiceCreateFormProps) {
  const { session } = useAuth();
  const [notes, setNotes] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);

  const incidentalItems = useMemo(
    () => incidentalReceipts.flatMap((receipt) => receipt.items),
    [incidentalReceipts],
  );

  async function handleCreateInvoice() {
    setSubmitting(true);
    setSubmitError(null);
    try {
      const invoice = await createInvoice(bookingDetail.id, notes);
      const nextPath =
        session && hasPermission(session.permissionCodes, "INVOICE_VIEW")
          ? `/dashboard/invoices/${invoice.id}`
          : "/dashboard/invoices/new";
      window.location.assign(nextPath);
    } catch (error) {
      setSubmitError(error instanceof Error ? error.message : "Không thể lập hóa đơn.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <div className="mb-4 flex items-start justify-between">
        <div>
          <h1 className="text-[20px] font-bold tracking-wide text-gray-800">
            LẬP HÓA ĐƠN THANH TOÁN
          </h1>
          <p className="mt-1 text-[13px] text-gray-500">
            Hóa đơn cuối cho tiệc cưới TC{String(bookingDetail.id).padStart(4, "0")}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => window.location.assign(`/dashboard/bookings/${bookingDetail.id}`)}
            className="rounded-lg border border-gray-200 px-3 py-2 text-[12px] font-semibold text-gray-600 hover:bg-gray-50"
          >
            <span className="inline-flex items-center gap-1.5">
              <ArrowLeft className="h-3.5 w-3.5" />
              Hủy
            </span>
          </button>
          <button
            type="button"
            onClick={() => window.print()}
            className="rounded-lg border border-gray-200 px-3 py-2 text-[12px] font-semibold text-gray-600 hover:bg-gray-50"
          >
            <span className="inline-flex items-center gap-1.5">
              <Printer className="h-3.5 w-3.5" />
              In hóa đơn
            </span>
          </button>
          <button
            type="button"
            onClick={handleCreateInvoice}
            disabled={submitting}
            className={cn(
              "rounded-lg px-3 py-2 text-[12px] font-semibold text-white",
              submitting ? "cursor-not-allowed bg-rose-300" : "bg-rose-500 hover:bg-rose-600",
            )}
          >
            <span className="inline-flex items-center gap-1.5">
              <Save className="h-3.5 w-3.5" />
              {submitting ? "Đang lưu..." : "Lưu hóa đơn"}
            </span>
          </button>
        </div>
      </div>

      <div className="grid grid-cols-12 gap-4">
        <div className="col-span-8 space-y-4">
          <div className="rounded-xl border border-gray-100 bg-white p-4">
            <h2 className="mb-3 text-[13px] font-bold uppercase tracking-wide text-gray-700">
              A. Thông tin tiệc
            </h2>
            <div className="grid grid-cols-3 gap-3 text-[13px]">
              <Info label="Mã tiệc" value={`TC${String(bookingDetail.id).padStart(4, "0")}`} />
              <Info
                label="Cô dâu - chú rể"
                value={`${bookingDetail.brideName} - ${bookingDetail.groomName}`}
              />
              <Info label="SĐT" value={bookingDetail.bridePhoneNumber} />
              <Info label="Sảnh" value={bookingDetail.hallName} />
              <Info label="Ngày đãi" value={bookingDetail.celebrationDate} />
              <Info label="Ca" value={bookingDetail.shiftName} />
            </div>
          </div>

          <div className="rounded-xl border border-gray-100 bg-white p-4">
            <h2 className="mb-3 text-[13px] font-bold uppercase tracking-wide text-gray-700">
              B. Chi tiết khoản mục
            </h2>
            <table className="w-full text-[13px]">
              <thead>
                <tr className="border-y border-gray-100 bg-gray-50">
                  <th className="px-3 py-2 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                    Khoản mục
                  </th>
                  <th className="px-3 py-2 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                    Số lượng
                  </th>
                  <th className="px-3 py-2 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                    Đơn giá
                  </th>
                  <th className="px-3 py-2 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                    Thành tiền
                  </th>
                </tr>
              </thead>
              <tbody>
                <tr className="border-b border-gray-100 bg-gray-50/70">
                  <td className="px-3 py-2.5 text-[12px] font-bold text-gray-800">1. Tiền bàn</td>
                  <td className="px-3 py-2.5" />
                  <td className="px-3 py-2.5" />
                  <td className="px-3 py-2.5" />
                </tr>
                <tr className="border-b border-gray-50">
                  <td className="px-3 py-2.5">Bàn tiệc {bookingDetail.hallName}</td>
                  <td className="px-3 py-2.5 text-right">{bookingDetail.tableCount}</td>
                  <td className="px-3 py-2.5 text-right">
                    {formatCurrency(bookingDetail.tablePrice)}
                  </td>
                  <td className="px-3 py-2.5 text-right font-semibold">
                    {formatCurrency(preview.hallTotalAmount)}
                  </td>
                </tr>
                <tr className="border-b border-gray-100 bg-gray-50/70">
                  <td className="px-3 py-2.5 text-[12px] font-bold text-gray-800">
                    2. Tiền món ăn
                  </td>
                  <td className="px-3 py-2.5" />
                  <td className="px-3 py-2.5" />
                  <td className="px-3 py-2.5" />
                </tr>
                {bookingDetail.menuItems.map((item) => (
                  <tr key={item.id} className="border-b border-gray-50">
                    <td className="px-3 py-2.5">{item.menuItemName}</td>
                    <td className="px-3 py-2.5 text-right">
                      {item.quantity * bookingDetail.tableCount}
                    </td>
                    <td className="px-3 py-2.5 text-right">{formatCurrency(item.priceSnapshot)}</td>
                    <td className="px-3 py-2.5 text-right font-semibold">
                      {formatCurrency(item.lineTotal * bookingDetail.tableCount)}
                    </td>
                  </tr>
                ))}
                <tr className="border-b border-gray-100 bg-gray-50/70">
                  <td className="px-3 py-2.5 text-[12px] font-bold text-gray-800">
                    3. Tiền dịch vụ
                  </td>
                  <td className="px-3 py-2.5" />
                  <td className="px-3 py-2.5" />
                  <td className="px-3 py-2.5" />
                </tr>
                {bookingDetail.services.length > 0 ? (
                  bookingDetail.services.map((item) => (
                    <tr key={item.id} className="border-b border-gray-50">
                      <td className="px-3 py-2.5">{item.serviceName}</td>
                      <td className="px-3 py-2.5 text-right">{item.quantity}</td>
                      <td className="px-3 py-2.5 text-right">
                        {formatCurrency(item.priceSnapshot)}
                      </td>
                      <td className="px-3 py-2.5 text-right font-semibold">
                        {formatCurrency(item.lineTotal)}
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr className="border-b border-gray-50">
                    <td className="px-3 py-2.5 text-gray-400">Không có dịch vụ</td>
                    <td className="px-3 py-2.5 text-right" aria-label="Không áp dụng" />
                    <td className="px-3 py-2.5 text-right" aria-label="Không áp dụng" />
                    <td className="px-3 py-2.5 text-right font-semibold">{formatCurrency(0)}</td>
                  </tr>
                )}
                <tr className="border-b border-gray-100 bg-gray-50/70">
                  <td className="px-3 py-2.5 text-[12px] font-bold text-gray-800">
                    4. Tiền phát sinh
                  </td>
                  <td className="px-3 py-2.5" />
                  <td className="px-3 py-2.5" />
                  <td className="px-3 py-2.5" />
                </tr>
                {incidentalItems.length > 0 ? (
                  incidentalItems.map((item) => (
                    <tr key={item.id} className="border-b border-gray-50">
                      <td className="px-3 py-2.5">{item.serviceName}</td>
                      <td className="px-3 py-2.5 text-right">{item.quantity}</td>
                      <td className="px-3 py-2.5 text-right">
                        {formatCurrency(item.appliedUnitPrice)}
                      </td>
                      <td className="px-3 py-2.5 text-right font-semibold">
                        {formatCurrency(item.lineTotal)}
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr className="border-b border-gray-50">
                    <td className="px-3 py-2.5 text-gray-400">Không có dịch vụ phát sinh</td>
                    <td className="px-3 py-2.5 text-right" aria-label="Không áp dụng" />
                    <td className="px-3 py-2.5 text-right" aria-label="Không áp dụng" />
                    <td className="px-3 py-2.5 text-right font-semibold">
                      {formatCurrency(preview.incidentalsTotalAmount)}
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>

        <div className="col-span-4 rounded-xl border border-gray-100 bg-white p-4">
          <h2 className="mb-3 text-[13px] font-bold uppercase tracking-wide text-gray-700">
            C. Tổng kết
          </h2>
          <Summary label="Tổng tiền hóa đơn" value={preview.subtotalAmount} />
          <Summary label="Tiền cọc" value={preview.depositAmount} minus />
          <Summary label="Tiền phạt thanh toán trễ" value={preview.latePaymentPenaltyAmount} />
          <Summary label="Tiền còn lại" value={preview.finalAmount} strong />

          <div className="mt-4 border-t border-gray-100 pt-3">
            <label className="mb-1 block text-[11px] font-semibold uppercase tracking-wide text-gray-500">
              Ghi chú
            </label>
            <textarea
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              rows={4}
              className="w-full rounded-lg border border-gray-200 px-3 py-2 text-[13px] outline-none focus:border-rose-300"
              placeholder="Ghi chú hóa đơn..."
            />
          </div>

          {submitError && <p className="mt-3 text-[12px] text-rose-600">{submitError}</p>}
        </div>
      </div>
    </div>
  );
}

function Info({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <p className="text-[11px] font-semibold uppercase tracking-wide text-gray-500">{label}</p>
      <p className="mt-1 text-[13px] font-semibold text-gray-700">{value}</p>
    </div>
  );
}

function Summary({
  label,
  value,
  strong,
  minus,
}: {
  label: string;
  value: number;
  strong?: boolean;
  minus?: boolean;
}) {
  return (
    <div className="mb-2 flex items-center justify-between border-b border-gray-100 pb-2">
      <p className={cn("text-[13px]", strong ? "font-semibold text-gray-800" : "text-gray-600")}>
        {label}
      </p>
      <p
        className={cn(
          "text-[13px]",
          strong ? "font-bold text-rose-600" : "font-semibold text-gray-700",
        )}
      >
        {minus ? "-" : ""}
        {new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value)}
      </p>
    </div>
  );
}
