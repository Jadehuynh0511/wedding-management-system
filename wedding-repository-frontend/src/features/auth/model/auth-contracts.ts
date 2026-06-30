import type { PermissionCode } from "@/entities/permission/model/permission";
import type { UserGroupName } from "@/entities/user-group/model/user-group";
import type { ApiResponse } from "@/shared/api/api-response";

export type LoginRequest = {
  username: string;
  password: string;
};

export type LoginResponse = {
  accessToken: string;
  tokenType: "Bearer";
  expiresAt: string;
  refreshToken: string;
  refreshExpiresAt: string;
};

export type RefreshTokenResponse = LoginResponse;

export type LoginMutationResponse = {
  expiresAt: string;
  refreshExpiresAt: string;
};

export type RefreshMutationResponse = LoginMutationResponse;

export type PermissionResponse = {
  id: number;
  code: PermissionCode;
  name: string;
  moduleKey: string;
  functionalGroup: string;
  description: string;
};

export type UserGroupResponse = {
  id: number;
  name: UserGroupName;
  systemGroup: boolean;
  description: string;
};

export type GroupPermissionResponse = {
  groupId: number;
  permissionCodes: PermissionCode[];
};

export type AssignPermissionRequest = {
  groupId: number;
  permissionCode: PermissionCode;
};

export type RevokePermissionRequest = {
  groupId: number;
  permissionCode: PermissionCode;
};

export type CurrentUserResponse = {
  id: number;
  username: string;
  groupName: UserGroupName;
  permissionCodes: PermissionCode[];
};

export type LoginApiResponse = ApiResponse<LoginResponse>;
export type RefreshTokenApiResponse = ApiResponse<RefreshTokenResponse>;
export type LoginMutationApiResponse = ApiResponse<LoginMutationResponse>;
export type RefreshMutationApiResponse = ApiResponse<RefreshMutationResponse>;
export type CurrentUserApiResponse = ApiResponse<CurrentUserResponse>;
