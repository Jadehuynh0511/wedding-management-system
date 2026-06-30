import type { UserGroupName } from "@/entities/user-group/model/user-group";

export const userStatusValues = ["ACTIVE", "INACTIVE"] as const;

export type UserStatus = (typeof userStatusValues)[number];

export type UserAccountSeed = {
  id: string;
  username: string;
  fullName: string;
  groupName: UserGroupName;
  status: UserStatus;
  notes: string;
};
