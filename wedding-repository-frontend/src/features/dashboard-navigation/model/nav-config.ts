import type { UserGroupName } from "@/entities/user-group/model/user-group";

export const ROUTE_LABEL_VI: Record<string, string> = {
  "/dashboard": "Trang chủ quản lý",
  "/dashboard/halls": "Quản lý sảnh",
  "/dashboard/hall-types": "Loại sảnh",
  "/dashboard/shifts": "Ca tiệc",
  "/dashboard/menu-items": "Món ăn",
  "/dashboard/services": "Dịch vụ",
  "/dashboard/bookings/new": "Tiệc cưới",
  "/dashboard/bookings": "Tra cứu tiệc cưới",
  "/dashboard/incidentals": "Dịch vụ phát sinh",
  "/dashboard/incidentals/new": "Lập phiếu phát sinh",
  "/dashboard/invoices": "Hóa đơn",
  "/dashboard/invoices/new": "Lập hóa đơn",
  "/dashboard/cancellations/new": "Phiếu hủy tiệc",
  "/dashboard/reports/monthly": "Báo cáo doanh số",
  "/dashboard/staff": "Nhân viên",
  "/dashboard/groups": "Nhóm người dùng",
  "/rbac-foundation": "Phân quyền",
  "/dashboard/audit-logs": "Nhật ký hệ thống",
  "/dashboard/system-rules": "Thay đổi quy định",
};

export const SIDEBAR_ROUTE_ORDER: string[] = [
  "/dashboard",
  "/dashboard/halls",
  "/dashboard/hall-types",
  "/dashboard/shifts",
  "/dashboard/menu-items",
  "/dashboard/services",
  "/dashboard/bookings/new",
  "/dashboard/bookings",
  "/dashboard/incidentals",
  "/dashboard/invoices",
  "/dashboard/cancellations/new",
  "/dashboard/reports/monthly",
  "/dashboard/staff",
  "/dashboard/groups",
  "/rbac-foundation",
  "/dashboard/audit-logs",
  "/dashboard/system-rules",
];

export const GROUP_DISPLAY_NAME: Record<UserGroupName, string> = {
  ADMIN: "Quản trị viên",
  STAFF: "Nhân viên",
};
