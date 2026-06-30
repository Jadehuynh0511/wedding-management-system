"use client";

import { Bell, Search } from "lucide-react";

import { useAuth } from "@/features/auth/ui/auth-provider";
import { GROUP_DISPLAY_NAME } from "@/features/dashboard-navigation/model/nav-config";

function getInitials(username: string) {
  const parts = username.trim().split(/\s+/);
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
}

export function TopHeader() {
  const { session } = useAuth();

  if (!session) return null;

  const roleLabel = GROUP_DISPLAY_NAME[session.groupName] ?? session.groupName;
  const initials = getInitials(session.username);

  return (
    <header className="flex h-14 flex-shrink-0 items-center justify-between border-b border-gray-100 bg-white px-6">
      {/* Search */}
      <div className="flex items-center gap-2 rounded-lg border border-gray-200 bg-gray-50 px-3 py-2 w-64">
        <Search className="h-3.5 w-3.5 flex-shrink-0 text-gray-400" />
        <input
          type="text"
          placeholder="Tìm kiếm..."
          className="bg-transparent text-[12px] text-gray-700 placeholder:text-gray-400 outline-none w-full"
        />
      </div>

      {/* Right: bell + user */}
      <div className="flex items-center gap-4">
        <button className="relative rounded-lg p-1.5 text-gray-500 hover:bg-gray-50 transition-colors">
          <Bell className="h-4 w-4" />
        </button>

        <div className="flex items-center gap-2.5">
          <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full bg-rose-100 text-[11px] font-bold text-rose-700">
            {initials}
          </div>
          <div className="text-left">
            <p className="text-[12px] font-semibold leading-tight text-gray-800">
              {session.username}
            </p>
            <p className="text-[10px] leading-tight text-gray-400">{roleLabel}</p>
          </div>
        </div>
      </div>
    </header>
  );
}
