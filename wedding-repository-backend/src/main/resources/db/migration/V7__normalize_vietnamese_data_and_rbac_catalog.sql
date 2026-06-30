-- V7 rewrites seeded text to proper Vietnamese with accents and aligns the RBAC catalog
-- with the locked 16-permission specification after the English column refactor in V6.

update hall_types
set description = seed.description
from (
  values
    ('Ruby', 'Loại sảnh tiêu chuẩn, phù hợp tiệc quy mô vừa.'),
    ('Sapphire', 'Loại sảnh nâng cấp với trang trí hiện đại.'),
    ('Emerald', 'Loại sảnh cao cấp cho tiệc cưới sang trọng.'),
    ('Diamond', 'Loại sảnh premium với sân khấu và ánh sáng lớn.'),
    ('Royal', 'Loại sảnh VIP cho tiệc cưới quy mô lớn.')
) as seed(hall_type_name, description)
where hall_types.hall_type_name = seed.hall_type_name;

update shifts
set shift_name = seed.shift_name,
    description = seed.description
from (
  values
    ('Ca trua', 'Ca trưa', 'Khung giờ trưa chuẩn cho tiệc cưới.'),
    ('Ca toi', 'Ca tối', 'Khung giờ tối chuẩn cho tiệc cưới.')
) as seed(legacy_shift_name, shift_name, description)
where shifts.shift_name = seed.legacy_shift_name;

update services
set service_name = seed.service_name,
    service_category = seed.service_category,
    unit_name = seed.unit_name,
    description = seed.description
from (
  values
    ('Trang tri cong hoa', 'Trang trí cổng hoa', 'Trang trí', 'gói', 'Gói cổng hoa tiêu chuẩn.'),
    ('Trang tri san khau', 'Trang trí sân khấu', 'Trang trí', 'gói', 'Backdrop và sân khấu cưới.'),
    ('Trang tri ban gallery', 'Trang trí bàn gallery', 'Trang trí', 'gói', 'Khu vực trưng bày ảnh cưới.'),
    ('Trang tri ban tiec', 'Trang trí bàn tiệc', 'Trang trí', 'gói', 'Setup trung tâm bàn và khăn phủ.'),
    ('MC chuong trinh', 'MC chương trình', 'Nghi lễ', 'gói', 'MC dẫn chương trình tiệc.'),
    ('Ban nhac acoustic', 'Ban nhạc acoustic', 'Giải trí', 'gói', 'Ban nhạc biểu diễn trong tiệc.'),
    ('Mua mo man', 'Múa mở màn', 'Giải trí', 'gói', 'Tiết mục mở màn sân khấu.'),
    ('Ao thuat giao luu', 'Ảo thuật giao lưu', 'Giải trí', 'gói', 'Hoạt náo và giao lưu cùng khách mời.'),
    ('Goi am thanh anh sang', 'Gói âm thanh ánh sáng', 'Kỹ thuật', 'gói', 'Âm thanh và đèn cơ bản.'),
    ('Man hinh LED', 'Màn hình LED', 'Kỹ thuật', 'gói', 'Màn hình LED trình chiếu.'),
    ('Quay phim full HD', 'Quay phim full HD', 'Media', 'gói', 'Quay phim toàn bộ sự kiện.'),
    ('Chup anh phong su', 'Chụp ảnh phóng sự', 'Media', 'gói', 'Chụp ảnh phóng sự cưới.'),
    ('Photobooth', 'Photobooth', 'Media', 'gói', 'Khu vực chụp ảnh lưu niệm.'),
    ('Banh cuoi', 'Bánh cưới', 'Ẩm thực bổ sung', 'chiếc', 'Bánh cưới nhiều tầng.'),
    ('Thap champagne', 'Tháp champagne', 'Ẩm thực bổ sung', 'gói', 'Setup tháp champagne.'),
    ('Phao hoa lanh', 'Pháo hoa lạnh', 'Hiệu ứng', 'gói', 'Hiệu ứng sân khấu an toàn trong nhà.'),
    ('Xe dua don co dau', 'Xe đưa đón cô dâu', 'Phương tiện', 'gói', 'Xe đưa đón trong ngày cưới.'),
    ('In thiep moi', 'In thiệp mời', 'In ấn', 'gói', 'Thiệp mời tiêu chuẩn cho khách mời.'),
    ('Hoa cam tay co dau', 'Hoa cầm tay cô dâu', 'Hoa tươi', 'bó', 'Hoa cầm tay theo concept tiệc.'),
    ('Qua cam on khach', 'Quà cảm ơn khách', 'Quà tặng', 'gói', 'Quà tặng nhỏ cho khách tham dự.')
) as seed(legacy_service_name, service_name, service_category, unit_name, description)
where services.service_name = seed.legacy_service_name;

update menu_items
set item_name = format('Món ăn %s', lpad(id::text, 3, '0')),
    item_category = case (id - 1) % 5
      when 0 then 'Khai vị'
      when 1 then 'Món chính'
      when 2 then 'Lẩu'
      when 3 then 'Tráng miệng'
      else 'Đồ uống'
    end,
    description = 'Dữ liệu mẫu khởi tạo cho danh mục món ăn.'
where item_name like 'Mon an %';

update permissions
set permission_code = seed.permission_code,
    display_name = seed.display_name,
    module_key = seed.module_key,
    description = seed.description
from (
  values
    ('HALL_MANAGE', 'HALL_MANAGE', 'Tiếp nhận sảnh', 'CATALOG', 'Quản lý danh sách sảnh và trạng thái sử dụng.'),
    ('WEDDING_BOOKING_CREATE', 'WEDDING_BOOKING_CREATE', 'Nhận đặt tiệc cưới', 'BOOKING', 'Tạo và xử lý phiếu đặt tiệc cưới.'),
    ('WEDDING_BOOKING_VIEW', 'WEDDING_BOOKING_VIEW', 'Tra cứu tiệc cưới', 'BOOKING', 'Xem và tra cứu danh sách tiệc cưới.'),
    ('INVOICE_CREATE', 'INVOICE_CREATE', 'Lập hóa đơn thanh toán', 'BILLING', 'Tạo hóa đơn thanh toán cho tiệc cưới.'),
    ('REVENUE_REPORT_VIEW', 'MONTHLY_REVENUE_REPORT_GENERATE', 'Lập báo cáo doanh số tháng', 'REPORTING', 'Xem và lập báo cáo doanh số tháng.'),
    ('HALL_TYPE_MANAGE', 'HALL_TYPE_MANAGE', 'Quản lý danh mục loại sảnh', 'CATALOG', 'Quản lý danh mục loại sảnh.'),
    ('SHIFT_MANAGE', 'SHIFT_MANAGE', 'Quản lý danh mục ca', 'CATALOG', 'Quản lý khung giờ tiệc cưới.'),
    ('MENU_ITEM_MANAGE', 'MENU_ITEM_MANAGE', 'Quản lý danh mục món ăn', 'CATALOG', 'Quản lý danh mục món ăn.'),
    ('SERVICE_MANAGE', 'SERVICE_MANAGE', 'Quản lý danh mục dịch vụ', 'CATALOG', 'Quản lý danh mục dịch vụ.'),
    ('DEPOSIT_RECEIPT_CREATE', 'DEPOSIT_RECEIPT_CREATE', 'Lập phiếu thu tiền cọc', 'BILLING', 'Tạo phiếu thu tiền cọc.'),
    ('INCIDENTAL_RECEIPT_CREATE', 'INCIDENTAL_RECEIPT_CREATE', 'Lập phiếu ghi nhận dịch vụ phát sinh', 'BILLING', 'Tạo phiếu ghi nhận dịch vụ phát sinh.'),
    ('CANCELLATION_RECEIPT_CREATE', 'CANCELLATION_RECEIPT_CREATE', 'Lập phiếu hủy tiệc cưới', 'BILLING', 'Tạo phiếu hủy tiệc cưới.'),
    ('SYSTEM_RULE_MANAGE', 'SYSTEM_RULE_MANAGE', 'Thay đổi quy định', 'SYSTEM', 'Quản lý các tham số quy định hệ thống.'),
    ('STAFF_ACCOUNT_MANAGE', 'STAFF_ACCOUNT_MANAGE', 'Quản lý tài khoản nhân viên', 'SYSTEM', 'Quản lý tài khoản nhân viên.'),
    ('USER_GROUP_MANAGE', 'USER_GROUP_MANAGE', 'Quản lý nhóm người dùng', 'SYSTEM', 'Quản lý nhóm người dùng và cơ cấu phân quyền.'),
    ('AUDIT_LOG_VIEW', 'AUDIT_LOG_VIEW', 'Quản lý nhật ký hệ thống', 'SYSTEM', 'Xem nhật ký hệ thống ở chế độ chỉ đọc.')
) as seed(legacy_permission_code, permission_code, display_name, module_key, description)
where permissions.permission_code = seed.legacy_permission_code;

update user_groups
set description = seed.description
from (
  values
    ('ADMIN', 'Nhóm quản trị hệ thống có toàn quyền RBAC.'),
    ('STAFF', 'Nhóm nhân viên nghiệp vụ mặc định.')
) as seed(group_name, description)
where user_groups.group_name = seed.group_name;

update users
set full_name = 'Quản trị viên hệ thống'
where username = 'admin'
  and full_name = 'System Administrator';
