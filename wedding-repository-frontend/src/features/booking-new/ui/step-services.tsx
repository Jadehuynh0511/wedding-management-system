"use client";

import { useMemo, useState } from "react";
import { Check, Plus, Search, Trash2, X } from "lucide-react";
import { cn } from "@/lib/utils";
import type { BookingFormState, BookingService } from "@/features/booking-new/model/booking";

type ServiceItem = { id: number; serviceName: string; unitName: string; currentPrice: number };

type StepServicesProps = {
  state: BookingFormState;
  onChange: (patch: Partial<BookingFormState>) => void;
  availableServices: ServiceItem[];
};

function fmt(n: number) {
  return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(n);
}

export function StepServices({ state, onChange, availableServices }: StepServicesProps) {
  const [pickerOpen, setPickerOpen] = useState(false);
  const [searchText, setSearchText] = useState("");

  function addService(svc: ServiceItem) {
    if (state.services.find((s) => s.serviceId === svc.id)) return;
    onChange({
      services: [
        ...state.services,
        {
          serviceId: svc.id,
          serviceName: svc.serviceName,
          unitName: svc.unitName,
          pricePerUnit: svc.currentPrice,
          quantity: 1,
          notes: "",
        },
      ],
    });
  }

  function removeService(serviceId: number) {
    onChange({ services: state.services.filter((s) => s.serviceId !== serviceId) });
  }

  function updateService(serviceId: number, patch: Partial<BookingService>) {
    onChange({
      services: state.services.map((s) => (s.serviceId === serviceId ? { ...s, ...patch } : s)),
    });
  }

  const serviceTotal = state.services.reduce((sum, s) => sum + s.pricePerUnit * s.quantity, 0);
  const selectedServiceIds = useMemo(
    () => new Set(state.services.map((item) => item.serviceId)),
    [state.services],
  );

  const filteredServices = useMemo(() => {
    const keyword = searchText.trim().toLowerCase();
    if (!keyword) return availableServices;

    return availableServices.filter(
      (svc) =>
        svc.serviceName.toLowerCase().includes(keyword) ||
        svc.unitName.toLowerCase().includes(keyword),
    );
  }, [availableServices, searchText]);

  const addableServices = availableServices.filter((svc) => !selectedServiceIds.has(svc.id));

  return (
    <div className="rounded-xl border border-gray-100 bg-white p-5">
      <div className="mb-4 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <span className="flex h-5 w-5 items-center justify-center rounded-full bg-rose-500 text-[10px] font-bold text-white">
            4
          </span>
          <h2 className="text-[13px] font-bold uppercase tracking-wide text-gray-700">
            Dịch vụ đi kèm
          </h2>
        </div>

        <div className="relative">
          <button
            type="button"
            disabled={addableServices.length === 0}
            onClick={() => setPickerOpen((open) => !open)}
            className="flex items-center gap-1.5 rounded-lg border border-gray-200 px-3 py-1.5 text-[12px] font-semibold text-gray-600 hover:bg-gray-50 transition-colors disabled:cursor-not-allowed disabled:opacity-40"
          >
            <Plus className="h-3.5 w-3.5" />
            Thêm dịch vụ
          </button>

          {pickerOpen && addableServices.length > 0 && (
            <div className="absolute right-0 top-10 z-30 w-[460px] rounded-xl border border-gray-100 bg-white shadow-xl">
              <div className="border-b border-gray-100 p-3">
                <div className="mb-2 flex items-center justify-between">
                  <p className="text-[12px] font-semibold uppercase tracking-wide text-gray-500">
                    Chọn dịch vụ
                  </p>
                  <button
                    type="button"
                    onClick={() => setPickerOpen(false)}
                    className="rounded p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors"
                  >
                    <X className="h-3.5 w-3.5" />
                  </button>
                </div>
                <div className="relative">
                  <Search className="pointer-events-none absolute left-2.5 top-1/2 h-3.5 w-3.5 -translate-y-1/2 text-gray-400" />
                  <input
                    value={searchText}
                    onChange={(e) => setSearchText(e.target.value)}
                    placeholder="Tìm theo tên dịch vụ hoặc đơn vị..."
                    className="w-full rounded-lg border border-gray-200 py-2 pl-8 pr-3 text-[12px] text-gray-700 outline-none focus:border-rose-300"
                  />
                </div>
              </div>

              <div className="max-h-72 overflow-y-auto p-2">
                {filteredServices.length === 0 && (
                  <p className="py-6 text-center text-[12px] text-gray-400">
                    Không tìm thấy dịch vụ phù hợp.
                  </p>
                )}

                {filteredServices.map((svc) => {
                  const selected = selectedServiceIds.has(svc.id);

                  return (
                    <button
                      key={svc.id}
                      type="button"
                      onClick={() => {
                        if (selected) return;
                        addService(svc);
                      }}
                      className={cn(
                        "mb-1 flex w-full items-center justify-between rounded-lg border px-3 py-2 text-left transition-colors",
                        selected
                          ? "cursor-default border-green-200 bg-green-50"
                          : "border-transparent hover:border-rose-200 hover:bg-rose-50",
                      )}
                    >
                      <div>
                        <p className="text-[13px] font-semibold text-gray-800">{svc.serviceName}</p>
                        <p className="text-[11px] text-gray-400">{svc.unitName}</p>
                      </div>
                      <div className="flex items-center gap-2">
                        <span className="text-[12px] font-medium text-gray-700">
                          {fmt(svc.currentPrice)}
                        </span>
                        {selected && (
                          <span className="inline-flex items-center gap-1 rounded-full border border-green-200 bg-white px-2 py-0.5 text-[10px] font-semibold text-green-600">
                            <Check className="h-3 w-3" />
                            Đã chọn
                          </span>
                        )}
                      </div>
                    </button>
                  );
                })}
              </div>

              <div className="flex items-center justify-between border-t border-gray-100 bg-gray-50 px-3 py-2 text-[11px] text-gray-500">
                <span>Tổng dịch vụ hiện có: {availableServices.length}</span>
                <span>Đã chọn: {state.services.length}</span>
              </div>
            </div>
          )}
        </div>
      </div>

      {state.services.length === 0 ? (
        <p className="py-6 text-center text-[13px] text-gray-400">
          Chưa có dịch vụ nào. Nhấn "+ Thêm dịch vụ" để thêm.
        </p>
      ) : (
        <>
          <div className="overflow-x-auto">
            <table className="w-full min-w-[720px] text-[13px]">
              <thead>
                <tr className="border-b border-gray-100 bg-gray-50">
                  <th className="w-16 whitespace-nowrap px-3 py-2.5 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                    STT
                  </th>
                  <th className="w-72 whitespace-nowrap px-3 py-2.5 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                    Tên dịch vụ
                  </th>
                  <th className="w-32 whitespace-nowrap px-3 py-2.5 text-center text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                    Số lượng
                  </th>
                  <th className="w-32 whitespace-nowrap px-3 py-2.5 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                    Đơn giá
                  </th>
                  <th className="w-32 whitespace-nowrap px-3 py-2.5 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                    Thành tiền
                  </th>
                  <th className="w-8" />
                </tr>
              </thead>
              <tbody>
                {state.services.map((svc, i) => (
                  <tr key={svc.serviceId} className="border-b border-gray-50">
                    <td className="whitespace-nowrap px-3 py-2.5 text-gray-500">{i + 1}</td>
                    <td className="whitespace-nowrap px-3 py-2.5 font-medium text-gray-800">
                      {svc.serviceName}
                    </td>
                    <td className="whitespace-nowrap px-3 py-2.5 text-center">
                      <input
                        type="number"
                        min={1}
                        value={svc.quantity}
                        onChange={(e) => {
                          const parsed = parseInt(e.target.value, 10);
                          if (!Number.isFinite(parsed)) return;
                          updateService(svc.serviceId, { quantity: Math.max(1, parsed) });
                        }}
                        className="w-16 rounded border border-gray-200 px-2 py-1 text-center text-[13px] text-gray-700 outline-none focus:border-rose-300"
                      />
                    </td>
                    <td className="whitespace-nowrap px-3 py-2.5 text-right text-gray-600">
                      {fmt(svc.pricePerUnit)}
                    </td>
                    <td className="whitespace-nowrap px-3 py-2.5 text-right font-medium text-gray-800">
                      {fmt(svc.pricePerUnit * svc.quantity)}
                    </td>
                    <td className="px-3 py-2.5">
                      <button
                        type="button"
                        onClick={() => removeService(svc.serviceId)}
                        className="rounded p-1 text-gray-300 hover:bg-rose-50 hover:text-rose-500 transition-colors"
                      >
                        <Trash2 className="h-3.5 w-3.5" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="mt-3 flex items-center justify-between border-t border-gray-100 pt-3 text-[12px]">
            <span className="text-gray-500">Tổng cộng dịch vụ đi kèm</span>
            <span className="font-semibold text-gray-700">{fmt(serviceTotal)}</span>
          </div>
        </>
      )}
    </div>
  );
}
