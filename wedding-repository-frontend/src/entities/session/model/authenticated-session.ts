import type { PermissionCode } from "@/entities/permission/model/permission";
import type { UserGroupName } from "@/entities/user-group/model/user-group";

export type AuthenticatedSession = {
  id: number;
  username: string;
  groupName: UserGroupName;
  permissionCodes: PermissionCode[];
};
