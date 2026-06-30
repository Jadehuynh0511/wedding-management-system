import { backendRequest } from "@/shared/api/backend-client";
import type {
  SystemSettings,
  UpdateCancellationRulePayload,
  UpdateDepositRulePayload,
  UpdatePenaltyRulePayload
} from "@/features/system-rules/model/system-rules";

// accessToken chỉ truyền khi gọi từ server component; ở client bỏ trống để đi qua BFF proxy.
export async function fetchSystemSettings(accessToken?: string): Promise<SystemSettings> {
  return backendRequest<SystemSettings>("/settings", { accessToken }, "Không thể tải cấu hình hệ thống.");
}

export async function updateDepositRule(payload: UpdateDepositRulePayload): Promise<SystemSettings> {
  return backendRequest<SystemSettings>(
    "/settings/deposit-rate",
    { method: "PATCH", jsonBody: payload },
    "Không thể cập nhật quy định đặt cọc."
  );
}

export async function updatePenaltyRule(payload: UpdatePenaltyRulePayload): Promise<SystemSettings> {
  return backendRequest<SystemSettings>(
    "/settings/penalty",
    { method: "PATCH", jsonBody: payload },
    "Không thể cập nhật quy định phạt trễ."
  );
}

export async function updateCancellationRule(payload: UpdateCancellationRulePayload): Promise<SystemSettings> {
  return backendRequest<SystemSettings>(
    "/settings/cancellation",
    { method: "PATCH", jsonBody: payload },
    "Không thể cập nhật quy định hủy tiệc."
  );
}
