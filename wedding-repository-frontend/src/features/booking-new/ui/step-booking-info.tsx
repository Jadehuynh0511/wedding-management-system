"use client";

import { useEffect, useState } from "react";
import { cn } from "@/lib/utils";
import { fetchAvailableHalls } from "@/features/booking-new/lib/booking-api";
import type {
  AvailableHall,
  BookingFormState,
  PaymentMethod,
} from "@/features/booking-new/model/booking";
import { PAYMENT_METHOD_LABEL } from "@/features/booking-new/model/booking";

type Shift = { id: number; shiftName: string; startTime: string; endTime: string };

type StepBookingInfoProps = {
  state: BookingFormState;
  onChange: (patch: Partial<BookingFormState>) => void;
  onTablePriceChange: (tablePrice: number) => void;
  shifts: Shift[];
  errors: Record<string, string>;
  currentUserName: string;
  minimumDepositPct: number;
  tablePrice: number;
};

export function StepBookingInfo({
  state,
  onChange,
  onTablePriceChange,
  shifts,
  errors,
  currentUserName,
  minimumDepositPct,
  tablePrice,
}: StepBookingInfoProps) {
  const [availableHalls, setAvailableHalls] = useState<AvailableHall[]>([]);
  const [loadingHalls, setLoadingHalls] = useState(false);
  const [hallsError, setHallsError] = useState<string | null>(null);

  useEffect(() => {
    if (!state.celebrationDate || !state.shiftId) {
      setAvailableHalls((prev) => (prev.length === 0 ? prev : []));
      setHallsError(null);
      onChange({ hallId: "" });
      onTablePriceChange(0);
      return;
    }

    let active = true;
    setLoadingHalls(true);
    setHallsError(null);

    fetchAvailableHalls(state.celebrationDate, parseInt(state.shiftId, 10))
      .then((halls) => {
        if (!active) {
          return;
        }

        setAvailableHalls(halls);
        setHallsError(null);
        if (halls.length === 1) {
          onChange({ hallId: String(halls[0].id) });
        }
      })
      .catch((err) => {
        if (!active) {
          return;
        }

        setAvailableHalls([]);
        setHallsError(err instanceof Error ? err.message : "Không thể tải danh sách sảnh trống.");
        onTablePriceChange(0);
      })
      .finally(() => {
        if (active) {
          setLoadingHalls(false);
        }
      });

    return () => {
      active = false;
    };
  }, [state.celebrationDate, state.shiftId, onChange, onTablePriceChange]);

  useEffect(() => {
    if (!state.hallId) {
      onTablePriceChange(0);
      return;
    }

    const hall = availableHalls.find((item) => String(item.id) === state.hallId);
    onTablePriceChange(hall?.tablePrice ?? 0);
  }, [state.hallId, availableHalls, onTablePriceChange]);

  const minDeposit =
    tablePrice > 0
      ? ((parseInt(state.tableCount, 10) || 0) * tablePrice * minimumDepositPct) / 100
      : 0;
  const selectedHall = state.hallId
    ? availableHalls.find((hall) => String(hall.id) === state.hallId)
    : null;
  const selectedHallMaxCapacity = selectedHall?.maxCapacity ?? 0;

  function field(
    label: string,
    required: boolean,
    children: React.ReactNode,
    hint?: string,
    error?: string,
  ) {
    return (
      <div>
        <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
          {label} {required && <span className="text-rose-500">*</span>}
        </label>
        {children}
        {error ? (
          <p className="mt-1 text-[11px] text-rose-500">{error}</p>
        ) : (
          hint && <p className="mt-1 text-[11px] text-gray-400">{hint}</p>
        )}
      </div>
    );
  }

  const inputCls = (err?: string) =>
    cn(
      "w-full rounded-lg border px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:ring-2 focus:ring-rose-100",
      err ? "border-rose-400" : "border-gray-200 focus:border-rose-400",
    );

  return (
    <div className="space-y-6">
      <div className="rounded-xl border border-gray-100 bg-white p-5">
        <div className="mb-4 flex items-center gap-2">
          <span className="flex h-5 w-5 items-center justify-center rounded-full bg-rose-500 text-[10px] font-bold text-white">
            1
          </span>
          <h2 className="text-[13px] font-bold uppercase tracking-wide text-gray-700">
            Thông tin khách hàng
          </h2>
        </div>
        <div className="grid grid-cols-3 gap-4">
          {field(
            "Tên chú rể",
            true,
            <input
              value={state.groomName}
              onChange={(e) => onChange({ groomName: e.target.value })}
              placeholder="Nguyễn Văn Bình"
              className={inputCls(errors.groomName)}
            />,
            undefined,
            errors.groomName,
          )}
          {field(
            "Tên cô dâu",
            true,
            <input
              value={state.brideName}
              onChange={(e) => onChange({ brideName: e.target.value })}
              placeholder="Trần Thu Hà"
              className={inputCls(errors.brideName)}
            />,
            undefined,
            errors.brideName,
          )}
          {field(
            "Số điện thoại",
            true,
            <input
              value={state.bridePhoneNumber}
              onChange={(e) => onChange({ bridePhoneNumber: e.target.value })}
              placeholder="0901 234 567"
              className={inputCls(errors.bridePhoneNumber)}
            />,
            undefined,
            errors.bridePhoneNumber,
          )}
          <div className="col-span-3">
            {field(
              "Địa chỉ",
              false,
              <input
                value={state.groomPhoneNumber}
                onChange={(e) => onChange({ groomPhoneNumber: e.target.value })}
                placeholder="86 đường Lê Thánh Tôn, Phường Bến Nghé, Quận 1, TP.HCM"
                className={inputCls()}
              />,
            )}
          </div>
        </div>
      </div>

      <div className="rounded-xl border border-gray-100 bg-white p-5">
        <div className="mb-4 flex items-center gap-2">
          <span className="flex h-5 w-5 items-center justify-center rounded-full bg-rose-500 text-[10px] font-bold text-white">
            2
          </span>
          <h2 className="text-[13px] font-bold uppercase tracking-wide text-gray-700">
            Thông tin tiệc
          </h2>
        </div>
        <div className="grid grid-cols-3 gap-4">
          {field(
            "Ngày đãi tiệc",
            true,
            <input
              type="date"
              value={state.celebrationDate}
              onChange={(e) => onChange({ celebrationDate: e.target.value })}
              min={new Date().toISOString().split("T")[0]}
              className={inputCls(errors.celebrationDate)}
            />,
            undefined,
            errors.celebrationDate,
          )}
          {field(
            "Ca tiệc",
            true,
            <select
              value={state.shiftId}
              onChange={(e) => onChange({ shiftId: e.target.value })}
              className={inputCls(errors.shiftId)}
            >
              <option value="">Chọn ca tiệc</option>
              {shifts.map((s) => (
                <option key={s.id} value={String(s.id)}>
                  {s.shiftName} ({s.startTime}–{s.endTime})
                </option>
              ))}
            </select>,
            undefined,
            errors.shiftId,
          )}
          {field(
            "Sảnh",
            true,
            <select
              value={state.hallId}
              onChange={(e) => onChange({ hallId: e.target.value })}
              disabled={loadingHalls || availableHalls.length === 0}
              className={cn(
                inputCls(errors.hallId || hallsError || undefined),
                (loadingHalls || availableHalls.length === 0) && "bg-gray-50 text-gray-400",
              )}
            >
              <option value="">
                {loadingHalls
                  ? "Đang tải..."
                  : !state.celebrationDate || !state.shiftId
                    ? "Chọn ngày + ca trước"
                    : hallsError
                      ? "Lỗi tải sảnh"
                      : availableHalls.length === 0
                        ? "Không có sảnh trống cho ngày + ca này"
                        : "Chọn sảnh"}
              </option>
              {availableHalls.map((h) => (
                <option key={h.id} value={String(h.id)}>
                  {h.hallName} — {h.hallTypeName} (tối đa {h.maxCapacity} bàn)
                </option>
              ))}
            </select>,
            undefined,
            errors.hallId || hallsError || undefined,
          )}

          {field(
            "Số lượng bàn chính thức",
            true,
            <input
              type="number"
              min={1}
              max={selectedHallMaxCapacity > 0 ? selectedHallMaxCapacity : undefined}
              value={state.tableCount}
              onChange={(e) => {
                const rawValue = e.target.value;
                if (!rawValue) {
                  onChange({ tableCount: "" });
                  return;
                }

                const parsedValue = parseInt(rawValue, 10);
                if (!Number.isFinite(parsedValue)) return;

                if (selectedHallMaxCapacity > 0 && parsedValue > selectedHallMaxCapacity) {
                  onChange({ tableCount: String(selectedHallMaxCapacity) });
                  return;
                }

                onChange({ tableCount: String(Math.max(1, parsedValue)) });
              }}
              placeholder="30"
              className={inputCls(errors.tableCount)}
            />,
            selectedHallMaxCapacity > 0
              ? `Tối đa ${selectedHallMaxCapacity} bàn theo sức chứa sảnh đã chọn`
              : undefined,
            errors.tableCount,
          )}
          {field(
            "Số bàn dự trữ",
            false,
            <input
              type="number"
              min={0}
              value={state.reservedTableCount}
              onChange={(e) => onChange({ reservedTableCount: e.target.value })}
              placeholder="0"
              className={inputCls()}
            />,
          )}
          {field(
            "Đơn giá / bàn",
            false,
            <input
              value={
                tablePrice > 0
                  ? new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(
                      tablePrice,
                    )
                  : "—"
              }
              disabled
              className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2.5 text-[13px] text-gray-400 outline-none"
            />,
            "Tự động từ sảnh đã chọn",
          )}

          {field(
            "Tiền đặt cọc",
            true,
            <input
              type="number"
              min={0}
              value={state.depositAmount}
              onChange={(e) => onChange({ depositAmount: e.target.value })}
              placeholder="Nhập số tiền cọc..."
              className={inputCls(errors.depositAmount)}
            />,
            minDeposit > 0
              ? `Tiền cọc tối thiểu ${minimumDepositPct}% = ${new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(minDeposit)}`
              : undefined,
            errors.depositAmount,
          )}
          {field(
            "Hình thức thanh toán cọc",
            false,
            <select
              value={state.paymentMethod}
              onChange={(e) => onChange({ paymentMethod: e.target.value as PaymentMethod })}
              className={inputCls()}
            >
              {(Object.keys(PAYMENT_METHOD_LABEL) as PaymentMethod[]).map((k) => (
                <option key={k} value={k}>
                  {PAYMENT_METHOD_LABEL[k]}
                </option>
              ))}
            </select>,
          )}
          {field(
            "Người tiếp nhận",
            false,
            <input
              value={currentUserName}
              disabled
              className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2.5 text-[13px] text-gray-400 outline-none"
            />,
          )}

          <div className="col-span-3">
            {field(
              "Ghi chú",
              false,
              <textarea
                value={state.notes}
                onChange={(e) => onChange({ notes: e.target.value })}
                rows={2}
                placeholder="Yêu cầu đặc biệt của khách hàng..."
                className="w-full resize-none rounded-lg border border-gray-200 px-3 py-2.5 text-[13px] text-gray-800 outline-none transition focus:border-rose-400 focus:ring-2 focus:ring-rose-100"
              />,
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
