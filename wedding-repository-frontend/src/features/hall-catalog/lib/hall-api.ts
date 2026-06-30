import { backendRequest } from "@/shared/api/backend-client";
import type { CreateHallPayload, Hall, UpdateHallPayload } from "@/features/hall-catalog/model/hall";

// accessToken chỉ truyền khi gọi từ server component; ở client bỏ trống để đi qua BFF proxy.
export async function fetchHalls(accessToken?: string): Promise<Hall[]> {
  return backendRequest<Hall[]>("/halls", { accessToken }, "Không thể tải danh sách sảnh.");
}

export async function fetchHall(id: number, accessToken?: string): Promise<Hall> {
  return backendRequest<Hall>(`/halls/${id}`, { accessToken }, "Không thể tải thông tin sảnh.");
}

export async function createHall(payload: CreateHallPayload): Promise<Hall> {
  return backendRequest<Hall>("/halls", { method: "POST", jsonBody: payload }, "Không thể tạo sảnh.");
}

export async function updateHall(id: number, payload: UpdateHallPayload): Promise<Hall> {
  return backendRequest<Hall>(`/halls/${id}`, { method: "PUT", jsonBody: payload }, "Không thể cập nhật sảnh.");
}

export async function deleteHall(id: number): Promise<void> {
  await backendRequest<void>(`/halls/${id}`, { method: "DELETE" }, "Không thể xóa sảnh.");
}
