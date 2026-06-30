"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { Heart, LoaderCircle, LogOut } from "lucide-react";

import { cn } from "@/lib/utils";
import { useAuth } from "@/features/auth/ui/auth-provider";
import { ROUTE_LABEL_VI, SIDEBAR_ROUTE_ORDER } from "@/features/dashboard-navigation/model/nav-config";
import { getAccessibleRoutesForSession } from "@/features/rbac/lib/authorization";
import { dashboardRouteManifest } from "@/shared/config/dashboard-routes";

const HOME_ROUTE = {
  href: "/dashboard",
  label: "Trang chủ quản lý"
};

const BOOKING_LOOKUP_ROUTE = "/dashboard/bookings";
const BOOKING_NEW_ROUTE = "/dashboard/bookings/new";
const BOOKING_UNIFIED_LABEL = "Tiệc cưới";

type SidebarItem = {
  href: string;
  label: string;
  activeMatchPrefix?: string;
};

export function SidebarNav() {
  const pathname = usePathname();
  const { session, logout, isLoggingOut } = useAuth();

  if (!session) return null;

  const accessibleRoutes = getAccessibleRoutesForSession(session);
  const accessibleHrefs = new Set(accessibleRoutes.map((r) => r.href));

  // Home luôn hiển thị, sau đó các route theo thứ tự định nghĩa.
  const orderedRoutes = SIDEBAR_ROUTE_ORDER.filter(
    (href) => href === HOME_ROUTE.href || accessibleHrefs.has(href)
  );
  const orderedItems: SidebarItem[] = [];
  let bookingUnifiedInserted = false;

  for (const href of orderedRoutes) {
    if (href === BOOKING_LOOKUP_ROUTE || href === BOOKING_NEW_ROUTE) {
      if (bookingUnifiedInserted) {
        continue;
      }

      const targetHref = BOOKING_LOOKUP_ROUTE;
      orderedItems.push({
        href: targetHref,
        label: BOOKING_UNIFIED_LABEL,
        activeMatchPrefix: BOOKING_LOOKUP_ROUTE
      });
      bookingUnifiedInserted = true;
      continue;
    }

    orderedItems.push({ href, label: getLabel(href) });
  }

  function isActive(item: SidebarItem) {
    if (item.href === HOME_ROUTE.href) return pathname === HOME_ROUTE.href;

    if (item.activeMatchPrefix) {
      return pathname === item.activeMatchPrefix || pathname.startsWith(item.activeMatchPrefix + "/");
    }

    return pathname === item.href || pathname.startsWith(item.href + "/");
  }

  function getLabel(href: string) {
    return ROUTE_LABEL_VI[href] ?? dashboardRouteManifest.find((r) => r.href === href)?.label ?? href;
  }

  return (
    <aside className="flex h-screen w-48 flex-shrink-0 flex-col border-r border-gray-100 bg-white">
      <div className="flex items-center gap-2.5 border-b border-gray-100 px-4 py-4">
        <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-lg bg-rose-100">
          <Heart className="h-4 w-4 fill-rose-500 text-rose-500" />
        </div>
        <div className="min-w-0">
          <p className="truncate text-[11px] font-bold leading-tight text-gray-800">QUẢN LÝ TIỆC CƯỚI</p>
          <p className="truncate text-[9px] leading-tight text-gray-400">Hệ thống quản lý nội bộ</p>
        </div>
      </div>

      <nav className="flex-1 overflow-y-auto py-2">
        <ul className="space-y-0.5 px-2">
          {orderedItems.map((item, index) => {
            const active = isActive(item);
            const num = String(index + 1).padStart(2, "0");

            return (
              <li key={item.href}>
                <Link
                  href={item.href}
                  className={cn(
                    "flex items-center gap-2 rounded-lg px-2 py-2 text-[12px] transition-colors",
                    active
                      ? "bg-rose-50 font-semibold text-rose-700"
                      : "text-gray-600 hover:bg-gray-50 hover:text-gray-900"
                  )}
                >
                  <span
                    className={cn(
                      "w-5 flex-shrink-0 text-[10px] font-semibold",
                      active ? "text-rose-400" : "text-gray-300"
                    )}
                  >
                    {num}
                  </span>
                  <span className="truncate">{item.label}</span>
                </Link>
              </li>
            );
          })}
        </ul>
      </nav>

      <div className="border-t border-gray-100 p-2">
        <button
          onClick={logout}
          disabled={isLoggingOut}
          className="flex w-full items-center gap-2 rounded-lg px-2 py-2 text-[12px] text-gray-500 transition-colors hover:bg-gray-50 hover:text-gray-700 disabled:opacity-50"
        >
          {isLoggingOut ? <LoaderCircle className="h-3.5 w-3.5 animate-spin" /> : <LogOut className="h-3.5 w-3.5" />}
          <span>Đăng xuất</span>
        </button>
      </div>
    </aside>
  );
}
