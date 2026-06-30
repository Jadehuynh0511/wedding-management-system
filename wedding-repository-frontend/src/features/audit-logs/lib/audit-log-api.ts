import { backendRequest } from "@/shared/api/backend-client";
import type { AuditLogPage, AuditLogSearchParams } from "@/features/audit-logs/model/audit-log";

// accessToken chỉ truyền khi gọi từ server component; ở client bỏ trống để đi qua BFF proxy.
export async function fetchAuditLogs(
  params: AuditLogSearchParams = {},
  accessToken?: string,
): Promise<AuditLogPage> {
  const query = new URLSearchParams();
  if (params.from) query.set("from", params.from);
  if (params.to) query.set("to", params.to);
  if (params.username) query.set("username", params.username);
  if (params.actionCode) query.set("actionCode", params.actionCode);
  if (params.resultStatus) query.set("resultStatus", params.resultStatus);
  query.set("page", String(params.page ?? 0));
  query.set("size", String(params.size ?? 20));

  // Khi gọi từ server component, đã có access token rồi nên gọi thẳng API mà không qua BFF proxy => tránh 1 hop trung gian không cần thiết.
  return backendRequest<AuditLogPage>(
    `/audit-logs?${query.toString()}`,
    { accessToken },
    "Không thể tải nhật ký hệ thống.",
  );
}
