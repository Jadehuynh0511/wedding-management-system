import { backendRequest } from "@/shared/api/backend-client";
import type {
  CreateServicePayload,
  ServiceItem,
  ServiceItemDetail,
  UpdateServicePayload,
  UpdateServicePricePayload
} from "@/features/service-catalog/model/service";

// accessToken chỉ truyền khi gọi từ server component; ở client bỏ trống để đi qua BFF proxy.
export async function fetchServices(accessToken?: string): Promise<ServiceItem[]> {
  return backendRequest<ServiceItem[]>("/services", { accessToken }, "Không thể tải danh sách dịch vụ.");
}

export async function fetchService(id: number, accessToken?: string): Promise<ServiceItemDetail> {
  return backendRequest<ServiceItemDetail>(`/services/${id}`, { accessToken }, "Không thể tải dịch vụ.");
}

export async function createService(payload: CreateServicePayload): Promise<ServiceItem> {
  return backendRequest<ServiceItem>("/services", { method: "POST", jsonBody: payload }, "Không thể tạo dịch vụ.");
}

export async function updateService(id: number, payload: UpdateServicePayload): Promise<ServiceItem> {
  return backendRequest<ServiceItem>(`/services/${id}`, { method: "PUT", jsonBody: payload }, "Không thể cập nhật dịch vụ.");
}

export async function updateServicePrice(id: number, payload: UpdateServicePricePayload): Promise<ServiceItemDetail> {
  return backendRequest<ServiceItemDetail>(`/services/${id}/price`, { method: "PATCH", jsonBody: payload }, "Không thể cập nhật giá dịch vụ.");
}

export async function deleteService(id: number): Promise<void> {
  await backendRequest<void>(`/services/${id}`, { method: "DELETE" }, "Không thể xóa dịch vụ.");
}
