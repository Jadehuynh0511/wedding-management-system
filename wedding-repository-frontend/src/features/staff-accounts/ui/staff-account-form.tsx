"use client";

import { FormEvent, ReactNode, useState } from "react";
import { useRouter } from "next/navigation";
import { ArrowLeft, KeyRound, Save } from "lucide-react";

import type { UserGroup } from "@/features/user-groups/model/user-group";
import {
  createStaffAccount,
  updateStaffAccount,
} from "@/features/staff-accounts/lib/staff-account-api";
import {
  formatStaffCode,
  type StaffAccount,
  type StaffAccountStatus,
} from "@/features/staff-accounts/model/staff-account";

type StaffAccountFormMode = "create" | "edit";

type StaffAccountFormProps = {
  mode: StaffAccountFormMode;
  groups: UserGroup[];
  staffAccount?: StaffAccount;
};

type StaffFormState = {
  fullName: string;
  username: string;
  password: string;
  email: string;
  phoneNumber: string;
  groupId: string;
  status: StaffAccountStatus;
};

export function StaffAccountForm({ mode, groups, staffAccount }: StaffAccountFormProps) {
  const router = useRouter();
  const [state, setState] = useState<StaffFormState>({
    fullName: staffAccount?.fullName ?? "",
    username: staffAccount?.username ?? "",
    password: "",
    email: staffAccount?.email ?? "",
    phoneNumber: staffAccount?.phoneNumber ?? "",
    groupId: staffAccount?.groupId ? String(staffAccount.groupId) : "",
    status: staffAccount?.status ?? "ACTIVE",
  });
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const isCreateMode = mode === "create";

  function updateField<K extends keyof StaffFormState>(key: K, value: StaffFormState[K]) {
    setState((current) => ({ ...current, [key]: value }));
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError(null);

    const groupId = Number(state.groupId);
    if (!state.fullName.trim()) return setError("Vui lòng nhập họ và tên.");
    if (!state.username.trim()) return setError("Vui lòng nhập tên đăng nhập.");
    if (isCreateMode && state.password.length < 8) {
      return setError("Mật khẩu khởi tạo phải có ít nhất 8 ký tự.");
    }
    if (!Number.isFinite(groupId) || groupId <= 0)
      return setError("Vui lòng chọn nhóm người dùng.");

    setSubmitting(true);
    try {
      const payload = {
        username: state.username.trim(),
        fullName: state.fullName.trim(),
        email: state.email.trim() || null,
        phoneNumber: state.phoneNumber.trim() || null,
        groupId,
        status: state.status,
      };

      if (isCreateMode) {
        const created = await createStaffAccount({
          ...payload,
          password: state.password,
        });
        router.push(`/dashboard/staff/${created.id}`);
      } else if (staffAccount) {
        const updated = await updateStaffAccount(staffAccount.id, payload);
        router.push(`/dashboard/staff/${updated.id}`);
      }

      router.refresh();
    } catch (submitError) {
      setError(submitError instanceof Error ? submitError.message : "Không thể lưu nhân viên.");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div>
      <div className="mb-6 flex items-start justify-between">
        <div>
          <h1 className="text-[20px] font-bold tracking-wide text-gray-800">
            {isCreateMode ? "THÊM NHÂN VIÊN" : "SỬA NHÂN VIÊN"}
          </h1>
          <p className="mt-1 text-[13px] text-gray-500">
            {isCreateMode
              ? "Tạo tài khoản nhân viên mới trong hệ thống"
              : `Chỉnh sửa tài khoản ${staffAccount ? formatStaffCode(staffAccount.id) : ""} — ${
                  staffAccount?.fullName ?? ""
                }`}
          </p>
        </div>
        <button
          type="button"
          onClick={() =>
            router.push(isCreateMode ? "/dashboard/staff" : `/dashboard/staff/${staffAccount?.id}`)
          }
          className="flex items-center gap-1.5 text-[12px] font-semibold text-gray-500 transition-colors hover:text-gray-700"
        >
          <ArrowLeft className="h-3.5 w-3.5" />
          Quay lại
        </button>
      </div>

      <form onSubmit={handleSubmit} className="rounded-xl border border-gray-100 bg-white p-6">
        <div className="mb-5 flex items-center gap-2 border-b border-gray-100 pb-4">
          <span className="flex h-5 w-5 items-center justify-center rounded bg-rose-500 text-[10px] font-bold text-white">
            1
          </span>
          <h2 className="text-[13px] font-bold uppercase tracking-wide text-gray-700">
            Thông tin tài khoản
          </h2>
        </div>

        <div className="grid grid-cols-2 gap-x-5 gap-y-4">
          <TextField
            label="Họ và tên"
            required
            value={state.fullName}
            onChange={(value) => updateField("fullName", value)}
            placeholder="Họ và tên đầy đủ"
          />
          <TextField
            label="Tên đăng nhập"
            required
            value={state.username}
            onChange={(value) => updateField("username", value)}
            placeholder="Ví dụ: baongoct"
          />
          {isCreateMode && (
            <div className="col-span-2">
              <TextField
                label="Mật khẩu"
                required
                type="password"
                value={state.password}
                onChange={(value) => updateField("password", value)}
                placeholder="Nhập mật khẩu khởi tạo"
              />
              <p className="mt-1 text-[11px] text-gray-400">
                Tối thiểu 8 ký tự. Nhân viên đổi sau lần đăng nhập đầu.
              </p>
            </div>
          )}
          <TextField
            label="Email"
            value={state.email}
            onChange={(value) => updateField("email", value)}
            placeholder="email@quanlytieccuoi.vn"
          />
          <TextField
            label="Số điện thoại"
            value={state.phoneNumber}
            onChange={(value) => updateField("phoneNumber", value)}
            placeholder="0901 234 567"
          />
          <SelectField
            label="Nhóm người dùng"
            required
            value={state.groupId}
            onChange={(value) => updateField("groupId", value)}
          >
            <option value="">Chọn nhóm</option>
            {groups.map((group) => (
              <option key={group.id} value={group.id}>
                {group.name}
              </option>
            ))}
          </SelectField>
          <SelectField
            label="Trạng thái"
            required
            value={state.status}
            onChange={(value) => updateField("status", value as StaffAccountStatus)}
          >
            <option value="ACTIVE">Hoạt động</option>
            <option value="INACTIVE">Khóa</option>
          </SelectField>
        </div>

        {!isCreateMode && (
          <button
            type="button"
            onClick={() => setError("Backend hiện chưa có API đặt lại mật khẩu.")}
            className="mt-5 inline-flex items-center gap-1.5 rounded-lg border border-gray-200 px-3 py-2 text-[12px] font-semibold text-gray-600 transition-colors hover:bg-gray-50"
          >
            <KeyRound className="h-3.5 w-3.5" />
            Đặt lại mật khẩu
          </button>
        )}

        {error && <p className="mt-4 text-[12px] font-medium text-rose-600">{error}</p>}

        <div className="mt-6 flex justify-end gap-2">
          <button
            type="button"
            onClick={() =>
              router.push(
                isCreateMode ? "/dashboard/staff" : `/dashboard/staff/${staffAccount?.id}`,
              )
            }
            className="rounded-lg border border-gray-200 px-4 py-2 text-[12px] font-semibold text-gray-600 transition-colors hover:bg-gray-50"
          >
            Hủy
          </button>
          <button
            type="submit"
            disabled={submitting}
            className="inline-flex items-center gap-1.5 rounded-lg bg-rose-500 px-4 py-2 text-[12px] font-semibold text-white transition-colors hover:bg-rose-600 disabled:cursor-not-allowed disabled:bg-rose-300"
          >
            <Save className="h-3.5 w-3.5" />
            {submitting ? "Đang lưu..." : isCreateMode ? "Lưu nhân viên" : "Lưu thay đổi"}
          </button>
        </div>
      </form>
    </div>
  );
}

function TextField({
  label,
  value,
  onChange,
  placeholder,
  required,
  type = "text",
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  required?: boolean;
  type?: string;
}) {
  return (
    <div>
      <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
        {label} {required && <span className="text-rose-500">*</span>}
      </label>
      <input
        type={type}
        value={value}
        onChange={(event) => onChange(event.target.value)}
        placeholder={placeholder}
        className="w-full rounded-lg border border-gray-100 bg-rose-50/40 px-3 py-2.5 text-[13px] text-gray-700 outline-none focus:border-rose-200"
      />
    </div>
  );
}

function SelectField({
  label,
  value,
  onChange,
  required,
  children,
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
  required?: boolean;
  children: ReactNode;
}) {
  return (
    <div>
      <label className="mb-1.5 block text-[12px] font-semibold text-gray-700">
        {label} {required && <span className="text-rose-500">*</span>}
      </label>
      <select
        value={value}
        onChange={(event) => onChange(event.target.value)}
        className="w-full rounded-lg border border-gray-100 bg-rose-50/40 px-3 py-2.5 text-[13px] text-gray-700 outline-none focus:border-rose-200"
      >
        {children}
      </select>
    </div>
  );
}
