"use client";

import { useRouter } from "next/navigation";
import { ArrowLeft, Pencil, ShieldCheck } from "lucide-react";

import {
  FUNCTIONAL_GROUP_LABEL,
  MODULE_KEY_LABEL,
  PERMISSION_SCREEN_MAP,
  type ApiPermission,
  type UserGroup,
} from "@/features/user-groups/model/user-group";

type GroupDetailProps = {
  group: UserGroup;
  groupIndex: number;
  grantedCodes: string[];
  permissionCatalog: ApiPermission[];
};

export function GroupDetail({
  group,
  groupIndex,
  grantedCodes,
  permissionCatalog,
}: GroupDetailProps) {
  const router = useRouter();
  const grantedSet = new Set(grantedCodes);
  const grantedPermissions = permissionCatalog.filter((p) => grantedSet.has(p.code));

  // Group by functionalGroup + moduleKey
  const grouped = grantedPermissions.reduce<Record<string, ApiPermission[]>>((acc, p) => {
    const key = `${FUNCTIONAL_GROUP_LABEL[p.functionalGroup] ?? p.functionalGroup} — ${MODULE_KEY_LABEL[p.moduleKey] ?? p.moduleKey}`;
    if (!acc[key]) acc[key] = [];
    acc[key].push(p);
    return acc;
  }, {});

  return (
    <div>
      {/* Header */}
      <div className="mb-6 flex items-start justify-between">
        <div>
          <h1 className="text-[20px] font-bold tracking-wide text-gray-800">
            THÔNG TIN NHÓM NGƯỜI DÙNG
          </h1>
          <p className="mt-1 text-[13px] text-gray-500">
            Chi tiết nhóm NG{String(groupIndex).padStart(2, "0")} — {group.name}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            onClick={() => router.push("/dashboard/groups")}
            className="flex items-center gap-1.5 text-[12px] font-semibold text-gray-500 hover:text-gray-700 transition-colors"
          >
            <ArrowLeft className="h-3.5 w-3.5" />
            Quay lại
          </button>
          <button
            onClick={() => router.push(`/dashboard/groups/${group.id}/edit`)}
            className="flex items-center gap-1.5 rounded-lg border border-gray-200 px-3 py-2 text-[12px] font-semibold text-gray-600 hover:bg-gray-50 transition-colors"
          >
            <Pencil className="h-3.5 w-3.5" />
            Sửa nhóm
          </button>
          <button
            onClick={() => router.push(`/dashboard/groups/${group.id}/permissions`)}
            className="flex items-center gap-1.5 rounded-lg bg-rose-500 px-3 py-2 text-[12px] font-semibold text-white hover:bg-rose-600 transition-colors"
          >
            <ShieldCheck className="h-3.5 w-3.5" />
            Phân quyền
          </button>
        </div>
      </div>

      {/* Section 1: Thông tin chung */}
      <div className="rounded-xl border border-gray-100 bg-white p-6">
        <div className="mb-5 flex items-center gap-2">
          <span className="flex h-5 w-5 items-center justify-center rounded-full bg-rose-500 text-[10px] font-bold text-white">
            1
          </span>
          <h2 className="text-[13px] font-bold uppercase tracking-wide text-gray-700">
            Thông tin chung
          </h2>
        </div>
        <div className="grid grid-cols-4 gap-x-8 gap-y-4">
          <div>
            <p className="text-[11px] font-semibold text-gray-500 mb-1">Mã nhóm</p>
            <p className="text-[13px] font-bold text-gray-800">
              NG{String(groupIndex).padStart(2, "0")}
            </p>
          </div>
          <div>
            <p className="text-[11px] font-semibold text-gray-500 mb-1">Tên nhóm</p>
            <p className="text-[13px] font-semibold text-gray-800">{group.name}</p>
          </div>
          <div>
            <p className="text-[11px] font-semibold text-gray-500 mb-1">Số người dùng</p>
            <p className="text-[13px] text-gray-700">—</p>
          </div>
          <div>
            <p className="text-[11px] font-semibold text-gray-500 mb-1">Nhóm hệ thống</p>
            <p className="text-[13px] text-gray-700">{group.systemGroup ? "Có" : "Không"}</p>
          </div>
        </div>
        {group.description && (
          <div className="mt-4">
            <p className="text-[11px] font-semibold text-gray-500 mb-1">Mô tả</p>
            <p className="text-[13px] text-gray-700">{group.description}</p>
          </div>
        )}
      </div>

      {/* Section 2: Danh sách chức năng được phép */}
      <div className="mt-6 rounded-xl border border-gray-100 bg-white p-6">
        <div className="mb-4 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <span className="flex h-5 w-5 items-center justify-center rounded-full bg-rose-500 text-[10px] font-bold text-white">
              2
            </span>
            <h2 className="text-[13px] font-bold uppercase tracking-wide text-gray-700">
              Danh sách chức năng được phép
            </h2>
          </div>
          <span className="text-[12px] text-gray-500">{grantedPermissions.length} quyền</span>
        </div>

        {grantedPermissions.length === 0 ? (
          <p className="text-[13px] text-gray-400 py-4">Nhóm này chưa được gán quyền nào.</p>
        ) : (
          <table className="w-full text-[13px]">
            <thead>
              <tr className="border-b border-gray-100 bg-gray-50">
                <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500 w-48">
                  Nhóm chức năng
                </th>
                <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                  Tên chức năng
                </th>
                <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                  Màn hình được load
                </th>
              </tr>
            </thead>
            <tbody>
              {Object.entries(grouped).map(([groupKey, perms]) =>
                perms.map((p, i) => (
                  <tr
                    key={p.id}
                    className="border-b border-gray-50 hover:bg-gray-50/50 transition-colors"
                  >
                    <td className="px-4 py-3 text-gray-400 text-[12px]">
                      {i === 0 ? groupKey : ""}
                    </td>
                    <td className="px-4 py-3 font-medium text-gray-800">{p.name}</td>
                    <td className="px-4 py-3 text-gray-500">
                      {PERMISSION_SCREEN_MAP[p.code] ?? "—"}
                    </td>
                  </tr>
                )),
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}
