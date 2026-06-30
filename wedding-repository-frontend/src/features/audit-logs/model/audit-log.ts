export type AuditResultStatus = "SUCCESS" | "FAIL";

export type AuditLog = {
  id: number;
  occurredAt: string; // ISO OffsetDateTime
  actorUserId: number;
  actorUsername: string;
  actorGroupName: string;
  actionCode: string;
  moduleKey: string;
  targetType: string | null;
  targetId: string | null;
  targetLabel: string | null;
  resultStatus: AuditResultStatus;
  description: string | null;
  errorMessage: string | null;
  details: Record<string, unknown> | null;
};

export type AuditLogPage = {
  items: AuditLog[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
};

export type AuditLogSearchParams = {
  from?: string;
  to?: string;
  username?: string;
  actionCode?: string;
  resultStatus?: AuditResultStatus | "";
  page?: number;
  size?: number;
};

export const RESULT_STATUS_CONFIG: Record<AuditResultStatus, { label: string; className: string }> = {
  SUCCESS: { label: "Thành công", className: "bg-green-50 text-green-700" },
  FAIL: { label: "Thất bại", className: "bg-rose-50 text-rose-700" }
};