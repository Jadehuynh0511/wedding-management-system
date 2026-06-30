import Link from "next/link";

import { cn } from "@/lib/utils";
import type {
  WeddingBookingStatus,
  WeddingBookingSummary,
} from "@/features/booking-lookup/model/booking-lookup";

type UpcomingWeddingsProps = {
  bookings: WeddingBookingSummary[];
};

const STATUS_LABELS: Record<WeddingBookingStatus, string> = {
  CHO_XAC_NHAN: "Chờ xác nhận",
  DA_XAC_NHAN: "Đã đặt cọc",
  DANG_DIEN_RA: "Đang diễn ra",
  DA_THANH_TOAN: "Đã thanh toán",
  DA_HUY: "Đã hủy",
};

const STATUS_BADGES: Record<WeddingBookingStatus, string> = {
  CHO_XAC_NHAN: "bg-amber-50 text-amber-700",
  DA_XAC_NHAN: "bg-blue-50 text-blue-700",
  DANG_DIEN_RA: "bg-violet-50 text-violet-700",
  DA_THANH_TOAN: "bg-green-50 text-green-700",
  DA_HUY: "bg-rose-50 text-rose-700",
};

function todayIsoDate() {
  const today = new Date();
  return [
    today.getFullYear(),
    String(today.getMonth() + 1).padStart(2, "0"),
    String(today.getDate()).padStart(2, "0"),
  ].join("-");
}

function formatBookingCode(id: number) {
  return `TC${String(id).padStart(4, "0")}`;
}

export function UpcomingWeddings({ bookings }: UpcomingWeddingsProps) {
  const today = todayIsoDate();
  const upcomingBookings = bookings
    .filter((booking) => booking.status !== "DA_HUY" && booking.celebrationDate >= today)
    .sort((left, right) => left.celebrationDate.localeCompare(right.celebrationDate))
    .slice(0, 6);

  return (
    <div className="rounded-xl border border-gray-100 bg-white p-5">
      <div className="mb-4 flex items-center justify-between">
        <h2 className="text-[13px] font-bold uppercase tracking-wide text-gray-700">
          Tiệc cưới sắp diễn ra
        </h2>
        <Link
          href="/dashboard/bookings"
          className="rounded-lg border border-gray-200 px-3 py-1.5 text-[11px] font-semibold text-gray-500 transition-colors hover:bg-gray-50"
        >
          Xem tất cả
        </Link>
      </div>

      <div className="overflow-x-auto">
        <table className="w-full min-w-[900px] text-[13px]">
          <thead>
            <tr className="border-b border-gray-100 bg-gray-50">
              {["Mã tiệc", "Cô dâu", "Chú rể", "Sảnh", "Ngày đãi", "Ca", "Trạng thái"].map(
                (header) => (
                  <th
                    key={header}
                    className="px-4 py-2.5 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500"
                  >
                    {header}
                  </th>
                ),
              )}
            </tr>
          </thead>
          <tbody>
            {upcomingBookings.length === 0 && (
              <tr>
                <td colSpan={7} className="py-10 text-center text-[13px] text-gray-300 italic">
                  Không có tiệc cưới sắp diễn ra.
                </td>
              </tr>
            )}
            {upcomingBookings.map((booking) => (
              <tr key={booking.id} className="border-b border-gray-50 last:border-b-0">
                <td className="px-4 py-3 font-semibold text-gray-700">
                  <Link href={`/dashboard/bookings/${booking.id}`}>{formatBookingCode(booking.id)}</Link>
                </td>
                <td className="px-4 py-3 text-gray-600">{booking.brideName}</td>
                <td className="px-4 py-3 text-gray-600">{booking.groomName}</td>
                <td className="px-4 py-3 text-gray-600">{booking.hallName}</td>
                <td className="px-4 py-3 text-gray-600">{booking.celebrationDate}</td>
                <td className="px-4 py-3 text-gray-600">{booking.shiftName}</td>
                <td className="px-4 py-3">
                  <span
                    className={cn(
                      "inline-flex rounded-md px-2 py-1 text-[11px] font-semibold",
                      STATUS_BADGES[booking.status],
                    )}
                  >
                    {STATUS_LABELS[booking.status]}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
