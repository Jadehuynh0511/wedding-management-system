"use client";

import { useCallback, useState } from "react";
import { useRouter } from "next/navigation";
import { ArrowLeft, ChevronRight, ChevronLeft, X, Save } from "lucide-react";
import { cn } from "@/lib/utils";
import { createBooking } from "@/features/booking-new/lib/booking-api";
import { DEFAULT_FORM_STATE, type BookingFormState } from "@/features/booking-new/model/booking";
import { BookingCostSummary } from "@/features/booking-new/ui/booking-cost-summary";
import { StepBookingInfo } from "@/features/booking-new/ui/step-booking-info";
import { StepMenuItems } from "@/features/booking-new/ui/step-menu-items";
import { StepServices } from "@/features/booking-new/ui/step-services";

type Shift = { id: number; shiftName: string; startTime: string; endTime: string };
type MenuItem = { id: number; itemName: string; itemCategory: string; currentPrice: number };
type ServiceItem = { id: number; serviceName: string; unitName: string; currentPrice: number };

type BookingFormProps = {
  shifts: Shift[];
  menuItems: MenuItem[];
  services: ServiceItem[];
  minimumDepositPct: number;
  currentUserName: string;
};

const STEPS = ["Thông tin", "Món ăn", "Dịch vụ", "Xác nhận"];
const BOOKING_LIST_PATH = "/dashboard/bookings";

export function BookingForm({
  shifts,
  menuItems,
  services,
  minimumDepositPct,
  currentUserName,
}: BookingFormProps) {
  const router = useRouter();
  const [step, setStep] = useState(1);
  const [formState, setFormState] = useState<BookingFormState>(DEFAULT_FORM_STATE);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitting, setSubmitting] = useState(false);
  const [submitError, setSubmitError] = useState<string | null>(null);
  const [tablePrice, setTablePrice] = useState(0);
  function navigateToBookingList() {
    window.location.assign(BOOKING_LIST_PATH);
  }

  const handleFormChange = useCallback((patch: Partial<BookingFormState>) => {
    setFormState((prev) => {
      const hasChanges = (Object.keys(patch) as Array<keyof BookingFormState>).some(
        (key) => prev[key] !== patch[key],
      );
      return hasChanges ? { ...prev, ...patch } : prev;
    });
  }, []);

  function validateStep1(): boolean {
    const errs: Record<string, string> = {};
    if (!formState.groomName.trim()) errs.groomName = "Vui lòng nhập tên chú rể.";
    if (!formState.brideName.trim()) errs.brideName = "Vui lòng nhập tên cô dâu.";
    if (!formState.bridePhoneNumber.trim()) errs.bridePhoneNumber = "Vui lòng nhập số điện thoại.";
    if (!formState.celebrationDate) errs.celebrationDate = "Vui lòng chọn ngày đãi tiệc.";
    if (!formState.shiftId) errs.shiftId = "Vui lòng chọn ca tiệc.";
    if (!formState.hallId) errs.hallId = "Vui lòng chọn sảnh.";
    if (!formState.tableCount || parseInt(formState.tableCount, 10) <= 0) {
      errs.tableCount = "Vui lòng nhập số bàn.";
    }

    const depositAmount = parseFloat(formState.depositAmount) || 0;
    const tableCount = parseInt(formState.tableCount, 10) || 0;
    const minimumDeposit = (tableCount * tablePrice * minimumDepositPct) / 100;

    if (depositAmount <= 0) {
      errs.depositAmount = "Vui lòng nhập tiền đặt cọc.";
    } else if (minimumDeposit > 0 && depositAmount < minimumDeposit) {
      errs.depositAmount = `Tiền đặt cọc phải tối thiểu ${new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(minimumDeposit)}.`;
    }

    setErrors(errs);
    return Object.keys(errs).length === 0;
  }

  function validateStep2(): boolean {
    if (formState.menuItems.length === 0) {
      setErrors({ menuItems: "Phải có ít nhất 1 món ăn." });
      return false;
    }
    setErrors({});
    return true;
  }

  function handleNext() {
    if (step === 1 && !validateStep1()) return;
    if (step === 2 && !validateStep2()) return;
    setErrors({});
    setStep((s) => Math.min(s + 1, 4));
  }

  async function handleSubmit() {
    setSubmitError(null);
    setSubmitting(true);
    try {
      await createBooking(formState);
      router.push(BOOKING_LIST_PATH);
      router.refresh();
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : "Đã có lỗi xảy ra.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <div className="mb-6 flex items-start justify-between">
        <div>
          <h1 className="text-[20px] font-bold tracking-wide text-gray-800">NHẬN ĐẶT TIỆC CƯỚI</h1>
          <p className="mt-1 text-[13px] text-gray-500">Tạo phiếu đặt tiệc cưới mới.</p>
        </div>
        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={navigateToBookingList}
            className="inline-flex items-center justify-center gap-1.5 rounded-lg border border-gray-200 px-4 py-2 text-[13px] font-semibold text-gray-600 transition-colors hover:bg-gray-50 hover:text-gray-800"
          >
            <ArrowLeft className="h-3.5 w-3.5" />
            Quay lại
          </button>
          <button
            type="button"
            onClick={navigateToBookingList}
            className="flex items-center gap-1.5 rounded-lg border border-gray-200 px-3 py-2 text-[12px] font-semibold text-gray-600 hover:bg-gray-50 transition-colors"
          >
            <X className="h-3.5 w-3.5" />
            Hủy
          </button>
          {step === 4 && (
            <button
              onClick={handleSubmit}
              disabled={submitting}
              className={cn(
                "flex items-center gap-1.5 rounded-lg px-3 py-2 text-[12px] font-semibold text-white transition-colors",
                submitting ? "bg-rose-300 cursor-not-allowed" : "bg-rose-500 hover:bg-rose-600",
              )}
            >
              <Save className="h-3.5 w-3.5" />
              {submitting ? "Đang lưu..." : "Lưu phiếu đặt"}
            </button>
          )}
        </div>
      </div>

      <div className="mb-5 flex items-center gap-0">
        {STEPS.map((label, i) => {
          const stepNum = i + 1;
          const active = stepNum === step;
          const done = stepNum < step;
          return (
            <div key={stepNum} className="flex items-center">
              <div className="flex items-center gap-2">
                <span
                  className={cn(
                    "flex h-6 w-6 items-center justify-center rounded-full text-[11px] font-bold",
                    done
                      ? "bg-green-500 text-white"
                      : active
                        ? "bg-rose-500 text-white"
                        : "bg-gray-200 text-gray-500",
                  )}
                >
                  {done ? "✓" : stepNum}
                </span>
                <span
                  className={cn(
                    "text-[12px] font-semibold",
                    active ? "text-rose-600" : done ? "text-green-600" : "text-gray-400",
                  )}
                >
                  {label}
                </span>
              </div>
              {i < STEPS.length - 1 && (
                <div className={cn("mx-3 h-0.5 w-10", done ? "bg-green-400" : "bg-gray-200")} />
              )}
            </div>
          );
        })}
      </div>

      <div className="flex gap-5 items-start">
        <div className="flex-1 space-y-5">
          {step === 1 && (
            <StepBookingInfo
              state={formState}
              onChange={handleFormChange}
              onTablePriceChange={setTablePrice}
              shifts={shifts}
              errors={errors}
              currentUserName={currentUserName}
              minimumDepositPct={minimumDepositPct}
              tablePrice={tablePrice}
            />
          )}
          {step === 2 && (
            <StepMenuItems
              state={formState}
              onChange={handleFormChange}
              availableMenuItems={menuItems}
              tableCount={parseInt(formState.tableCount, 10) || 0}
            />
          )}
          {step === 3 && (
            <StepServices
              state={formState}
              onChange={handleFormChange}
              availableServices={services}
            />
          )}
          {step === 4 && (
            <ConfirmStep state={formState} shifts={shifts} submitError={submitError} />
          )}

          {errors.menuItems && <p className="text-[12px] text-rose-600">{errors.menuItems}</p>}

          <div className="flex justify-between">
            <button
              type="button"
              onClick={() => setStep((s) => Math.max(1, s - 1))}
              disabled={step === 1}
              className="flex items-center gap-1.5 rounded-lg border border-gray-200 px-4 py-2 text-[12px] font-semibold text-gray-600 hover:bg-gray-50 transition-colors disabled:opacity-40"
            >
              <ChevronLeft className="h-3.5 w-3.5" />
              Bước trước
            </button>
            {step < 4 && (
              <button
                type="button"
                onClick={handleNext}
                className="flex items-center gap-1.5 rounded-lg bg-rose-500 px-4 py-2 text-[12px] font-semibold text-white hover:bg-rose-600 transition-colors"
              >
                Tiếp theo
                <ChevronRight className="h-3.5 w-3.5" />
              </button>
            )}
          </div>
        </div>

        <div className="w-64 flex-shrink-0 sticky top-6">
          <BookingCostSummary
            state={formState}
            tablePrice={tablePrice}
            minimumDepositPct={minimumDepositPct}
            currentStep={step}
            submitting={submitting}
            onCancel={() => router.push(BOOKING_LIST_PATH)}
            onSave={handleSubmit}
          />
        </div>
      </div>
    </div>
  );
}

function ConfirmStep({
  state,
  shifts,
  submitError,
}: {
  state: BookingFormState;
  shifts: { id: number; shiftName: string }[];
  submitError: string | null;
}) {
  const shiftName = shifts.find((s) => String(s.id) === state.shiftId)?.shiftName ?? "—";
  function fmt(n: number) {
    return new Intl.NumberFormat("vi-VN", { style: "currency", currency: "VND" }).format(n);
  }

  return (
    <div className="rounded-xl border border-gray-100 bg-white p-5 space-y-4">
      <div className="flex items-center gap-2 mb-2">
        <span className="text-[13px] font-bold uppercase tracking-wide text-gray-700">
          Xác nhận thông tin đặt tiệc
        </span>
      </div>
      <div className="grid grid-cols-2 gap-3 text-[13px]">
        <Row label="Chú rể" value={state.groomName} />
        <Row label="Cô dâu" value={state.brideName} />
        <Row label="Số điện thoại" value={state.bridePhoneNumber} />
        <Row label="Ngày đãi tiệc" value={state.celebrationDate} />
        <Row label="Ca tiệc" value={shiftName} />
        <Row
          label="Số bàn"
          value={`${state.tableCount} bàn${parseInt(state.reservedTableCount, 10) > 0 ? ` (+${state.reservedTableCount} dự trữ)` : ""}`}
        />
        <Row
          label="Tiền đặt cọc"
          value={parseFloat(state.depositAmount) > 0 ? fmt(parseFloat(state.depositAmount)) : "—"}
        />
        <Row
          label="Hình thức TT"
          value={
            state.paymentMethod === "TIEN_MAT"
              ? "Tiền mặt"
              : state.paymentMethod === "CHUYEN_KHOAN"
                ? "Chuyển khoản"
                : "Thẻ"
          }
        />
        <Row label="Số món ăn" value={`${state.menuItems.length} món`} />
        <Row label="Số dịch vụ" value={`${state.services.length} dịch vụ`} />
      </div>
      {submitError && <p className="text-[12px] text-rose-600">{submitError}</p>}
    </div>
  );
}

function Row({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-lg bg-gray-50 px-3 py-2.5">
      <p className="text-[11px] font-semibold text-gray-500 mb-0.5">{label}</p>
      <p className="text-[13px] text-gray-800">{value || "—"}</p>
    </div>
  );
}
