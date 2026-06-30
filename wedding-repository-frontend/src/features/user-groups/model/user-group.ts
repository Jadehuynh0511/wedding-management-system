export type UserGroup = {
  id: number;
  name: string;
  systemGroup: boolean;
  description: string;
};

export type GroupPermission = {
  groupId: number;
  permissionCodes: string[];
};

export type ApiPermission = {
  id: number;
  code: string;
  name: string;
  moduleKey: string;
  functionalGroup: string;
  description: string;
};

export const PERMISSION_SCREEN_MAP: Record<string, string> = {
  HALL_MANAGE: "Danh sách sảnh, Thêm/Sửa/Xóa sảnh",
  HALL_TYPE_MANAGE: "Loại sảnh, Thêm/Sửa/Xóa loại sảnh",
  SHIFT_MANAGE: "Ca tiệc, Thêm/Sửa/Xóa ca tiệc",
  MENU_ITEM_MANAGE: "Danh sách món ăn, Thêm/Sửa/Xóa món ăn",
  SERVICE_MANAGE: "Danh sách dịch vụ, Thêm/Sửa/Xóa dịch vụ",
  WEDDING_BOOKING_CREATE: "Danh sách tiệc, Thêm/Sửa tiệc",
  WEDDING_BOOKING_VIEW: "Tra cứu tiệc cưới",
  INVOICE_VIEW: "Danh sách hóa đơn, Chi tiết hóa đơn",
  DEPOSIT_RECEIPT_CREATE: "Phiếu thu cọc",
  INCIDENTAL_RECEIPT_CREATE: "Phiếu dịch vụ phát sinh",
  INVOICE_CREATE: "Lập hóa đơn thanh toán",
  CANCELLATION_RECEIPT_CREATE: "Phiếu hủy tiệc",
  MONTHLY_REVENUE_REPORT_GENERATE: "Báo cáo doanh số tháng",
  STAFF_ACCOUNT_MANAGE: "Quản lý nhân viên",
  USER_GROUP_MANAGE: "Quản lý nhóm người dùng",
  SYSTEM_RULE_MANAGE: "Thay đổi quy định",
  AUDIT_LOG_VIEW: "Nhật ký hệ thống",
};

export const FUNCTIONAL_GROUP_LABEL: Record<string, string> = {
  NGHIEP_VU: "Nghiệp vụ",
  HE_THONG: "Hệ thống",
};

export const MODULE_KEY_LABEL: Record<string, string> = {
  CATALOG: "Danh mục",
  BOOKING: "Tiệc cưới",
  BILLING: "Thanh toán",
  REPORTING: "Báo cáo",
  SYSTEM: "Hệ thống",
};
