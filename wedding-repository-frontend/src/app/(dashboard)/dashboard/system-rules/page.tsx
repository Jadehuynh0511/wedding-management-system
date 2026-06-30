import { redirect } from "next/navigation";

import { requireAuthenticatedSession } from "@/features/auth/lib/server-session";
import { getRequestCookieValue } from "@/features/auth/lib/request-cookies";
import { canAccessRoute, findDashboardRouteByHref } from "@/features/rbac/lib/authorization";
import { fetchSystemSettings } from "@/features/system-rules/lib/system-rules-api";
import { fetchAuditLogs } from "@/features/audit-logs/lib/audit-log-api";
import { SystemRulesForm } from "@/features/system-rules/ui/system-rules-form";
import { AUTH_ACCESS_TOKEN_COOKIE_NAME } from "@/features/auth/model/auth-session";

export default async function SystemRulesPage() {
  const session = await requireAuthenticatedSession();
  const route = findDashboardRouteByHref("/dashboard/system-rules");
  if (route && !canAccessRoute(session, route)) redirect("/forbidden");

  const accessToken = getRequestCookieValue(AUTH_ACCESS_TOKEN_COOKIE_NAME) ?? "";

  const [settings, auditPage] = await Promise.all([
    fetchSystemSettings(accessToken),
    fetchAuditLogs(
      {
        actionCode: "SYSTEM_RULE",
        page: 0,
        size: 10
      },
      accessToken
    ).catch(() => ({ items: [], totalElements: 0, totalPages: 0, page: 0, size: 10 }))
  ]);

  const isAdmin = session.groupName === "ADMIN";

  return (
    <SystemRulesForm
      initial={settings}
      recentChanges={auditPage.items}
      isAdmin={isAdmin}
    />
  );
}