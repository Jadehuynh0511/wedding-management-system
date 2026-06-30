import { redirect } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchAuditLogs } from "@/features/audit-logs/lib/audit-log-api";
import { AuditLogTable } from "@/features/audit-logs/ui/audit-log-table";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";

export default async function AuditLogsPage() {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/audit-logs");
  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  // Đọc cookie httpOnly trên server component để lấy access token, gọi API lấy data ban đầu render trang.
  // Phía client sẽ không bao giờ cầm token này, mọi request sau đó đều phải đi qua BFF proxy.
  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";
  // Call API trực tiếp từ server component, bỏ qua BFF proxy vì đã có token rồi => tránh 1 hop trung gian không cần thiết.
  const initialData = await fetchAuditLogs({ page: 0, size: 20 }, accessToken);

  return (
    <div>
      <div className="mb-6">
        <h1 className="text-[20px] font-bold tracking-wide text-gray-800">NHẬT KÝ HỆ THỐNG</h1>
        <p className="mt-1 text-[13px] text-gray-500">Lịch sử hoạt động bất biến — chỉ đọc</p>
      </div>
      <AuditLogTable initialData={initialData} />
    </div>
  );
}
