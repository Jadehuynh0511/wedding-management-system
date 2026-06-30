import React from "react";

import { SidebarNav } from "@/features/dashboard-navigation/ui/sidebar-nav";
import { TopHeader } from "@/features/dashboard-navigation/ui/top-header";

type DashboardShellProps = {
  children: React.ReactNode;
};

export function DashboardShell({ children }: DashboardShellProps) {
  return (
    <div className="flex h-screen overflow-hidden bg-gray-50">
      <SidebarNav />

      <div className="relative flex flex-1 flex-col overflow-hidden">
        {/* Watermark */}
        <div
          className="pointer-events-none absolute inset-0 z-0 flex items-end justify-center pb-12"
          aria-hidden="true"
        >
          <img
            src="/wedding pic.png"
            alt=""
            className="w-[720px] select-none opacity-[0.08]"
          />
        </div>

        <TopHeader />
        <main className="relative z-10 flex-1 overflow-y-auto p-6">{children}</main>
      </div>
    </div>
  );
}
