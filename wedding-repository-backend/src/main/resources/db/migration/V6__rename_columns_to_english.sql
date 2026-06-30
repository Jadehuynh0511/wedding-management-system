-- V6 continues the English schema refactor by renaming legacy Vietnamese columns after V5 completed the table rename.
-- Keeping this in a new migration preserves Flyway history for databases that already applied V4 and V5.

alter table hall_types rename column ten_loai_sanh to hall_type_name;
alter table hall_types rename column don_gia_ban_toi_thieu to minimum_table_price;
alter table hall_types rename column mo_ta to description;

alter table halls rename column loaisanh_id to hall_type_id;
alter table halls rename column ten_sanh to hall_name;
alter table halls rename column suc_chua_toi_da to max_capacity;
alter table halls rename column don_gia_ban to table_price;
alter table halls rename column trang_thai to status;
alter table halls rename column mo_ta to description;

alter table shifts rename column ten_ca to shift_name;
alter table shifts rename column gio_bat_dau to start_time;
alter table shifts rename column gio_ket_thuc to end_time;
alter table shifts rename column mo_ta to description;

alter table menu_items rename column ten_mon_an to item_name;
alter table menu_items rename column loai_mon to item_category;
alter table menu_items rename column don_gia_hien_tai to current_price;
alter table menu_items rename column trang_thai to status;
alter table menu_items rename column mo_ta to description;

alter table services rename column ten_dich_vu to service_name;
alter table services rename column loai_dich_vu to service_category;
alter table services rename column don_vi_tinh to unit_name;
alter table services rename column don_gia_hien_tai to current_price;
alter table services rename column trang_thai to status;
alter table services rename column mo_ta to description;

alter table permissions rename column ma_chuc_nang to permission_code;
alter table permissions rename column ten_chuc_nang to display_name;
alter table permissions rename column mo_ta to description;

alter table user_groups rename column ten_nhom to group_name;
alter table user_groups rename column mo_ta to description;
alter table user_groups rename column la_nhom_he_thong to system_group;

alter table users rename column nhomnguoidung_id to user_group_id;
alter table users rename column ten_dang_nhap to username;
alter table users rename column mat_khau_hash to password_hash;
alter table users rename column ho_ten to full_name;
alter table users rename column so_dien_thoai to phone_number;
alter table users rename column trang_thai to status;

alter table group_permissions rename column nhomnguoidung_id to user_group_id;
alter table group_permissions rename column chucnang_id to permission_id;

alter table system_parameters rename column ti_le_coc_toi_thieu to minimum_deposit_percentage;
alter table system_parameters rename column phat_tre_enabled to late_payment_penalty_enabled;
alter table system_parameters rename column ti_le_phat_tre to late_payment_penalty_rate;
alter table system_parameters rename column so_ngay_huy to cancellation_deadline_days;
alter table system_parameters rename column ti_le_hoan_coc to deposit_refund_percentage;

alter table wedding_bookings rename column sanh_id to hall_id;
alter table wedding_bookings rename column ca_id to shift_id;
alter table wedding_bookings rename column ten_chu_re to groom_name;
alter table wedding_bookings rename column ten_co_dau to bride_name;
alter table wedding_bookings rename column so_dien_thoai_chu_re to groom_phone_number;
alter table wedding_bookings rename column so_dien_thoai_co_dau to bride_phone_number;
alter table wedding_bookings rename column ngay_dat_tiec to booking_date;
alter table wedding_bookings rename column ngay_dai_tiec to celebration_date;
alter table wedding_bookings rename column so_luong_ban to table_count;
alter table wedding_bookings rename column so_ban_du_tru to reserved_table_count;
alter table wedding_bookings rename column don_gia_ban to table_price;
alter table wedding_bookings rename column trang_thai to status;
alter table wedding_bookings rename column ghi_chu to notes;

alter table booking_menu_items rename column tieccuoi_id to wedding_booking_id;
alter table booking_menu_items rename column monan_id to menu_item_id;
alter table booking_menu_items rename column so_luong to quantity;
alter table booking_menu_items rename column don_gia_snapshot to price_snapshot;
alter table booking_menu_items rename column thanh_tien to line_total;
alter table booking_menu_items rename column ghi_chu to notes;

alter table booking_services rename column tieccuoi_id to wedding_booking_id;
alter table booking_services rename column dichvu_id to service_id;
alter table booking_services rename column so_luong to quantity;
alter table booking_services rename column don_gia_snapshot to price_snapshot;
alter table booking_services rename column thanh_tien to line_total;
alter table booking_services rename column ghi_chu to notes;

alter table deposit_receipts rename column tieccuoi_id to wedding_booking_id;
alter table deposit_receipts rename column nguoidung_id to user_id;
alter table deposit_receipts rename column thoi_diem_thu to received_at;
alter table deposit_receipts rename column so_tien to amount;
alter table deposit_receipts rename column phuong_thuc_thanh_toan to payment_method;
alter table deposit_receipts rename column ghi_chu to notes;

alter table incidental_receipts rename column tieccuoi_id to wedding_booking_id;
alter table incidental_receipts rename column nguoidung_id to user_id;
alter table incidental_receipts rename column thoi_diem_lap to recorded_at;
alter table incidental_receipts rename column tong_tien to total_amount;
alter table incidental_receipts rename column ghi_chu to notes;

alter table incidental_receipt_items rename column phieuphatsinh_id to incidental_receipt_id;
alter table incidental_receipt_items rename column dichvu_id to service_id;
alter table incidental_receipt_items rename column so_luong to quantity;
alter table incidental_receipt_items rename column don_gia_ap_dung to applied_unit_price;
alter table incidental_receipt_items rename column thanh_tien to line_total;
alter table incidental_receipt_items rename column ghi_chu to notes;

alter table invoices rename column tieccuoi_id to wedding_booking_id;
alter table invoices rename column nguoidung_id to user_id;
alter table invoices rename column thoi_diem_thanh_toan to paid_at;
alter table invoices rename column tong_tien_ban to hall_total_amount;
alter table invoices rename column tong_tien_mon_an to menu_items_total_amount;
alter table invoices rename column tong_tien_dich_vu to services_total_amount;
alter table invoices rename column tong_tien_phat_sinh to incidentals_total_amount;
alter table invoices rename column tien_dat_coc to deposit_amount;
alter table invoices rename column tien_phat_tre to late_payment_penalty_amount;
alter table invoices rename column tong_thanh_toan to final_amount;
alter table invoices rename column ghi_chu to notes;

alter table cancellation_receipts rename column tieccuoi_id to wedding_booking_id;
alter table cancellation_receipts rename column nguoidung_id to user_id;
alter table cancellation_receipts rename column thoi_diem_huy to cancelled_at;
alter table cancellation_receipts rename column so_ngay_truoc_tiec to days_before_celebration;
alter table cancellation_receipts rename column ti_le_hoan_coc_ap_dung to applied_deposit_refund_percentage;
alter table cancellation_receipts rename column tien_hoan to refund_amount;
alter table cancellation_receipts rename column ly_do to reason;

alter table monthly_revenue_reports rename column thang to report_month;
alter table monthly_revenue_reports rename column nam to report_year;
alter table monthly_revenue_reports rename column tong_doanh_thu to total_revenue;
alter table monthly_revenue_reports rename column tong_so_tiec to total_wedding_bookings;
alter table monthly_revenue_reports rename column thoi_diem_tao to generated_at;

alter table monthly_revenue_report_items rename column baocaodoanhso_id to monthly_revenue_report_id;
alter table monthly_revenue_report_items rename column ngay_bao_cao to report_date;
alter table monthly_revenue_report_items rename column so_luong_tiec to wedding_booking_count;
alter table monthly_revenue_report_items rename column doanh_thu to revenue;
alter table monthly_revenue_report_items rename column ti_le to revenue_ratio;

alter index idx_halls_loaisanh_id rename to idx_halls_hall_type_id;
alter index idx_menu_items_trang_thai rename to idx_menu_items_status;
alter index idx_services_trang_thai rename to idx_services_status;
alter index idx_users_nhomnguoidung_id rename to idx_users_user_group_id;
alter index idx_wedding_bookings_sanh_id rename to idx_wedding_bookings_hall_id;
alter index idx_wedding_bookings_ca_id rename to idx_wedding_bookings_shift_id;
alter index idx_wedding_bookings_ngay_dai_tiec rename to idx_wedding_bookings_celebration_date;
alter index idx_wedding_bookings_trang_thai rename to idx_wedding_bookings_status;
alter index idx_booking_menu_items_tieccuoi_id rename to idx_booking_menu_items_wedding_booking_id;
alter index idx_booking_services_tieccuoi_id rename to idx_booking_services_wedding_booking_id;
alter index idx_deposit_receipts_nguoidung_id rename to idx_deposit_receipts_user_id;
alter index idx_incidental_receipts_tieccuoi_id rename to idx_incidental_receipts_wedding_booking_id;
alter index idx_incidental_receipts_nguoidung_id rename to idx_incidental_receipts_user_id;
alter index idx_incidental_receipt_items_phieuphatsinh_id rename to idx_incidental_receipt_items_receipt_id;
alter index idx_invoices_nguoidung_id rename to idx_invoices_user_id;
alter index idx_cancellation_receipts_nguoidung_id rename to idx_cancellation_receipts_user_id;
alter index idx_monthly_revenue_report_items_baocaodoanhso_id rename to idx_monthly_revenue_report_items_report_id;
