"use client";

import { useRouter } from "next/navigation";
import { Eye, Pencil, ShieldCheck } from "lucide-react";

import type { UserGroup } from "@/features/user-groups/model/user-group";

type GroupTableProps = {
  groups: UserGroup[];
};

export function GroupTable({ groups }: GroupTableProps) {
  const router = useRouter();

  return (
    <div className="overflow-hidden rounded-xl border border-gray-100 bg-white">
      <table className="w-full text-[13px]">
        <thead>
          <tr className="border-b border-gray-100 bg-gray-50">
            <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500 w-28 whitespace-nowrap">Mã nhóm</th>
            <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">Tên nhóm</th>
            <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">Mô tả</th>
            <th className="px-4 py-3 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500 w-32 whitespace-nowrap">Số người dùng</th>
            <th className="px-4 py-3 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500 w-28 whitespace-nowrap">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          {groups.length === 0 ? (
            <tr>
              <td colSpan={5} className="py-12 text-center text-[13px] text-gray-400">Không có dữ liệu</td>
            </tr>
          ) : (
            groups.map((group, index) => (
              <tr key={group.id} className="border-b border-gray-50 hover:bg-rose-50/30 transition-colors">
                <td className="px-4 py-3 font-mono font-bold text-gray-700">
                  NG{String(index + 1).padStart(2, "0")}
                </td>
                <td className="px-4 py-3 font-semibold text-gray-800">{group.name}</td>
                <td className="px-4 py-3 text-gray-500 max-w-sm truncate">{group.description || "—"}</td>
                <td className="px-4 py-3 text-right text-gray-600">—</td>
                <td className="px-4 py-3">
                  <div className="flex items-center justify-end gap-2">
                    <button
                      onClick={() => router.push(`/dashboard/groups/${group.id}`)}
                      className="rounded-lg p-1.5 text-gray-400 hover:bg-gray-100 hover:text-gray-600 transition-colors"
                      title="Xem chi tiết"
                    >
                      <Eye className="h-3.5 w-3.5" />
                    </button>
                    <button
                      onClick={() => router.push(`/dashboard/groups/${group.id}/edit`)}
                      className="rounded-lg p-1.5 text-gray-400 hover:bg-blue-50 hover:text-blue-600 transition-colors"
                      title="Sửa nhóm"
                    >
                      <Pencil className="h-3.5 w-3.5" />
                    </button>
                    <button
                      onClick={() => router.push(`/dashboard/groups/${group.id}/permissions`)}
                      className="rounded-lg p-1.5 text-gray-400 hover:bg-rose-50 hover:text-rose-600 transition-colors"
                      title="Phân quyền"
                    >
                      <ShieldCheck className="h-3.5 w-3.5" />
                    </button>
                  </div>
                </td>
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}