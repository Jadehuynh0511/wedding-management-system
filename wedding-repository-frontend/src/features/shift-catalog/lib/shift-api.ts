import { backendRequest } from "@/shared/api/backend-client";
import type { CreateShiftPayload, Shift, UpdateShiftPayload } from "@/features/shift-catalog/model/shift";

// accessToken chỉ truyền khi gọi từ server component; ở client bỏ trống để đi qua BFF proxy.
export async function fetchShifts(accessToken?: string): Promise<Shift[]> {
  return backendRequest<Shift[]>("/shifts", { accessToken }, "Không thể tải danh sách ca tiệc.");
}

export async function fetchShift(id: number, accessToken?: string): Promise<Shift> {
  return backendRequest<Shift>(`/shifts/${id}`, { accessToken }, "Không thể tải ca tiệc.");
}

export async function createShift(payload: CreateShiftPayload): Promise<Shift> {
  return backendRequest<Shift>("/shifts", { method: "POST", jsonBody: payload }, "Không thể tạo ca tiệc.");
}

export async function updateShift(id: number, payload: UpdateShiftPayload): Promise<Shift> {
  return backendRequest<Shift>(`/shifts/${id}`, { method: "PUT", jsonBody: payload }, "Không thể cập nhật ca tiệc.");
}

export async function deleteShift(id: number): Promise<void> {
  await backendRequest<void>(`/shifts/${id}`, { method: "DELETE" }, "Không thể xóa ca tiệc.");
}
