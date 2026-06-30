"use client";

import { useRouter } from "next/navigation";
import { ArrowLeft, CheckCircle2, Pencil } from "lucide-react";

import {
  FUNCTIONAL_GROUP_LABEL,
  MODULE_KEY_LABEL,
  PERMISSION_SCREEN_MAP,
  type ApiPermission,
} from "@/features/user-groups/model/user-group";
import {
  formatStaffCode,
  getStaffInitials,
  STAFF_STATUS_BADGE_CLASS,
  STAFF_STATUS_LABEL,
  type StaffAccount,
} from "@/features/staff-accounts/model/staff-account";
import { cn } from "@/lib/utils";

type StaffAccountDetailProps = {
  staffAccount: StaffAccount;
  permissionCatalog: ApiPermission[];
  permissionCodes: string[];
};

export function StaffAccountDetail({
  staffAccount,
  permissionCatalog,
  permissionCodes,
}: StaffAccountDetailProps) {
  const router = useRouter();
  const grantedSet = new Set(permissionCodes);
  const permissions = permissionCatalog.filter((permission) => grantedSet.has(permission.code));

  return (
    <div>
      <div className="mb-6 flex items-start justify-between">
        <div>
          <h1 className="text-[20px] font-bold tracking-wide text-gray-800">THÔNG TIN NHÂN VIÊN</h1>
          <p className="mt-1 text-[13px] text-gray-500">
            Hồ sơ chi tiết — {formatStaffCode(staffAccount.id)} {staffAccount.fullName}
          </p>
        </div>
        <div className="flex items-center gap-2">
          <button
            type="button"
            onClick={() => router.push("/dashboard/staff")}
            className="flex items-center gap-1.5 text-[12px] font-semibold text-gray-500 transition-colors hover:text-gray-700"
          >
            <ArrowLeft className="h-3.5 w-3.5" />
            Quay lại
          </button>
          <button
            type="button"
            onClick={() => router.push(`/dashboard/staff/${staffAccount.id}/edit`)}
            className="flex items-center gap-1.5 rounded-lg border border-gray-200 px-3 py-2 text-[12px] font-semibold text-gray-600 transition-colors hover:bg-gray-50"
          >
            <Pencil className="h-3.5 w-3.5" />
            Sửa nhân viên
          </button>
        </div>
      </div>

      <div className="grid grid-cols-[280px_1fr] gap-5">
        <aside className="rounded-xl border border-gray-100 bg-white p-6">
          <div className="flex flex-col items-center border-b border-gray-100 pb-5 text-center">
            <div className="flex h-24 w-24 items-center justify-center rounded-full bg-rose-100 text-[28px] font-bold text-rose-500">
              {getStaffInitials(staffAccount.fullName)}
            </div>
            <h2 className="mt-4 text-[15px] font-bold text-gray-800">{staffAccount.fullName}</h2>
            <p className="mt-1 text-[12px] text-gray-500">{staffAccount.username}</p>
          </div>

          <div className="mt-5 space-y-3">
            <ProfileMeta label="Nhóm người dùng" value={staffAccount.groupName} />
            <ProfileMeta label="Vai trò" value={staffAccount.groupName} />
            <div>
              <p className="text-[11px] font-semibold uppercase tracking-wide text-gray-400">
                Trạng thái
              </p>
              <span
                className={cn(
                  "mt-1 inline-flex rounded border px-2 py-0.5 text-[11px] font-medium",
                  STAFF_STATUS_BADGE_CLASS[staffAccount.status],
                )}
              >
                {STAFF_STATUS_LABEL[staffAccount.status]}
              </span>
            </div>
          </div>
        </aside>

        <section className="rounded-xl border border-gray-100 bg-white p-6">
          <SectionTitle index={1} title="Hồ sơ nhân viên" />
          <div className="grid grid-cols-3 gap-x-8 gap-y-5">
            <InfoItem label="Mã nhân viên" value={formatStaffCode(staffAccount.id)} />
            <InfoItem label="Tên đăng nhập" value={staffAccount.username} />
            <InfoItem label="Họ và tên" value={staffAccount.fullName} />
            <InfoItem label="Email" value={staffAccount.email ?? "—"} />
            <InfoItem label="Số điện thoại" value={staffAccount.phoneNumber ?? "—"} />
            <InfoItem label="Nhóm người dùng" value={staffAccount.groupName} />
            <InfoItem label="Vai trò" value={staffAccount.groupName} />
          </div>
        </section>
      </div>

      <section className="mt-5 rounded-xl border border-gray-100 bg-white p-6">
        <SectionTitle index={2} title={`Quyền theo nhóm — ${staffAccount.groupName}`} />
        {permissions.length === 0 ? (
          <p className="py-6 text-[13px] text-gray-400">Nhóm này chưa được gán quyền nào.</p>
        ) : (
          <table className="w-full text-[13px]">
            <thead>
              <tr className="border-b border-gray-100 bg-gray-50">
                <th className="w-48 px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                  Nhóm chức năng
                </th>
                <th className="px-4 py-3 text-left text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                  Tên chức năng
                </th>
                <th className="px-4 py-3 text-right text-[11px] font-semibold uppercase tracking-wide text-gray-500">
                  Được phép
                </th>
              </tr>
            </thead>
            <tbody>
              {permissions.map((permission) => (
                <tr key={permission.id} className="border-b border-gray-50">
                  <td className="px-4 py-3 text-[12px] text-gray-400">
                    {FUNCTIONAL_GROUP_LABEL[permission.functionalGroup] ??
                      MODULE_KEY_LABEL[permission.moduleKey] ??
                      permission.functionalGroup}
                  </td>
                  <td className="px-4 py-3">
                    <p className="font-medium text-gray-800">{permission.name}</p>
                    <p className="mt-0.5 text-[11px] text-gray-400">
                      {PERMISSION_SCREEN_MAP[permission.code] ?? permission.description}
                    </p>
                  </td>
                  <td className="px-4 py-3 text-right">
                    <CheckCircle2 className="ml-auto h-4 w-4 text-emerald-500" />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </section>
    </div>
  );
}

function SectionTitle({ index, title }: { index: number; title: string }) {
  return (
    <div className="mb-5 flex items-center gap-2">
      <span className="flex h-5 w-5 items-center justify-center rounded bg-rose-500 text-[10px] font-bold text-white">
        {index}
      </span>
      <h2 className="text-[13px] font-bold uppercase tracking-wide text-gray-700">{title}</h2>
    </div>
  );
}

function ProfileMeta({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <p className="text-[11px] font-semibold uppercase tracking-wide text-gray-400">{label}</p>
      <p className="mt-1 text-[12px] font-semibold text-gray-700">{value}</p>
    </div>
  );
}

function InfoItem({ label, value }: { label: string; value: string }) {
  return (
    <div>
      <p className="text-[11px] font-semibold text-gray-400">{label}</p>
      <p className="mt-1 text-[13px] font-semibold text-gray-800">{value}</p>
    </div>
  );
}
