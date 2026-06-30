"use client";

import { useEffect, useMemo, useRef, useState } from "react";
import { ArrowLeft, Check, ChevronDown, Plus, Printer, Save, Search, Trash2 } from "lucide-react";

import { cn } from "@/lib/utils";
import { createIncidentalReceipt } from "@/features/incidental-receipt/lib/incidental-api";
import {
  calcIncidentalTotal,
  type IncidentalReceiptFormState,
  type IncidentalReceiptLine,
  type IncidentalServiceOption,
} from "@/features/incidental-receipt/model/incidental";

type IncidentalReceiptFormProps = {
  initialState: IncidentalReceiptFormState;
  availableServices: IncidentalServiceOption[];
};

function formatCurrency(value: number) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(value);
}

function formatDateForInput(value: Date) {
  const y = value.getFullYear();
  const m = String(value.getMonth() + 1).padStart(2, "0");
  const d = String(value.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
}

function buildIncidentalSummaryQuery(savedId: number, totalAmount: number, itemNames: string[]) {
  const params = new URLSearchParams();
  params.set("incidentalSaved", String(savedId));
  params.set("incidentalTotal", String(totalAmount));
  if (itemNames.length > 0) {
    params.set("incidentalItems", itemNames.join(" | "));
  }
  return params.toString();
}

export function IncidentalReceiptForm({
  initialState,
  availableServices,
}: IncidentalReceiptFormProps) {
  const [state, setState] = useState<IncidentalReceiptFormState>(initialState);
  const [selectedServiceId, setSelectedServiceId] = useState("");
  const [serviceQuery, setServiceQuery] = useState("");
  const [servicePickerOpen, setServicePickerOpen] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const servicePickerRef = useRef<HTMLDivElement | null>(null);

  const usedServiceIds = useMemo(() => new Set(state.lines.map((line) => line.serviceId)), [state.lines]);
  const selectableServices = useMemo(
    () => availableServices.filter((service) => !usedServiceIds.has(service.id)),
    [availableServices, usedServiceIds],
  );
  const filteredSelectableServices = useMemo(() => {
    const normalized = serviceQuery.trim().toLowerCase();
    if (!normalized) return selectableServices;
    return selectableServices.filter((service) =>
      `${service.serviceName} ${service.unitName}`.toLowerCase().includes(normalized),
    );
  }, [selectableServices, serviceQuery]);
  const selectedService = useMemo(
    () => selectableServices.find((service) => String(service.id) === selectedServiceId) ?? null,
    [selectableServices, selectedServiceId],
  );
  const total = useMemo(() => calcIncidentalTotal(state.lines), [state.lines]);

  useEffect(() => {
    function handleOutsideClick(event: MouseEvent) {
      if (!servicePickerRef.current) return;
      if (!servicePickerRef.current.contains(event.target as Node)) setServicePickerOpen(false);
    }
    document.addEventListener("mousedown", handleOutsideClick);
    return () => document.removeEventListener("mousedown", handleOutsideClick);
  }, []);

  function backToBookingDetail() {
    window.location.assign(`/dashboard/bookings/${state.bookingId}`);
  }

  function addServiceLine() {
    if (!selectedServiceId) return;
    const service = selectableServices.find((item) => String(item.id) === selectedServiceId);
    if (!service) return;

    const nextLine: IncidentalReceiptLine = {
      serviceId: service.id,
      serviceName: service.serviceName,
      unitName: service.unitName,
      quantity: 1,
      unitPrice: service.currentPrice,
      notes: "",
    };

    setState((prev) => ({ ...prev, lines: [...prev.lines, nextLine] }));
    setSelectedServiceId("");
    setServiceQuery("");
    setServicePickerOpen(false);
  }

  function removeLine(serviceId: number) {
    setState((prev) => ({
      ...prev,
      lines: prev.lines.filter((line) => line.serviceId !== serviceId),
    }));
  }

  function updateLine(serviceId: number, patch: Partial<IncidentalReceiptLine>) {
    setState((prev) => ({
      ...prev,
      lines: prev.lines.map((line) => (line.serviceId === serviceId ? { ...line, ...patch } : line)),
    }));
  }

  async function handleSubmit() {
    if (state.lines.length === 0) {
      setSubmitError("Vui lòng thêm ít nhất 1 dịch vụ phát sinh.");
      return;
    }

    const hasInvalidQty = state.lines.some(
      (line) => !Number.isInteger(line.quantity) || !Number.isFinite(line.quantity) || line.quantity < 1,
    );
    if (hasInvalidQty) {
      setSubmitError("Số lượng dịch vụ phải là số nguyên và lớn hơn hoặc bằng 1.");
      return;
    }

    setSubmitting(true);
    setSubmitError(null);
    try {
      const saved = await createIncidentalReceipt(state.bookingId, {
        notes: state.notes.trim() || null,
        items: state.lines.map((line) => ({
          serviceId: line.serviceId,
          quantity: line.quantity,
          notes: line.notes.trim() || null,
        })),
      });
      const itemNames = saved.items.map((item) => item.serviceName);
      const query = buildIncidentalSummaryQuery(saved.id, saved.totalAmount, itemNames);
      window.location.assign(`/dashboard/bookings/${state.bookingId}?${query}`);
    } catch (error) {
      setSubmitError(error instanceof Error ? error.message : "Không thể lưu phiếu dịch vụ phát sinh.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <div className="mb-4 flex items-start justify-between">
        <div>
          <h1 className="text-[20px] font-bold tracking-wide text-gray-800">LẬP PHIẾU DỊCH VỤ PHÁT SINH</h1>
          <p className="mt-1 text-[13px] text-gray-500">Ghi nhận các dịch vụ phát sinh trong ngày tiệc</p>
        </div>
        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={backToBookingDetail}
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
              In phiếu
            </span>
          </button>
          <button
            type="button"
            onClick={handleSubmit}
            disabled={submitting}
            className={cn(
              "rounded-lg px-3 py-2 text-[12px] font-semibold text-white",
              submitting ? "cursor-not-allowed bg-rose-300" : "bg-rose-500 hover:bg-rose-600",
            )}
          >
            <span className="inline-flex items-center gap-1.5">
              <Save className="h-3.5 w-3.5" />
              {submitting ? "Đang lưu..." : "Lưu phiếu"}
            </span>
          </button>
        </div>
      </div>

      <div className="rounded-xl border border-gray-100 bg-white p-4">
        <div className="grid grid-cols-4 gap-3">
          <div>
            <label className="mb-1 block text-[11px] font-semibold uppercase tracking-wide text-gray-500">Mã tiệc</label>
            <input value={state.bookingCode} disabled className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2 text-[13px] text-gray-600" />
          </div>
          <div>
            <label className="mb-1 block text-[11px] font-semibold uppercase tracking-wide text-gray-500">Cô dâu - Chú rể</label>
            <input value={`${state.brideName} - ${state.groomName}`} disabled className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2 text-[13px] text-gray-600" />
          </div>
          <div>
            <label className="mb-1 block text-[11px] font-semibold uppercase tracking-wide text-gray-500">Ngày lập</label>
            <input
              value={state.createdDate || formatDateForInput(new Date())}
              onChange={(e) => setState((prev) => ({ ...prev, createdDate: e.target.value }))}
              type="date"
              className="w-full rounded-lg border border-gray-200 bg-white px-3 py-2 text-[13px] text-gray-700"
            />
          </div>
          <div>
            <label className="mb-1 block text-[11px] font-semibold uppercase tracking-wide text-gray-500">Người lập</label>
            <input value={state.creatorName} disabled className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2 text-[13px] text-gray-600" />
          </div>
        </div>
      </div>

      <div className="mt-3 rounded-xl border border-gray-100 bg-white p-4">
        <div className="mb-3 flex items-center justify-between">
          <h2 className="text-[13px] font-bold uppercase tracking-wide text-gray-700">Danh sách dịch vụ phát sinh</h2>
          <div className="flex items-center gap-2">
            <div ref={servicePickerRef} className="relative">
              <button
                type="button"
                onClick={() => setServicePickerOpen((prev) => !prev)}
                className="flex min-w-[280px] items-center justify-between rounded-lg border border-gray-200 bg-white px-3 py-2 text-left text-[13px] text-gray-700"
              >
                <span className={cn(!selectedService && "text-gray-400")}>
                  {selectedService
                    ? `${selectedService.serviceName} (${selectedService.unitName}) - ${formatCurrency(selectedService.currentPrice)}`
                    : "Chọn dịch vụ để thêm..."}
                </span>
                <ChevronDown className="h-4 w-4 text-gray-400" />
              </button>

              {servicePickerOpen && (
                <div className="absolute right-0 z-20 mt-1 w-[420px] rounded-lg border border-gray-200 bg-white p-2 shadow-lg">
                  <div className="relative mb-2">
                    <Search className="pointer-events-none absolute left-2 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-gray-400" />
                    <input
                      value={serviceQuery}
                      onChange={(e) => setServiceQuery(e.target.value)}
                      placeholder="Tìm dịch vụ..."
                      className="w-full rounded-md border border-gray-200 py-1.5 pl-7 pr-2 text-[12px] text-gray-700 outline-none focus:border-rose-300"
                    />
                  </div>
                  <div className="max-h-64 overflow-y-auto">
                    {filteredSelectableServices.length === 0 ? (
                      <div className="px-2 py-4 text-center text-[12px] text-gray-400">Không còn dịch vụ phù hợp.</div>
                    ) : (
                      filteredSelectableServices.map((service) => {
                        const active = String(service.id) === selectedServiceId;
                        return (
                          <button
                            key={service.id}
                            type="button"
                            onClick={() => {
                              setSelectedServiceId(String(service.id));
                              setServicePickerOpen(false);
                            }}
                            className={cn(
                              "flex w-full items-start justify-between rounded-md px-2 py-2 text-left hover:bg-rose-50",
                              active && "bg-rose-50",
                            )}
                          >
                            <div>
                              <p className="text-[13px] font-medium text-gray-800">{service.serviceName}</p>
                              <p className="text-[11px] text-gray-400">{service.unitName}</p>
                            </div>
                            <div className="ml-4 flex items-center gap-2">
                              <span className="text-[12px] text-gray-700">{formatCurrency(service.currentPrice)}</span>
                              {active && <Check className="h-4 w-4 text-rose-500" />}
                            </div>
                          </button>
                        );
                      })
                    )}
                  </div>
                </div>
              )}
            </div>

            <button
              type="button"
              onClick={addServiceLine}
              disabled={!selectedServiceId}
              className="rounded-lg border border-gray-200 px-3 py-2 text-[12px] font-semibold text-gray-600 hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-40"
            >
              <span className="inline-flex items-center gap-1.5">
                <Plus className="h-3.5 w-3.5" />
                Thêm dịch vụ
              </span>
            </button>
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full min-w-[920px] text-[13px]">
            <thead>
              <tr className="border-y border-gray-100 bg-gray-50">
                <th className="w-16 px-3 py-2 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">STT</th>
                <th className="px-3 py-2 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">Dịch vụ</th>
                <th className="w-32 px-3 py-2 text-center text-[11px] font-semibold uppercase tracking-wide text-gray-500">Số lượng</th>
                <th className="w-36 px-3 py-2 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500">Đơn giá</th>
                <th className="w-36 px-3 py-2 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500">Thành tiền</th>
                <th className="w-10" />
              </tr>
            </thead>
            <tbody>
              {state.lines.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-3 py-8 text-center text-gray-400">Chưa có dịch vụ phát sinh nào.</td>
                </tr>
              )}
              {state.lines.map((line, idx) => (
                <tr key={line.serviceId} className="border-b border-gray-50">
                  <td className="px-3 py-2.5 text-gray-500">{idx + 1}</td>
                  <td className="px-3 py-2.5">
                    <p className="font-medium text-gray-800">{line.serviceName}</p>
                    <p className="text-[11px] text-gray-400">{line.unitName}</p>
                  </td>
                  <td className="px-3 py-2.5 text-center">
                    <input
                      type="number"
                      min={1}
                      value={line.quantity}
                      onChange={(e) => {
                        const parsed = parseInt(e.target.value, 10);
                        if (!Number.isFinite(parsed)) return;
                        updateLine(line.serviceId, { quantity: Math.max(1, parsed) });
                      }}
                      className="w-16 rounded border border-gray-200 px-2 py-1 text-center text-[13px] text-gray-700 outline-none focus:border-rose-300"
                    />
                  </td>
                  <td className="px-3 py-2.5 text-right text-gray-700">{formatCurrency(line.unitPrice)}</td>
                  <td className="px-3 py-2.5 text-right font-medium text-gray-800">{formatCurrency(line.quantity * line.unitPrice)}</td>
                  <td className="px-3 py-2.5 text-right">
                    <button
                      type="button"
                      onClick={() => removeLine(line.serviceId)}
                      className="rounded p-1 text-gray-300 hover:bg-rose-50 hover:text-rose-600"
                    >
                      <Trash2 className="h-3.5 w-3.5" />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="mt-3 flex items-center justify-between border-t border-gray-100 pt-3">
          <div className="w-full">
            <label className="mb-1 block text-[11px] font-semibold uppercase tracking-wide text-gray-500">Ghi chú</label>
            <input
              value={state.notes}
              onChange={(e) => setState((prev) => ({ ...prev, notes: e.target.value }))}
              placeholder="Ghi chú thêm..."
              className="w-full rounded-lg border border-gray-200 px-3 py-2 text-[13px] text-gray-700"
            />
          </div>
          <div className="ml-4 min-w-[260px] text-right">
            <p className="text-[12px] text-gray-500">Tổng tiền phát sinh</p>
            <p className="text-[20px] font-bold text-rose-600">{formatCurrency(total)}</p>
          </div>
        </div>

        {submitError && <p className="mt-3 text-[12px] text-rose-600">{submitError}</p>}
      </div>
    </div>
  );
}
