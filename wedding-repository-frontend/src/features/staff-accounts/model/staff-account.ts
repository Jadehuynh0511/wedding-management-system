export type StaffAccountStatus = "ACTIVE" | "INACTIVE";

export type StaffAccount = {
  id: number;
  username: string;
  fullName: string;
  email: string | null;
  phoneNumber: string | null;
  status: StaffAccountStatus;
  groupId: number;
  groupName: string;
};

export type StaffAccountPayload = {
  username: string;
  fullName: string;
  email: string | null;
  phoneNumber: string | null;
  groupId: number;
  status: StaffAccountStatus;
};

export type CreateStaffAccountPayload = StaffAccountPayload & {
  password: string;
};

export const STAFF_STATUS_LABEL: Record<StaffAccountStatus, string> = {
  ACTIVE: "Hoạt động",
  INACTIVE: "Khóa",
};

export const STAFF_STATUS_BADGE_CLASS: Record<StaffAccountStatus, string> = {
  ACTIVE: "border-emerald-100 bg-emerald-50 text-emerald-600",
  INACTIVE: "border-gray-200 bg-gray-50 text-gray-500",
};

export function formatStaffCode(id: number) {
  return `NV${String(id).padStart(3, "0")}`;
}

export function getStaffInitials(fullName: string) {
  const parts = fullName.trim().split(/\s+/).filter(Boolean);

  if (parts.length === 0) return "?";
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();

  return `${parts[parts.length - 2][0]}${parts[parts.length - 1][0]}`.toUpperCase();
}
