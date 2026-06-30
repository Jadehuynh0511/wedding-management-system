import type { Permission } from "@/entities/permission/model/permission";
import type { GroupPermissionState, UserGroup } from "@/entities/user-group/model/user-group";
import type { UserAccountSeed } from "@/entities/user/model/user-account";

export const lockedRbacDecisions = [
  "QD11 is an administration rule. It is not part of the permission catalog.",
  "Only the ADMIN group may assign or revoke permissions.",
  "Business authorization uses permission_code values instead of hardcoded roles.",
  "JWT should carry identity only, so permission changes take effect on the next request.",
  "English snake_case table names are the schema source of truth for RBAC tables.",
  "functionalGroup is UI and API metadata. It does not force a new database column right now."
] as const;

export const rbacTableNameMapping = [
  { legacyName: "chucnang", targetName: "permissions", role: "Permission catalog" },
  { legacyName: "nhomnguoidung", targetName: "user_groups", role: "User group container" },
  { legacyName: "nguoidung", targetName: "users", role: "Authenticated account" },
  { legacyName: "bangphanquyen", targetName: "group_permissions", role: "Group-to-permission mapping" }
] as const;

// The frontend treats this catalog as the locked source of truth until the backend
// starts returning the same rows from the seeded permissions table.
export const permissionCatalog: Permission[] = [
  {
    id: 1,
    code: "HALL_MANAGE",
    displayName: "Tiếp nhận sảnh",
    moduleKey: "CATALOG",
    functionalGroup: "NGHIEP_VU",
    description: "Manage hall inventory and reception availability."
  },
  {
    id: 2,
    code: "WEDDING_BOOKING_CREATE",
    displayName: "Nhận đặt tiệc cưới",
    moduleKey: "BOOKING",
    functionalGroup: "NGHIEP_VU",
    description: "Create wedding booking records and submit booking details."
  },
  {
    id: 3,
    code: "WEDDING_BOOKING_VIEW",
    displayName: "Tra cứu tiệc cưới",
    moduleKey: "BOOKING",
    functionalGroup: "NGHIEP_VU",
    description: "Search, filter, and review wedding bookings."
  },
  {
    id: 4,
    code: "INVOICE_VIEW",
    displayName: "Xem hóa đơn thanh toán",
    moduleKey: "BILLING",
    functionalGroup: "NGHIEP_VU",
    description: "Browse and inspect created payment invoices."
  },
  {
    id: 5,
    code: "INVOICE_CREATE",
    displayName: "Lập hóa đơn thanh toán",
    moduleKey: "BILLING",
    functionalGroup: "NGHIEP_VU",
    description: "Generate final payment invoices for bookings."
  },
  {
    id: 6,
    code: "DEPOSIT_RECEIPT_CREATE",
    displayName: "Lập phiếu thu tiền cọc",
    moduleKey: "BILLING",
    functionalGroup: "NGHIEP_VU",
    description: "Issue deposit receipts during booking intake."
  },
  {
    id: 7,
    code: "INCIDENTAL_RECEIPT_CREATE",
    displayName: "Lập phiếu ghi nhận dịch vụ phát sinh",
    moduleKey: "BILLING",
    functionalGroup: "NGHIEP_VU",
    description: "Record incidental services after the original booking."
  },
  {
    id: 8,
    code: "CANCELLATION_RECEIPT_CREATE",
    displayName: "Lập phiếu hủy tiệc cưới",
    moduleKey: "BILLING",
    functionalGroup: "NGHIEP_VU",
    description: "Process cancellation receipts and release the booked slot."
  },
  {
    id: 9,
    code: "MONTHLY_REVENUE_REPORT_GENERATE",
    displayName: "Lập báo cáo doanh số tháng",
    moduleKey: "REPORTING",
    functionalGroup: "NGHIEP_VU",
    description: "Generate and inspect monthly revenue reports."
  },
  {
    id: 10,
    code: "HALL_TYPE_MANAGE",
    displayName: "Quản lý danh mục loại sảnh",
    moduleKey: "CATALOG",
    functionalGroup: "NGHIEP_VU",
    description: "Maintain hall type catalog definitions."
  },
  {
    id: 11,
    code: "SHIFT_MANAGE",
    displayName: "Quản lý danh mục ca",
    moduleKey: "CATALOG",
    functionalGroup: "NGHIEP_VU",
    description: "Maintain hall shift definitions and schedules."
  },
  {
    id: 12,
    code: "MENU_ITEM_MANAGE",
    displayName: "Quản lý danh mục món ăn",
    moduleKey: "CATALOG",
    functionalGroup: "NGHIEP_VU",
    description: "Maintain the menu item catalog."
  },
  {
    id: 13,
    code: "SERVICE_MANAGE",
    displayName: "Quản lý danh mục dịch vụ",
    moduleKey: "CATALOG",
    functionalGroup: "NGHIEP_VU",
    description: "Maintain the additional services catalog."
  },
  {
    id: 14,
    code: "STAFF_ACCOUNT_MANAGE",
    displayName: "Quản lý tài khoản nhân viên",
    moduleKey: "SYSTEM",
    functionalGroup: "HE_THONG",
    description: "Create, update, and disable staff accounts."
  },
  {
    id: 15,
    code: "USER_GROUP_MANAGE",
    displayName: "Quản lý nhóm người dùng",
    moduleKey: "SYSTEM",
    functionalGroup: "HE_THONG",
    description: "Manage user groups and their permission boundaries."
  },
  {
    id: 16,
    code: "SYSTEM_RULE_MANAGE",
    displayName: "Thay đổi quy định",
    moduleKey: "SYSTEM",
    functionalGroup: "NGHIEP_VU",
    description: "Update system parameters that change business rules."
  },
  {
    id: 17,
    code: "AUDIT_LOG_VIEW",
    displayName: "Quản lý nhật ký hệ thống",
    moduleKey: "SYSTEM",
    functionalGroup: "HE_THONG",
    description: "Read immutable audit log records."
  }
];

export const userGroupSeeds: UserGroup[] = [
  {
    id: "group-admin",
    name: "ADMIN",
    systemGroup: true,
    description: "System administration group with full RBAC ownership."
  },
  {
    id: "group-staff",
    name: "STAFF",
    systemGroup: true,
    description: "Default operational group. No implicit permissions are granted."
  }
];

export const adminSeedAccount: UserAccountSeed = {
  id: "user-admin-local",
  username: "admin",
  fullName: "Local Administrator",
  groupName: "ADMIN",
  status: "ACTIVE",
  notes: "Development seed account expected from the backend Flyway data foundation."
};

export const groupPermissionSeeds: GroupPermissionState[] = [
  {
    groupId: "group-admin",
    groupName: "ADMIN",
    permissionCodes: permissionCatalog.map((permission) => permission.code)
  },
  {
    groupId: "group-staff",
    groupName: "STAFF",
    permissionCodes: []
  }
];

export const lockedApiEndpoints = [
  "POST /api/auth/login",
  "POST /api/auth/refresh",
  "POST /api/auth/logout",
  "GET /api/auth/me",
  "GET /api/permissions",
  "GET /api/groups",
  "GET /api/groups/{groupId}/permissions",
  "POST /api/permissions/assign",
  "DELETE /api/permissions/revoke"
] as const;

export const manualVerificationChecklist = [
  "Verify permissions contains exactly 17 rows after the backend seed runs.",
  "Verify user_groups contains ADMIN and STAFF only for the initial RBAC setup.",
  "Verify ADMIN receives all 17 permissions and STAFF receives none by default.",
  "Verify the local admin account exists, is active, and can read /api/auth/me.",
  "Verify refresh-token rotation returns a new token pair and invalidates reused refresh tokens.",
  "Verify logout revokes the active session family and blocks the next secured request immediately.",
  "Verify a permission assignment affects the next authenticated request without a new JWT.",
  "Verify non-ADMIN users receive 403 for assign and revoke endpoints."
] as const;
