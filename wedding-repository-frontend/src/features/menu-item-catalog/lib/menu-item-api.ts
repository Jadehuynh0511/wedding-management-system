import { backendRequest } from "@/shared/api/backend-client";
import type { CreateMenuItemPayload, MenuItem, UpdateMenuItemPayload } from "@/features/menu-item-catalog/model/menu-item";

// accessToken chỉ truyền khi gọi từ server component; ở client bỏ trống để đi qua BFF proxy.
export async function fetchMenuItems(accessToken?: string): Promise<MenuItem[]> {
  return backendRequest<MenuItem[]>("/menu-items", { accessToken }, "Không thể tải danh sách món ăn.");
}

export async function fetchMenuItem(id: number, accessToken?: string): Promise<MenuItem> {
  return backendRequest<MenuItem>(`/menu-items/${id}`, { accessToken }, "Không thể tải món ăn.");
}

export async function createMenuItem(payload: CreateMenuItemPayload): Promise<MenuItem> {
  return backendRequest<MenuItem>("/menu-items", { method: "POST", jsonBody: payload }, "Không thể tạo món ăn.");
}

export async function updateMenuItem(id: number, payload: UpdateMenuItemPayload): Promise<MenuItem> {
  return backendRequest<MenuItem>(`/menu-items/${id}`, { method: "PUT", jsonBody: payload }, "Không thể cập nhật món ăn.");
}

export async function deleteMenuItem(id: number): Promise<void> {
  await backendRequest<void>(`/menu-items/${id}`, { method: "DELETE" }, "Không thể xóa món ăn.");
}
