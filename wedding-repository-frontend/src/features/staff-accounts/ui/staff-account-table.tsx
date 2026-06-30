"use client";

import { useMemo, useState, useTransition } from "react";
import { useRouter } from "next/navigation";
import { Eye, Filter, LockKeyhole, LockKeyholeOpen, Pencil, Plus, RefreshCw } from "lucide-react";

import { cn } from "@/lib/utils";
import type { UserGroup } from "@/features/user-groups/model/user-group";
import {
  deactivateStaffAccount,
  updateStaffAccount,
} from "@/features/staff-accounts/lib/staff-account-api";
import {
  getStaffInitials,
  STAFF_STATUS_BADGE_CLASS,
  STAFF_STATUS_LABEL,
  type StaffAccount,
  type StaffAccountStatus,
} from "@/features/staff-accounts/model/staff-account";

type StaffAccountTableProps = {
  staffAccounts: StaffAccount[];
  groups: UserGroup[];
  currentUserId: number;
};

export function StaffAccountTable({
  staffAccounts,
  groups,
  currentUserId,
}: StaffAccountTableProps) {
  const router = useRouter();
  const [keyword, setKeyword] = useState("");
  const [groupId, setGroupId] = useState("");
  const [status, setStatus] = useState<"" | StaffAccountStatus>("");
  const [deactivatingId, setDeactivatingId] = useState<number | null>(null);
  const [unlockingId, setUnlockingId] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isPending, startTransition] = useTransition();

  const filteredStaff = useMemo(() => {
    const normalizedKeyword = keyword.trim().toLowerCase();

    return staffAccounts.filter((staff) => {
      const matchesKeyword =
        !normalizedKeyword ||
        staff.username.toLowerCase().includes(normalizedKeyword) ||
        staff.fullName.toLowerCase().includes(normalizedKeyword) ||
        (staff.email ?? "").toLowerCase().includes(normalizedKeyword);
      const matchesGroup = !groupId || staff.groupId === Number(groupId);
      const matchesStatus = !status || staff.status === status;

      return matchesKeyword && matchesGroup && matchesStatus;
    });
  }, [groupId, keyword, staffAccounts, status]);

  async function handleDeactivate(staff: StaffAccount) {
    if (staff.status === "INACTIVE") return;
    if (staff.id === currentUserId) {
      setError("Bạn không thể tự khóa tài khoản đang đăng nhập.");
      return;
    }

    const confirmed = window.confirm(`Khóa tài khoản ${staff.username}?`);
    if (!confirmed) return;

    setDeactivatingId(staff.id);
    setError(null);
    try {
      await deactivateStaffAccount(staff.id);
      router.refresh();
    } catch (deactivateError) {
      setError(
        deactivateError instanceof Error
          ? deactivateError.message
          : "Không thể khóa tài khoản nhân viên.",
      );
    } finally {
      setDeactivatingId(null);
    }
  }

  async function handleUnlock(staff: StaffAccount) {
    if (staff.status === "ACTIVE") return;
    const confirmed = window.confirm(`Mở khóa tài khoản ${staff.username}?`);
    if (!confirmed) return;

    setUnlockingId(staff.id);
    setError(null);
    try {
      await updateStaffAccount(staff.id, {
        username: staff.username,
        fullName: staff.fullName,
        email: staff.email,
        phoneNumber: staff.phoneNumber,
        groupId: staff.groupId,
        status: "ACTIVE",
      });
      router.refresh();
    } catch (unlockError) {
      setError(
        unlockError instanceof Error
          ? unlockError.message
          : "Không thể mở khóa tài khoản nhân viên.",
      );
    } finally {
      setUnlockingId(null);
    }
  }

  function handleRefresh() {
    startTransition(() => router.refresh());
  }

  return (
    <div>
      <div className="mb-4 flex items-center justify-between">
        <div />
        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={handleRefresh}
            disabled={isPending}
            className="inline-flex items-center gap-1.5 rounded-lg border border-gray-200 px-3 py-2 text-[12px] font-semibold text-gray-600 transition-colors hover:bg-gray-50 disabled:opacity-60"
          >
            <RefreshCw className={cn("h-3.5 w-3.5", isPending && "animate-spin")} />
            Làm mới
          </button>
          <button
            type="button"
            onClick={() => router.push("/dashboard/staff/new")}
            className="inline-flex items-center gap-1.5 rounded-lg bg-rose-500 px-3 py-2 text-[12px] font-semibold text-white transition-colors hover:bg-rose-600"
          >
            <Plus className="h-3.5 w-3.5" />
            Thêm nhân viên
          </button>
        </div>
      </div>

      <div className="overflow-hidden rounded-xl border border-gray-100 bg-white">
        <div className="grid grid-cols-[1.5fr_1fr_1fr_auto] gap-2 border-b border-gray-100 bg-white p-3">
          <input
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
            placeholder="Tìm theo tên, tên đăng nhập..."
            className="rounded-lg border border-gray-100 bg-rose-50/40 px-3 py-2 text-[12px] text-gray-700 outline-none focus:border-rose-200"
          />
          <select
            value={groupId}
            onChange={(event) => setGroupId(event.target.value)}
            className="rounded-lg border border-gray-100 bg-rose-50/40 px-3 py-2 text-[12px] text-gray-600 outline-none focus:border-rose-200"
          >
            <option value="">Tất cả nhóm</option>
            {groups.map((group) => (
              <option key={group.id} value={group.id}>
                {group.name}
              </option>
            ))}
          </select>
          <select
            value={status}
            onChange={(event) => setStatus(event.target.value as "" | StaffAccountStatus)}
            className="rounded-lg border border-gray-100 bg-rose-50/40 px-3 py-2 text-[12px] text-gray-600 outline-none focus:border-rose-200"
          >
            <option value="">Tất cả trạng thái</option>
            <option value="ACTIVE">Hoạt động</option>
            <option value="INACTIVE">Khóa</option>
          </select>
          <button
            type="button"
            className="inline-flex items-center justify-center gap-1.5 rounded-lg border border-gray-200 px-4 py-2 text-[12px] font-semibold text-gray-600"
          >
            <Filter className="h-3.5 w-3.5" />
            Áp dụng
          </button>
        </div>

        {error && (
          <div className="border-b border-rose-100 bg-rose-50 px-4 py-2 text-[12px] text-rose-600">
            {error}
          </div>
        )}

        <table className="w-full text-[13px]">
          <thead>
            <tr className="border-b border-gray-100 bg-gray-50">
              <th className="w-16 px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                STT
              </th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Tên đăng nhập
              </th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Họ và tên
              </th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Nhóm người dùng
              </th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Vai trò
              </th>
              <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Trạng thái
              </th>
              <th className="px-4 py-3 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                Thao tác
              </th>
            </tr>
          </thead>
          <tbody>
            {filteredStaff.length === 0 ? (
              <tr>
                <td colSpan={7} className="py-12 text-center text-[13px] text-gray-400">
                  Không có dữ liệu nhân viên
                </td>
              </tr>
            ) : (
              filteredStaff.map((staff, index) => (
                <tr
                  key={staff.id}
                  className="border-b border-gray-50 transition-colors hover:bg-rose-50/30"
                >
                  <td className="px-4 py-3 text-gray-500">{index + 1}</td>
                  <td className="px-4 py-3 font-bold text-gray-700">{staff.username}</td>
                  <td className="px-4 py-3">
                    <div className="flex items-center gap-2">
                      <span className="flex h-6 w-6 items-center justify-center rounded-full bg-rose-100 text-[10px] font-bold text-rose-500">
                        {getStaffInitials(staff.fullName)}
                      </span>
                      <span className="font-medium text-gray-700">{staff.fullName}</span>
                    </div>
                  </td>
                  <td className="px-4 py-3 text-gray-600">{staff.groupName}</td>
                  <td className="px-4 py-3 text-gray-500">{staff.groupName}</td>
                  <td className="px-4 py-3">
                    <span
                      className={cn(
                        "inline-flex rounded border px-2 py-0.5 text-[11px] font-medium",
                        STAFF_STATUS_BADGE_CLASS[staff.status],
                      )}
                    >
                      {STAFF_STATUS_LABEL[staff.status]}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex items-center justify-end gap-1.5">
                      <button
                        type="button"
                        onClick={() => router.push(`/dashboard/staff/${staff.id}`)}
                        className="rounded-lg p-1.5 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600"
                        title="Xem chi tiết"
                      >
                        <Eye className="h-3.5 w-3.5" />
                      </button>
                      <button
                        type="button"
                        onClick={() => router.push(`/dashboard/staff/${staff.id}/edit`)}
                        className="rounded-lg p-1.5 text-gray-400 transition-colors hover:bg-blue-50 hover:text-blue-600"
                        title="Sửa nhân viên"
                      >
                        <Pencil className="h-3.5 w-3.5" />
                      </button>
                      <button
                        type="button"
                        onClick={() =>
                          staff.status === "INACTIVE"
                            ? handleUnlock(staff)
                            : handleDeactivate(staff)
                        }
                        disabled={deactivatingId === staff.id || unlockingId === staff.id}
                        className={cn(
                          "rounded-lg p-1.5 transition-colors",
                          staff.status === "INACTIVE"
                            ? "text-gray-300 hover:bg-gray-50 hover:text-gray-500"
                            : staff.id === currentUserId
                              ? "text-gray-300 hover:bg-gray-50"
                              : "text-emerald-500 hover:bg-emerald-50 hover:text-emerald-600",
                        )}
                        title={
                          staff.status === "INACTIVE"
                            ? "Mở khóa"
                            : staff.id === currentUserId
                              ? ""
                              : "Khóa"
                        }
                      >
                        {staff.status === "INACTIVE" ? (
                          <LockKeyholeOpen className="h-3.5 w-3.5" />
                        ) : (
                          <LockKeyhole className="h-3.5 w-3.5" />
                        )}
                      </button>
                    </div>
                  </td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}
