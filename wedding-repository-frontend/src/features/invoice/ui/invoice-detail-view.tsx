"use client";

import { useEffect, useMemo } from "react";
import { ArrowLeft, Printer } from "lucide-react";

import { cn } from "@/lib/utils";
import type { InvoiceDetail } from "@/features/invoice/lib/invoice-api";

type InvoiceDetailViewProps = {
  detail: InvoiceDetail;
  autoPrint?: boolean;
};

function formatCurrency(value: number) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value ?? 0);
}

function formatDateTime(value: string) {
  return new Intl.DateTimeFormat("vi-VN", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(new Date(value));
}

function invoiceCode(invoiceId: number) {
  return `HD${String(invoiceId).padStart(4, "0")}`;
}

function receiptCode(receiptId: number) {
  return `PS${String(receiptId).padStart(4, "0")}`;
}

export function InvoiceDetailView({ detail, autoPrint = false }: InvoiceDetailViewProps) {
  const incidentalItems = useMemo(
    () => detail.incidentalReceipts.flatMap((receipt) => receipt.items),
    [detail.incidentalReceipts],
  );

  useEffect(() => {
    if (!autoPrint) return;
    const handle = window.setTimeout(() => window.print(), 50);
    return () => window.clearTimeout(handle);
  }, [autoPrint]);

  return (
    <div>
      <div className="mb-4 flex items-start justify-between">
        <div>
          <h1 className="text-[20px] font-bold tracking-wide text-gray-800">
            CHI TIẾT HÓA ĐƠN THANH TOÁN
          </h1>
          <p className="mt-1 text-[13px] text-gray-500">
            {invoiceCode(detail.id)} cho tiệc cưới TC{String(detail.weddingBookingId).padStart(4, "0")}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => window.location.assign("/dashboard/invoices")}
            className="rounded-lg border border-gray-200 px-3 py-2 text-[12px] font-semibold text-gray-600 hover:bg-gray-50"
          >
            <span className="inline-flex items-center gap-1.5">
              <ArrowLeft className="h-3.5 w-3.5" />
              Quay lại
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
        </div>
      </div>

      <div className="grid grid-cols-12 gap-4">
        <div className="col-span-8 space-y-4">
          <div className="rounded-xl border border-gray-100 bg-white p-4">
            <h2 className="mb-3 text-[13px] font-bold uppercase tracking-wide text-gray-700">
              A. Thông tin thanh toán
            </h2>
            <div className="grid grid-cols-3 gap-3 text-[13px]">
              <Info label="Mã hóa đơn" value={invoiceCode(detail.id)} />
              <Info
                label="Cô dâu - chú rể"
                value={`${detail.brideName} - ${detail.groomName}`}
              />
              <Info label="SĐT" value={detail.bridePhoneNumber} />
              <Info label="Sảnh" value={detail.hallName} />
              <Info label="Ngày đãi" value={detail.celebrationDate} />
              <Info label="Ca" value={detail.shiftName} />
              <Info label="Số bàn / dự trữ" value={`${detail.tableCount} / ${detail.reservedTableCount}`} />
              <Info label="Đơn giá bàn" value={formatCurrency(detail.tablePrice)} />
              <Info label="Ngày thanh toán" value={formatDateTime(detail.paidAt)} />
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
                <SectionHeader label="1. Tiền bàn" />
                <tr className="border-b border-gray-50">
                  <td className="px-3 py-2.5">Bàn tiệc {detail.hallName}</td>
                  <td className="px-3 py-2.5 text-right">{detail.tableCount}</td>
                  <td className="px-3 py-2.5 text-right">{formatCurrency(detail.tablePrice)}</td>
                  <td className="px-3 py-2.5 text-right font-semibold">
                    {formatCurrency(detail.hallTotalAmount)}
                  </td>
                </tr>

                <SectionHeader label="2. Tiền món ăn" />
                {detail.menuItems.map((item) => (
                  <tr key={item.id} className="border-b border-gray-50">
                    <td className="px-3 py-2.5">{item.menuItemName}</td>
                    <td className="px-3 py-2.5 text-right">{item.quantity * detail.tableCount}</td>
                    <td className="px-3 py-2.5 text-right">{formatCurrency(item.priceSnapshot)}</td>
                    <td className="px-3 py-2.5 text-right font-semibold">
                      {formatCurrency(item.lineTotal * detail.tableCount)}
                    </td>
                  </tr>
                ))}

                <SectionHeader label="3. Tiền dịch vụ" />
                {detail.services.length > 0 ? (
                  detail.services.map((item) => (
                    <tr key={item.id} className="border-b border-gray-50">
                      <td className="px-3 py-2.5">{item.serviceName}</td>
                      <td className="px-3 py-2.5 text-right">{item.quantity}</td>
                      <td className="px-3 py-2.5 text-right">{formatCurrency(item.priceSnapshot)}</td>
                      <td className="px-3 py-2.5 text-right font-semibold">
                        {formatCurrency(item.lineTotal)}
                      </td>
                    </tr>
                  ))
                ) : (
                  <EmptyLine label="Không có dịch vụ" value={0} />
                )}

                <SectionHeader label="4. Tiền phát sinh" />
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
                  <EmptyLine label="Không có dịch vụ phát sinh" value={0} />
                )}
              </tbody>
            </table>
          </div>
        </div>

        <div className="col-span-4 rounded-xl border border-gray-100 bg-white p-4">
          <h2 className="mb-3 text-[13px] font-bold uppercase tracking-wide text-gray-700">
            C. Tổng kết
          </h2>
          <Summary label="Tổng tiền hóa đơn" value={detail.subtotalAmount} />
          <Summary label="Tiền cọc" value={detail.depositAmount} minus />
          <Summary label="Còn lại trước phạt" value={detail.outstandingAmount} />
          <Summary label="Tiền phạt thanh toán trễ" value={detail.latePaymentPenaltyAmount} />
          <Summary label="Tổng thanh toán" value={detail.finalAmount} strong />

          <div className="mt-4 border-t border-gray-100 pt-3">
            <p className="mb-1 block text-[11px] font-semibold uppercase tracking-wide text-gray-500">
              Ghi chú
            </p>
            <div className="rounded-lg border border-gray-200 bg-gray-50 px-3 py-2 text-[13px] text-gray-700">
              {detail.notes || "Không có ghi chú."}
            </div>
          </div>
        </div>
      </div>

      <div className="mt-4 rounded-xl border border-gray-100 bg-white p-4">
        <div className="mb-4 flex items-center justify-between gap-3">
          <div>
            <h2 className="text-[13px] font-bold uppercase tracking-wide text-gray-700">
              Phiếu dịch vụ phát sinh
            </h2>
            <p className="mt-1 text-[12px] text-gray-500">
              Các phiếu đã được ghi nhận trước khi chốt hóa đơn.
            </p>
          </div>
          <div className="rounded-full bg-rose-50 px-3 py-1 text-[12px] font-semibold text-rose-600">
            {detail.incidentalReceipts.length} phiếu
          </div>
        </div>

        {detail.incidentalReceipts.length === 0 ? (
          <div className="rounded-lg border border-dashed border-gray-200 py-8 text-center text-[13px] text-gray-400">
            Tiệc này không có phiếu dịch vụ phát sinh.
          </div>
        ) : (
          <div className="space-y-4">
            {detail.incidentalReceipts.map((receipt) => (
              <section key={receipt.id} className="rounded-lg border border-gray-100">
                <div className="flex items-center justify-between gap-3 border-b border-gray-100 bg-gray-50 px-4 py-3">
                  <div>
                    <p className="text-[13px] font-bold text-gray-700">{receiptCode(receipt.id)}</p>
                    <p className="mt-0.5 text-[12px] text-gray-500">
                      Ngày lập: {formatDateTime(receipt.recordedAt)}
                    </p>
                  </div>
                  <div className="text-right">
                    <p className="text-[12px] text-gray-500">Tổng tiền</p>
                    <p className="text-[14px] font-bold text-rose-600">
                      {formatCurrency(receipt.totalAmount)}
                    </p>
                  </div>
                </div>
                <div className="overflow-x-auto">
                  <table className="w-full min-w-[720px] text-[12px]">
                    <thead>
                      <tr className="border-b border-gray-100">
                        <th className="px-4 py-2 text-left font-semibold text-gray-500">Dịch vụ</th>
                        <th className="px-4 py-2 text-left font-semibold text-gray-500">Đơn vị</th>
                        <th className="px-4 py-2 text-right font-semibold text-gray-500">SL</th>
                        <th className="px-4 py-2 text-right font-semibold text-gray-500">Đơn giá</th>
                        <th className="px-4 py-2 text-right font-semibold text-gray-500">Thành tiền</th>
                        <th className="px-4 py-2 text-left font-semibold text-gray-500">Ghi chú</th>
                      </tr>
                    </thead>
                    <tbody>
                      {receipt.items.map((item) => (
                        <tr key={item.id} className="border-b border-gray-50 last:border-b-0">
                          <td className="px-4 py-2.5 font-medium text-gray-700">{item.serviceName}</td>
                          <td className="px-4 py-2.5 text-gray-600">{item.unitName}</td>
                          <td className="px-4 py-2.5 text-right text-gray-600">{item.quantity}</td>
                          <td className="px-4 py-2.5 text-right text-gray-600">
                            {formatCurrency(item.appliedUnitPrice)}
                          </td>
                          <td className="px-4 py-2.5 text-right font-semibold text-gray-700">
                            {formatCurrency(item.lineTotal)}
                          </td>
                          <td className="px-4 py-2.5 text-gray-600">{item.notes || "-"}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
                {receipt.notes && (
                  <p className="border-t border-gray-100 px-4 py-3 text-[12px] text-gray-500">
                    Ghi chú phiếu: {receipt.notes}
                  </p>
                )}
              </section>
            ))}
          </div>
        )}
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

function SectionHeader({ label }: { label: string }) {
  return (
    <tr className="border-b border-gray-100 bg-gray-50/70">
      <td className="px-3 py-2.5 text-[12px] font-bold text-gray-800">{label}</td>
      <td className="px-3 py-2.5" />
      <td className="px-3 py-2.5" />
      <td className="px-3 py-2.5" />
    </tr>
  );
}

function EmptyLine({ label, value }: { label: string; value: number }) {
  return (
    <tr className="border-b border-gray-50">
      <td className="px-3 py-2.5 text-gray-400">{label}</td>
      <td className="px-3 py-2.5 text-right" aria-label="Khong ap dung" />
      <td className="px-3 py-2.5 text-right" aria-label="Khong ap dung" />
      <td className="px-3 py-2.5 text-right font-semibold">{formatCurrency(value)}</td>
    </tr>
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
        {formatCurrency(value)}
      </p>
    </div>
  );
}
