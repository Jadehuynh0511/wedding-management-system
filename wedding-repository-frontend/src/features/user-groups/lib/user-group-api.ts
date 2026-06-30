import { backendRequest } from "@/shared/api/backend-client";
import type { ApiPermission, GroupPermission, UserGroup } from "@/features/user-groups/model/user-group";

export async function fetchGroups(accessToken?: string): Promise<UserGroup[]> {
  return backendRequest<UserGroup[]>("/groups", { accessToken }, "Không thể tải danh sách nhóm.");
}

export async function fetchGroupPermissions(
  groupId: number,
  accessToken?: string,
): Promise<GroupPermission> {
  return backendRequest<GroupPermission>(
    `/groups/${groupId}/permissions`,
    { accessToken },
    "Không thể tải quyền của nhóm.",
  );
}

export async function fetchPermissionCatalog(accessToken?: string): Promise<ApiPermission[]> {
  return backendRequest<ApiPermission[]>("/permissions", { accessToken }, "Không thể tải danh mục quyền.");
}

export async function assignPermission(groupId: number, permissionCode: string): Promise<void> {
  await backendRequest<void>(
    "/permissions/assign",
    { method: "POST", jsonBody: { groupId, permissionCode } },
    "Không thể gán quyền.",
  );
}

export async function revokePermission(groupId: number, permissionCode: string): Promise<void> {
  await backendRequest<void>(
    "/permissions/revoke",
    { method: "DELETE", jsonBody: { groupId, permissionCode } },
    "Không thể thu hồi quyền.",
  );
}
