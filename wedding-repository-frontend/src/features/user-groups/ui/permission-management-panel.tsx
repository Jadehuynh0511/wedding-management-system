"use client";

import { useMemo, useState } from "react";
import { AlertCircle, Save, Shield, X } from "lucide-react";

import { cn } from "@/lib/utils";
import {
  assignPermission,
  fetchGroupPermissions,
  revokePermission,
} from "@/features/user-groups/lib/user-group-api";
import {
  FUNCTIONAL_GROUP_LABEL,
  MODULE_KEY_LABEL,
  PERMISSION_SCREEN_MAP,
  type ApiPermission,
  type UserGroup,
} from "@/features/user-groups/model/user-group";

type PermissionManagementPanelProps = {
  groups: UserGroup[];
  permissionCatalog: ApiPermission[];
  initialGroupId: number | null;
  initialGrantedCodes: string[];
};

type PermissionGroup = {
  key: string;
  label: string;
  permissions: ApiPermission[];
};

function groupPermissionCatalog(permissionCatalog: ApiPermission[]) {
  return permissionCatalog.reduce<PermissionGroup[]>((groups, permission) => {
    const functionalGroup = FUNCTIONAL_GROUP_LABEL[permission.functionalGroup] ?? permission.functionalGroup;
    const moduleKey = MODULE_KEY_LABEL[permission.moduleKey] ?? permission.moduleKey;
    const key = `${permission.functionalGroup}:${permission.moduleKey}`;
    const label = `${functionalGroup} — ${moduleKey}`;
    const existing = groups.find((group) => group.key === key);

    if (existing) {
      existing.permissions.push(permission);
      return groups;
    }

    groups.push({ key, label, permissions: [permission] });
    return groups;
  }, []);
}

function buildGroupCode(group: UserGroup, index: number) {
  return `NG${String(index + 1).padStart(2, "0")}`;
}

function areSetsEqual(left: Set<string>, right: Set<string>) {
  if (left.size !== right.size) return false;
  for (const value of left) {
    if (!right.has(value)) return false;
  }
  return true;
}

export function PermissionManagementPanel({
  groups,
  permissionCatalog,
  initialGroupId,
  initialGrantedCodes,
}: PermissionManagementPanelProps) {
  const initialSelectedGroupId = initialGroupId ?? groups[0]?.id ?? null;
  const [selectedGroupId, setSelectedGroupId] = useState<number | null>(initialSelectedGroupId);
  const [savedCodes, setSavedCodes] = useState<Set<string>>(new Set(initialGrantedCodes));
  const [draftCodes, setDraftCodes] = useState<Set<string>>(new Set(initialGrantedCodes));
  const [loadingGroup, setLoadingGroup] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const groupedPermissions = useMemo(() => groupPermissionCatalog(permissionCatalog), [permissionCatalog]);
  const dirty = !areSetsEqual(savedCodes, draftCodes);
  const selectedGroupIndex = groups.findIndex((group) => group.id === selectedGroupId);
  const selectedGroup = selectedGroupIndex >= 0 ? groups[selectedGroupIndex] : null;

  async function handleGroupChange(nextGroupId: number) {
    setSelectedGroupId(nextGroupId);
    setLoadingGroup(true);
    setError(null);

    try {
      const groupPermissions = await fetchGroupPermissions(nextGroupId);
      const nextCodes = new Set(groupPermissions.permissionCodes);
      setSavedCodes(nextCodes);
      setDraftCodes(new Set(nextCodes));
    } catch (err) {
      setSavedCodes(new Set());
      setDraftCodes(new Set());
      setError(err instanceof Error ? err.message : "Không thể tải quyền của nhóm.");
    } finally {
      setLoadingGroup(false);
    }
  }

  function togglePermission(permissionCode: string) {
    if (!selectedGroupId || saving || loadingGroup) return;

    setDraftCodes((prev) => {
      const next = new Set(prev);
      if (next.has(permissionCode)) {
        next.delete(permissionCode);
      } else {
        next.add(permissionCode);
      }
      return next;
    });
  }

  function resetDraft() {
    setDraftCodes(new Set(savedCodes));
    setError(null);
  }

  async function savePermissions() {
    if (!selectedGroupId || !dirty || saving) return;

    setSaving(true);
    setError(null);

    const toAssign = [...draftCodes].filter((code) => !savedCodes.has(code));
    const toRevoke = [...savedCodes].filter((code) => !draftCodes.has(code));

    try {
      for (const code of toAssign) {
        await assignPermission(selectedGroupId, code);
      }

      for (const code of toRevoke) {
        await revokePermission(selectedGroupId, code);
      }

      setSavedCodes(new Set(draftCodes));
    } catch (err) {
      setError(err instanceof Error ? err.message : "Không thể lưu phân quyền.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex items-start justify-between gap-3">
        <div>
          <h1 className="text-[20px] font-bold tracking-wide text-gray-800">PHÂN QUYỀN HỆ THỐNG</h1>
          <p className="mt-1 text-[13px] text-gray-500">
            Cấu hình quyền truy cập cho từng nhóm người dùng
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={resetDraft}
            disabled={!dirty || saving || loadingGroup}
            className="inline-flex items-center gap-1.5 rounded-lg border border-gray-200 px-3 py-2 text-[12px] font-semibold text-gray-600 transition-colors hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
          >
            <X className="h-3.5 w-3.5" />
            Hủy
          </button>
          <button
            type="button"
            onClick={savePermissions}
            disabled={!dirty || saving || loadingGroup || !selectedGroupId}
            className="inline-flex items-center gap-1.5 rounded-lg bg-rose-500 px-3 py-2 text-[12px] font-semibold text-white transition-colors hover:bg-rose-600 disabled:cursor-not-allowed disabled:opacity-60"
          >
            <Save className="h-3.5 w-3.5" />
            {saving ? "Đang lưu..." : "Lưu phân quyền"}
          </button>
        </div>
      </div>

      <div className="rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-[12px] text-amber-700">
        <div className="flex items-center gap-2">
          <AlertCircle className="h-3.5 w-3.5" />
          <span>
            <span className="font-semibold">Lưu ý:</span> Chỉ Admin mới được phép phân quyền. Mọi
            thay đổi sẽ có hiệu lực ngay sau khi lưu.
          </span>
        </div>
      </div>

      {error && (
        <div className="rounded-lg border border-rose-200 bg-rose-50 px-4 py-3 text-[12px] text-rose-700">
          {error}
        </div>
      )}

      <div className="rounded-xl border border-gray-100 bg-white p-5">
        <div className="grid grid-cols-12 items-center gap-3">
          <label className="col-span-12 flex items-center gap-3 md:col-span-5">
            <span className="whitespace-nowrap text-[12px] font-semibold text-gray-600">
              Nhóm người dùng:
            </span>
            <select
              value={selectedGroupId ?? ""}
              onChange={(event) => handleGroupChange(Number(event.target.value))}
              disabled={loadingGroup || saving || groups.length === 0}
              className="w-full rounded-lg border border-gray-200 bg-gray-50 px-3 py-2 text-[13px] font-semibold text-gray-700 outline-none transition-colors focus:border-rose-300 focus:bg-white disabled:cursor-wait disabled:opacity-60"
            >
              {groups.length === 0 && <option value="">Chưa có nhóm người dùng</option>}
              {groups.map((group, index) => (
                <option key={group.id} value={group.id}>
                  {buildGroupCode(group, index)} — {group.name}
                </option>
              ))}
            </select>
          </label>

          <div className="col-span-12 text-[12px] text-gray-400 md:col-span-7">
            {selectedGroup?.description || "Chọn nhóm để xem và chỉnh quyền truy cập"}
          </div>
        </div>
      </div>

      <div className="overflow-hidden rounded-xl border border-gray-100 bg-white">
        <div className="flex items-center justify-between border-b border-gray-100 px-5 py-4">
          <div className="flex items-center gap-2">
            <Shield className="h-4 w-4 text-rose-500" />
            <h2 className="text-[13px] font-bold uppercase tracking-wide text-gray-700">
              Ma trận phân quyền
              {selectedGroup ? ` — ${selectedGroup.name}` : ""}
            </h2>
          </div>
          <div className="text-[12px] text-gray-400">
            <span className="font-semibold text-gray-600">{draftCodes.size}</span>/
            {permissionCatalog.length} quyền
          </div>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full min-w-[900px] text-[13px]">
            <thead>
              <tr className="border-b border-gray-100 bg-gray-50">
                <th className="w-44 px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                  Nhóm chức năng
                </th>
                <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                  Tên chức năng
                </th>
                <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                  Màn hình được load
                </th>
                <th className="w-28 px-4 py-3 text-center text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                  Được phép
                </th>
              </tr>
            </thead>
            <tbody>
              {groupedPermissions.map((group) =>
                group.permissions.map((permission, index) => {
                  const granted = draftCodes.has(permission.code);

                  return (
                    <tr key={permission.id} className="border-b border-gray-50 last:border-b-0">
                      <td className="px-4 py-3 align-top text-[12px] font-semibold text-gray-600">
                        {index === 0 ? group.label : ""}
                      </td>
                      <td className="px-4 py-3 font-medium text-gray-700">{permission.name}</td>
                      <td className="px-4 py-3 text-[12px] text-gray-400">
                        {PERMISSION_SCREEN_MAP[permission.code] ?? permission.description ?? "—"}
                      </td>
                      <td className="px-4 py-3 text-center">
                        <button
                          type="button"
                          onClick={() => togglePermission(permission.code)}
                          disabled={!selectedGroupId || saving || loadingGroup}
                          className={cn(
                            "inline-flex h-4 w-4 items-center justify-center rounded border transition-colors disabled:cursor-not-allowed disabled:opacity-50",
                            granted
                              ? "border-rose-500 bg-rose-500 text-white"
                              : "border-gray-200 bg-white hover:border-rose-300",
                          )}
                          aria-label={`${granted ? "Thu hồi" : "Cấp"} quyền ${permission.name}`}
                        >
                          {granted && <span className="text-[11px] leading-none">✓</span>}
                        </button>
                      </td>
                    </tr>
                  );
                }),
              )}
              {permissionCatalog.length === 0 && (
                <tr>
                  <td colSpan={4} className="px-4 py-10 text-center text-[13px] text-gray-400">
                    Chưa có danh mục quyền.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
