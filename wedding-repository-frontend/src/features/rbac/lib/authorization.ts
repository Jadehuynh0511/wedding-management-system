import type { PermissionCode } from "@/entities/permission/model/permission";
import type { AuthenticatedSession } from "@/entities/session/model/authenticated-session";
import type { UserGroupName } from "@/entities/user-group/model/user-group";
import { groupPermissionSeeds, permissionCatalog, userGroupSeeds } from "@/features/rbac/model/rbac-foundation";
import { dashboardRouteManifest } from "@/shared/config/dashboard-routes";

export type GroupPermissionSummary = {
  groupName: UserGroupName;
  grantedPermissions: PermissionCode[];
  grantedCount: number;
  missingCount: number;
};

export function getPermissionCodesForGroup(groupName: UserGroupName): PermissionCode[] {
  return groupPermissionSeeds.find((groupPermission) => groupPermission.groupName === groupName)?.permissionCodes ?? [];
}

export function canManagePermissions(groupName: UserGroupName): boolean {
  return groupName === "ADMIN";
}

export function hasPermission(permissionCodes: PermissionCode[], permissionCode: PermissionCode): boolean {
  return permissionCodes.includes(permissionCode);
}

export function canAccessRoute(
  session: AuthenticatedSession,
  route: (typeof dashboardRouteManifest)[number]
): boolean {
  if (route.requiredGroup) {
    return session.groupName === route.requiredGroup;
  }

  if (!route.requiredPermission) {
    return true;
  }

  return hasPermission(session.permissionCodes, route.requiredPermission);
}

export function buildGroupPermissionSummaries(): GroupPermissionSummary[] {
  return userGroupSeeds.map((group) => {
    const grantedPermissions = getPermissionCodesForGroup(group.name);

    return {
      groupName: group.name,
      grantedPermissions,
      grantedCount: grantedPermissions.length,
      missingCount: permissionCatalog.length - grantedPermissions.length
    };
  });
}

export function buildPreviewSession(groupName: UserGroupName): AuthenticatedSession {
  return {
    id: groupName === "ADMIN" ? -1 : -2,
    username: `${groupName.toLowerCase()}-preview`,
    groupName,
    permissionCodes: getPermissionCodesForGroup(groupName)
  };
}

export function getAccessibleRoutes(groupName: UserGroupName) {
  const session = buildPreviewSession(groupName);

  // Reusing the same route guard logic here keeps the preview page honest:
  // route visibility is derived the same way production navigation should be derived.
  return dashboardRouteManifest.filter((route) => canAccessRoute(session, route));
}

export function getAccessibleRoutesForSession(session: AuthenticatedSession) {
  return dashboardRouteManifest.filter((route) => canAccessRoute(session, route));
}

export function getImplementedAccessibleRoutesForSession(session: AuthenticatedSession) {
  return getAccessibleRoutesForSession(session).filter((route) => route.availability === "ready");
}

export function getPlannedAccessibleRoutesForSession(session: AuthenticatedSession) {
  return getAccessibleRoutesForSession(session).filter((route) => route.availability !== "ready");
}

export function findDashboardRouteByHref(href: string) {
  return dashboardRouteManifest.find((route) => route.href === href);
}
