import { backendRequest } from "@/shared/api/backend-client";
import type { CreateHallTypePayload, Hall, HallType, UpdateHallTypePayload } from "@/features/hall-type-catalog/model/hall-type";

// accessToken chỉ truyền khi gọi từ server component; ở client bỏ trống để đi qua BFF proxy.
export async function fetchHallTypes(accessToken?: string): Promise<HallType[]> {
  return backendRequest<HallType[]>("/hall-types", { accessToken }, "Không thể tải danh sách loại sảnh.");
}

export async function fetchHallType(id: number, accessToken?: string): Promise<HallType> {
  return backendRequest<HallType>(`/hall-types/${id}`, { accessToken }, "Không thể tải loại sảnh.");
}

export async function createHallType(payload: CreateHallTypePayload): Promise<HallType> {
  return backendRequest<HallType>("/hall-types", { method: "POST", jsonBody: payload }, "Không thể tạo loại sảnh.");
}

export async function updateHallType(id: number, payload: UpdateHallTypePayload): Promise<HallType> {
  return backendRequest<HallType>(`/hall-types/${id}`, { method: "PUT", jsonBody: payload }, "Không thể cập nhật loại sảnh.");
}

export async function deleteHallType(id: number): Promise<void> {
  await backendRequest<void>(`/hall-types/${id}`, { method: "DELETE" }, "Không thể xóa loại sảnh.");
}

export async function fetchHallsByType(hallTypeId: number, accessToken?: string): Promise<Hall[]> {
  const halls = await backendRequest<Hall[]>("/halls", { accessToken }, "Không thể tải danh sách sảnh.");
  return halls.filter((h) => h.hallTypeId === hallTypeId);
}
