"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { ArrowLeft, ShieldCheck, ShieldOff } from "lucide-react";

import { cn } from "@/lib/utils";
import { assignPermission, revokePermission } from "@/features/user-groups/lib/user-group-api";
import {
  FUNCTIONAL_GROUP_LABEL,
  MODULE_KEY_LABEL,
  PERMISSION_SCREEN_MAP,
  type ApiPermission,
  type UserGroup,
} from "@/features/user-groups/model/user-group";

type GroupPermissionsFormProps = {
  group: UserGroup;
  groupIndex: number;
  initialGrantedCodes: string[];
  permissionCatalog: ApiPermission[];
  isAdmin: boolean;
};

export function GroupPermissionsForm({
  group,
  groupIndex,
  initialGrantedCodes,
  permissionCatalog,
  isAdmin,
}: GroupPermissionsFormProps) {
  const router = useRouter();
  const [grantedCodes, setGrantedCodes] = useState<Set<string>>(new Set(initialGrantedCodes));
  const [pendingCode, setPendingCode] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Group permissions by functionalGroup + moduleKey
  const grouped = permissionCatalog.reduce<Record<string, ApiPermission[]>>((acc, p) => {
    const key = `${FUNCTIONAL_GROUP_LABEL[p.functionalGroup] ?? p.functionalGroup} — ${MODULE_KEY_LABEL[p.moduleKey] ?? p.moduleKey}`;
    if (!acc[key]) acc[key] = [];
    acc[key].push(p);
    return acc;
  }, {});

  async function handleToggle(code: string) {
    if (!isAdmin || pendingCode) return;
    const isGranted = grantedCodes.has(code);
    setPendingCode(code);
    setError(null);
    try {
      if (isGranted) {
        await revokePermission(group.id, code);
        setGrantedCodes((prev) => {
          const next = new Set(prev);
          next.delete(code);
          return next;
        });
      } else {
        await assignPermission(group.id, code);
        setGrantedCodes((prev) => new Set([...prev, code]));
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : "Đã có lỗi xảy ra.");
    } finally {
      setPendingCode(null);
    }
  }

  return (
    <div>
      {/* Header */}
      <div className="mb-6 flex items-start justify-between">
        <div>
          <h1 className="text-[20px] font-bold tracking-wide text-gray-800">PHÂN QUYỀN NHÓM</h1>
          <p className="mt-1 text-[13px] text-gray-500">
            Quản lý quyền của nhóm NG{String(groupIndex).padStart(2, "0")} — {group.name}
          </p>
        </div>
        <button
          onClick={() => router.push(`/dashboard/groups/${group.id}`)}
          className="flex items-center gap-1.5 text-[12px] font-semibold text-gray-500 hover:text-gray-700 transition-colors"
        >
          <ArrowLeft className="h-3.5 w-3.5" />
          Quay lại
        </button>
      </div>

      {!isAdmin && (
        <div className="mb-4 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-[12px] text-amber-700">
          Chỉ tài khoản <span className="font-semibold">Admin</span> mới được thay đổi phân quyền.
        </div>
      )}

      {error && (
        <div className="mb-4 rounded-lg border border-rose-200 bg-rose-50 px-4 py-3 text-[12px] text-rose-700">
          {error}
        </div>
      )}

      <div className="rounded-xl border border-gray-100 bg-white overflow-hidden">
        {/* Stats bar */}
        <div className="flex items-center justify-between border-b border-gray-100 bg-gray-50 px-6 py-3">
          <div className="flex items-center gap-3">
            <span className="flex items-center gap-1.5 text-[12px] text-green-700">
              <ShieldCheck className="h-3.5 w-3.5" />
              <span className="font-semibold">{grantedCodes.size}</span> quyền đang có
            </span>
            <span className="text-gray-300">|</span>
            <span className="flex items-center gap-1.5 text-[12px] text-gray-400">
              <ShieldOff className="h-3.5 w-3.5" />
              <span className="font-semibold">
                {permissionCatalog.length - grantedCodes.size}
              </span>{" "}
              quyền chưa có
            </span>
          </div>
        </div>

        <table className="w-full text-[13px]">
          <thead>
            <tr className="border-b border-gray-100 bg-gray-50/50">
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500 w-12">
                Cấp
              </th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500 w-44">
                Nhóm chức năng
              </th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Tên chức năng
              </th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Màn hình
              </th>
              <th className="px-4 py-3 text-center text-[11px] font-semibold uppercase tracking-wide text-gray-500 w-20">
                Quyền
              </th>
            </tr>
          </thead>
          <tbody>
            {Object.entries(grouped).map(([groupKey, perms]) =>
              perms.map((p, i) => {
                const granted = grantedCodes.has(p.code);
                const isPending = pendingCode === p.code;
                return (
                  <tr
                    key={p.id}
                    className={cn(
                      "border-b border-gray-50 transition-colors",
                      granted ? "bg-green-50/30" : "hover:bg-gray-50/60",
                    )}
                  >
                    <td className="px-4 py-3 text-center">
                      {granted ? (
                        <ShieldCheck className="h-3.5 w-3.5 text-green-500 mx-auto" />
                      ) : (
                        <ShieldOff className="h-3.5 w-3.5 text-gray-300 mx-auto" />
                      )}
                    </td>
                    <td className="px-4 py-3 text-[12px] text-gray-400">
                      {i === 0 ? groupKey : ""}
                    </td>
                    <td className="px-4 py-3 font-medium text-gray-800">{p.name}</td>
                    <td className="px-4 py-3 text-[12px] text-gray-500">
                      {PERMISSION_SCREEN_MAP[p.code] ?? "—"}
                    </td>
                    <td className="px-4 py-3 text-center">
                      <button
                        onClick={() => handleToggle(p.code)}
                        disabled={!isAdmin || isPending}
                        className={cn(
                          "relative h-6 w-11 rounded-full transition-colors duration-200 disabled:cursor-not-allowed",
                          granted ? "bg-green-500" : "bg-gray-200",
                          isPending && "opacity-50",
                        )}
                        title={
                          !isAdmin
                            ? "Chỉ Admin mới được thay đổi"
                            : granted
                              ? "Thu hồi quyền"
                              : "Cấp quyền"
                        }
                      >
                        <span
                          className={cn(
                            "absolute top-0.5 h-5 w-5 rounded-full bg-white shadow-md transition-all duration-200",
                            granted ? "right-0.5 left-auto" : "left-0.5 right-auto",
                          )}
                        />
                      </button>
                    </td>
                  </tr>
                );
              }),
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
