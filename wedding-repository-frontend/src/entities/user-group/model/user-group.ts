import type { PermissionCode } from "@/entities/permission/model/permission";

export const userGroupNameValues = ["ADMIN", "STAFF"] as const;

export type UserGroupName = (typeof userGroupNameValues)[number];

export type UserGroup = {
  id: string;
  name: UserGroupName;
  systemGroup: boolean;
  description: string;
};

export type GroupPermissionState = {
  groupId: string;
  groupName: UserGroupName;
  permissionCodes: PermissionCode[];
};
