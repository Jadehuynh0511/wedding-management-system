import type { ModuleKey, PermissionCode } from "@/entities/permission/model/permission";
import type { UserGroupName } from "@/entities/user-group/model/user-group";

export type DashboardRouteManifestItem = {
  href: string;
  label: string;
  description: string;
  moduleKey: ModuleKey;
  availability?: "ready" | "planned";
  requiredPermission?: PermissionCode;
  requiredGroup?: UserGroupName;
};

// The RBAC admin center is guarded by the ADMIN group rule from QD11, not by a catalog permission.
export const dashboardRouteManifest: DashboardRouteManifestItem[] = [
  {
    href: "/dashboard/halls",
    label: "Halls",
    description: "Manage hall inventory and reception availability.",
    moduleKey: "CATALOG",
    availability: "ready",
    requiredPermission: "HALL_MANAGE"
  },
  {
    href: "/dashboard/bookings/new",
    label: "New booking",
    description: "Create wedding bookings and capture booking details.",
    moduleKey: "BOOKING",
    availability: "ready",
    requiredPermission: "WEDDING_BOOKING_CREATE"
  },
  {
    href: "/dashboard/bookings",
    label: "Booking lookup",
    description: "Search and review existing wedding bookings.",
    moduleKey: "BOOKING",
    requiredPermission: "WEDDING_BOOKING_VIEW"
  },
  {
    href: "/dashboard/invoices",
    label: "Invoice list",
    description: "Browse payment invoice candidates and created invoices.",
    moduleKey: "BILLING",
    requiredPermission: "INVOICE_VIEW"
  },
  {
    href: "/dashboard/invoices/new",
    label: "Invoice",
    description: "Generate settlement invoices for completed parties.",
    moduleKey: "BILLING",
    requiredPermission: "INVOICE_CREATE"
  },
  {
    href: "/dashboard/incidentals",
    label: "Incidental receipt list",
    description: "Browse wedding bookings and review recorded incidental services.",
    moduleKey: "BILLING",
    requiredPermission: "INCIDENTAL_RECEIPT_CREATE"
  },
  {
    href: "/dashboard/incidentals/new",
    label: "Incidental receipt",
    description: "Record additional services consumed after booking.",
    moduleKey: "BILLING",
    requiredPermission: "INCIDENTAL_RECEIPT_CREATE"
  },
  {
    href: "/dashboard/cancellations/new",
    label: "Cancellation receipt",
    description: "Process cancellations and release hall/shift availability.",
    moduleKey: "BILLING",
    requiredPermission: "CANCELLATION_RECEIPT_CREATE"
  },
  {
    href: "/dashboard/reports/monthly",
    label: "Monthly revenue",
    description: "Generate and inspect monthly revenue reports.",
    moduleKey: "REPORTING",
    requiredPermission: "MONTHLY_REVENUE_REPORT_GENERATE"
  },
  {
    href: "/dashboard/hall-types",
    label: "Hall types",
    description: "Maintain hall type catalog entries.",
    moduleKey: "CATALOG",
    availability: "ready",
    requiredPermission: "HALL_TYPE_MANAGE"
  },
  {
    href: "/dashboard/shifts",
    label: "Shifts",
    description: "Maintain shift definitions and constraints.",
    moduleKey: "CATALOG",
    availability: "ready",
    requiredPermission: "SHIFT_MANAGE"
  },
  {
    href: "/dashboard/menu-items",
    label: "Menu items",
    description: "Maintain wedding menu catalog entries.",
    moduleKey: "CATALOG",
    availability: "ready",
    requiredPermission: "MENU_ITEM_MANAGE"
  },
  {
    href: "/dashboard/services",
    label: "Services",
    description: "Maintain the add-on service catalog.",
    moduleKey: "CATALOG",
    availability: "ready",
    requiredPermission: "SERVICE_MANAGE"
  },
  {
    href: "/dashboard/staff",
    label: "Staff accounts",
    description: "Create and manage staff login accounts.",
    moduleKey: "SYSTEM",
    requiredPermission: "STAFF_ACCOUNT_MANAGE"
  },
  {
    href: "/dashboard/groups",
    label: "User groups",
    description: "Maintain user groups and their RBAC boundaries.",
    moduleKey: "SYSTEM",
    availability: "ready",
    requiredPermission: "USER_GROUP_MANAGE"
  },
  {
    href: "/dashboard/system-rules",
    label: "System rules",
    description: "Update system parameters that drive business rules.",
    moduleKey: "SYSTEM",
    availability: "ready",
    requiredPermission: "SYSTEM_RULE_MANAGE"
  },
  {
    href: "/dashboard/audit-logs",
    label: "Audit logs",
    description: "Read immutable system activity history.",
    moduleKey: "SYSTEM",
    availability: "ready",
    requiredPermission: "AUDIT_LOG_VIEW"
  },
  {
    href: "/rbac-foundation",
    label: "RBAC foundation",
    description: "Inspect the locked M1 permission catalog and route guard plan.",
    moduleKey: "SYSTEM",
    availability: "ready",
    requiredGroup: "ADMIN"
  }
];
