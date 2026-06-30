"use client";

import { usePathname, useRouter, useSearchParams } from "next/navigation";

import { cn } from "@/lib/utils";

type WeddingBookingModeTabsProps = {
  canViewList: boolean;
  canCreate: boolean;
};

export function WeddingBookingModeTabs({ canViewList, canCreate }: WeddingBookingModeTabsProps) {
  const router = useRouter();
  const pathname = usePathname();
  const searchParams = useSearchParams();
  const mode = searchParams.get("mode");

  const isNewPath = pathname === "/dashboard/bookings/new";
  const isListPath = pathname === "/dashboard/bookings";
  const activeMode = isNewPath || mode === "new" ? "new" : "list";

  if (!canViewList && !canCreate) return null;

  return (
    <div className="mb-5 inline-flex items-center rounded-full border border-gray-200 bg-white p-1">
      <button
        type="button"
        disabled={!canViewList}
        onClick={() => router.push("/dashboard/bookings?mode=list")}
        className={cn(
          "rounded-full px-4 py-1.5 text-[12px] font-semibold transition-colors",
          activeMode === "list" && isListPath ? "bg-rose-500 text-white" : "text-gray-600 hover:bg-gray-50",
          !canViewList && "cursor-not-allowed text-gray-300 hover:bg-white"
        )}
        title={canViewList ? "Tra cứu tiệc cưới" : "Bạn chưa có quyền tra cứu tiệc cưới"}
      >
        Tra cứu tiệc cưới
      </button>
      <button
        type="button"
        disabled={!canCreate}
        onClick={() => router.push("/dashboard/bookings/new?mode=new")}
        className={cn(
          "rounded-full px-4 py-1.5 text-[12px] font-semibold transition-colors",
          activeMode === "new" && isNewPath ? "bg-rose-500 text-white" : "text-gray-600 hover:bg-gray-50",
          !canCreate && "cursor-not-allowed text-gray-300 hover:bg-white"
        )}
        title={canCreate ? "Nhận đặt tiệc cưới" : "Bạn chưa có quyền nhận đặt tiệc cưới"}
      >
        Nhận đặt tiệc cưới
      </button>
    </div>
  );
}
