import Link from "next/link";

import { cn } from "@/lib/utils";
import type { WeddingBookingSummary } from "@/features/booking-lookup/model/booking-lookup";

const SHIFTS = [
  { key: "TRUA", label: "TRƯA", keywords: ["trưa", "trua"] },
  { key: "CHIEU", label: "CHIỀU", keywords: ["chiều", "chieu"] },
  { key: "TOI", label: "TỐI", keywords: ["tối", "toi"] },
];
const DAY_NAMES = ["T2", "T3", "T4", "T5", "T6", "T7", "CN"];

type HallScheduleProps = {
  bookings: WeddingBookingSummary[];
};

function getWeekDays() {
  const today = new Date();
  const monday = new Date(today);
  const day = today.getDay() || 7;
  monday.setDate(today.getDate() - day + 1);

  return Array.from({ length: 7 }, (_, index) => {
    const date = new Date(monday);
    date.setDate(monday.getDate() + index);
    return date;
  });
}

function formatIsoDate(date: Date) {
  return [
    date.getFullYear(),
    String(date.getMonth() + 1).padStart(2, "0"),
    String(date.getDate()).padStart(2, "0"),
  ].join("-");
}

function formatDayLabel(date: Date) {
  return `${String(date.getDate()).padStart(2, "0")}/${String(date.getMonth() + 1).padStart(2, "0")}`;
}

function matchesShift(booking: WeddingBookingSummary, shift: (typeof SHIFTS)[number]) {
  const shiftName = booking.shiftName.toLocaleLowerCase("vi-VN");
  return (
    shift.key === booking.shiftName ||
    shift.key === String(booking.shiftId) ||
    shift.key === booking.shiftName.toUpperCase() ||
    shift.keywords.some((keyword) => shiftName.includes(keyword))
  );
}

function isToday(date: Date) {
  const today = new Date();
  return formatIsoDate(date) === formatIsoDate(today);
}

function isActiveBooking(booking: WeddingBookingSummary) {
  return booking.status !== "DA_HUY";
}

export function HallSchedule({ bookings }: HallScheduleProps) {
  const days = getWeekDays();

  return (
    <div className="rounded-xl border border-gray-100 bg-white p-5">
      <div className="mb-4 flex items-center justify-between">
        <h2 className="text-[13px] font-bold uppercase tracking-wide text-gray-700">
          Lịch sảnh tuần này
        </h2>
        <Link
          href="/dashboard/bookings"
          className="rounded-lg border border-gray-200 px-3 py-1.5 text-[11px] font-semibold text-gray-500 transition-colors hover:bg-gray-50"
        >
          Xem cả tháng
        </Link>
      </div>

      <div className="grid grid-cols-7 gap-2">
        {days.map((day, index) => {
          const dayKey = formatIsoDate(day);
          const dayBookings = bookings.filter(
            (booking) => booking.celebrationDate === dayKey && isActiveBooking(booking),
          );

          return (
            <div key={dayKey} className="min-w-0">
              <div className="mb-2 text-center">
                <p className="text-[10px] font-semibold text-gray-400">{DAY_NAMES[index]}</p>
                <p
                  className={cn(
                    "text-[12px] font-bold",
                    isToday(day) ? "text-rose-500" : "text-gray-600",
                  )}
                >
                  {formatDayLabel(day)}
                </p>
              </div>

              <div className="space-y-1.5">
                {SHIFTS.map((shift) => {
                  const shiftBookings = dayBookings.filter((booking) => matchesShift(booking, shift));
                  const firstBooking = shiftBookings[0];

                  return (
                    <div
                      key={shift.key}
                      className={cn(
                        "min-h-14 rounded-md px-1.5 py-1",
                        shiftBookings.length > 0 ? "bg-rose-50" : "bg-gray-50",
                      )}
                    >
                      <p className="mb-0.5 text-[9px] font-semibold text-gray-400">{shift.label}</p>
                      {firstBooking ? (
                        <Link
                          href={`/dashboard/bookings/${firstBooking.id}`}
                          className="block truncate text-[10px] font-semibold text-rose-700"
                          title={shiftBookings.map((booking) => booking.hallName).join(", ")}
                        >
                          {firstBooking.hallName}
                          {shiftBookings.length > 1 ? ` +${shiftBookings.length - 1}` : ""}
                        </Link>
                      ) : (
                        <p className="text-[10px] text-gray-300 italic">—</p>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
