-- V4 seeds the dynamic RBAC baseline for milestone M1.

insert into chucnang (ma_chuc_nang, ten_chuc_nang, module_key, mo_ta)
values
  ('HALL_MANAGE', 'Tiep nhan sanh', 'CATALOG', 'Quan ly danh sach sanh va trang thai su dung.'),
  ('WEDDING_BOOKING_CREATE', 'Nhan dat tiec cuoi', 'BOOKING', 'Tao va xu ly phieu dat tiec cuoi.'),
  ('WEDDING_BOOKING_VIEW', 'Tra cuu tiec cuoi', 'BOOKING', 'Xem va tra cuu danh sach tiec cuoi.'),
  ('INVOICE_CREATE', 'Lap hoa don thanh toan', 'BILLING', 'Tao hoa don thanh toan cho tiec cuoi.'),
  ('REVENUE_REPORT_VIEW', 'Lap bao cao doanh so', 'REPORTING', 'Xem va tao bao cao doanh so thang.'),
  ('HALL_TYPE_MANAGE', 'Quan ly loai sanh', 'CATALOG', 'Quan ly danh muc loai sanh.'),
  ('SHIFT_MANAGE', 'Quan ly ca', 'CATALOG', 'Quan ly khung gio tiec cuoi.'),
  ('MENU_ITEM_MANAGE', 'Quan ly mon an', 'CATALOG', 'Quan ly danh muc mon an.'),
  ('SERVICE_MANAGE', 'Quan ly dich vu', 'CATALOG', 'Quan ly danh muc dich vu.'),
  ('DEPOSIT_RECEIPT_CREATE', 'Lap phieu thu tien coc', 'BILLING', 'Tao phieu thu tien coc.'),
  ('INCIDENTAL_RECEIPT_CREATE', 'Lap phieu dich vu phat sinh', 'BILLING', 'Tao phieu ghi nhan dich vu phat sinh.'),
  ('CANCELLATION_RECEIPT_CREATE', 'Lap phieu huy tiec', 'BILLING', 'Tao phieu huy tiec cuoi.'),
  ('SYSTEM_RULE_MANAGE', 'Thay doi quy dinh', 'SYSTEM', 'Quan ly cac tham so quy dinh he thong.'),
  ('STAFF_ACCOUNT_MANAGE', 'Quan ly tai khoan nhan vien', 'SYSTEM', 'Quan ly tai khoan nhan vien.'),
  ('USER_GROUP_MANAGE', 'Quan ly nhom nguoi dung', 'SYSTEM', 'Quan ly nhom nguoi dung va co cau phan quyen.'),
  ('AUDIT_LOG_VIEW', 'Xem nhat ky he thong', 'SYSTEM', 'Xem nhat ky he thong che do chi doc.')
on conflict (ma_chuc_nang) do nothing;

insert into nhomnguoidung (ten_nhom, mo_ta, la_nhom_he_thong)
values
  ('ADMIN', 'Nhom quan tri he thong co toan quyen RBAC.', true),
  ('STAFF', 'Nhom nhan vien nghiep vu mac dinh.', true)
on conflict (ten_nhom) do nothing;

insert into nguoidung (
  nhomnguoidung_id,
  ten_dang_nhap,
  mat_khau_hash,
  ho_ten,
  email,
  so_dien_thoai,
  trang_thai
)
select
  group_admin.id,
  'admin',
  '$2a$10$7EqJtq98hPqEX7fNZaFWoOHiA8nY6jwxKF1u9Ii.YTZGi4ZTSdK1e',
  'System Administrator',
  'admin@local.dev',
  '0900000000',
  'ACTIVE'
from nhomnguoidung group_admin
where group_admin.ten_nhom = 'ADMIN'
  and not exists (
    select 1
    from nguoidung existing_user
    where existing_user.ten_dang_nhap = 'admin'
  );

insert into bangphanquyen (nhomnguoidung_id, chucnang_id)
select
  group_admin.id,
  permission_item.id
from nhomnguoidung group_admin
cross join chucnang permission_item
where group_admin.ten_nhom = 'ADMIN'
on conflict do nothing;
