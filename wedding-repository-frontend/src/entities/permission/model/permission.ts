export const functionalGroups = ["NGHIEP_VU", "HE_THONG"] as const;
export const moduleKeys = ["CATALOG", "BOOKING", "BILLING", "REPORTING", "SYSTEM"] as const;

export const permissionCodeValues = [
  "HALL_MANAGE",
  "WEDDING_BOOKING_CREATE",
  "WEDDING_BOOKING_VIEW",
  "INVOICE_VIEW",
  "INVOICE_CREATE",
  "DEPOSIT_RECEIPT_CREATE",
  "INCIDENTAL_RECEIPT_CREATE",
  "CANCELLATION_RECEIPT_CREATE",
  "MONTHLY_REVENUE_REPORT_GENERATE",
  "HALL_TYPE_MANAGE",
  "SHIFT_MANAGE",
  "MENU_ITEM_MANAGE",
  "SERVICE_MANAGE",
  "STAFF_ACCOUNT_MANAGE",
  "USER_GROUP_MANAGE",
  "SYSTEM_RULE_MANAGE",
  "AUDIT_LOG_VIEW"
] as const;

export type FunctionalGroup = (typeof functionalGroups)[number];
export type ModuleKey = (typeof moduleKeys)[number];
export type PermissionCode = (typeof permissionCodeValues)[number];

export type Permission = {
  id: number;
  code: PermissionCode;
  displayName: string;
  moduleKey: ModuleKey;
  functionalGroup: FunctionalGroup;
  description: string;
};
