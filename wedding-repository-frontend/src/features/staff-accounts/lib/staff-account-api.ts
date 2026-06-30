import { backendRequest } from "@/shared/api/backend-client";
import type {
  CreateStaffAccountPayload,
  StaffAccount,
  StaffAccountPayload,
} from "@/features/staff-accounts/model/staff-account";

export async function fetchStaffAccounts(accessToken?: string): Promise<StaffAccount[]> {
  return backendRequest<StaffAccount[]>(
    "/users",
    { accessToken },
    "Không thể tải danh sách nhân viên.",
  );
}

export async function fetchStaffAccount(id: number, accessToken?: string): Promise<StaffAccount> {
  return backendRequest<StaffAccount>(
    `/users/${id}`,
    { accessToken },
    "Không thể tải thông tin nhân viên.",
  );
}

export async function createStaffAccount(
  payload: CreateStaffAccountPayload,
): Promise<StaffAccount> {
  return backendRequest<StaffAccount>(
    "/users",
    {
      method: "POST",
      jsonBody: payload,
    },
    "Không thể thêm nhân viên.",
  );
}

export async function updateStaffAccount(
  id: number,
  payload: StaffAccountPayload,
): Promise<StaffAccount> {
  return backendRequest<StaffAccount>(
    `/users/${id}`,
    {
      method: "PUT",
      jsonBody: payload,
    },
    "Không thể cập nhật nhân viên.",
  );
}

export async function deactivateStaffAccount(id: number): Promise<void> {
  await backendRequest<void>(
    `/users/${id}`,
    { method: "DELETE" },
    "Không thể khóa tài khoản nhân viên.",
  );
}
