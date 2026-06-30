-- V5 standardizes the schema table names to English snake_case.
-- This migration intentionally renames tables and user-defined indexes only.
-- Column names and constraint names remain unchanged for now to keep the change set focused and lower-risk.

alter table loaisanh rename to hall_types;
alter table sanh rename to halls;
alter table ca rename to shifts;
alter table monan rename to menu_items;
alter table dichvu rename to services;
alter table chucnang rename to permissions;
alter table nhomnguoidung rename to user_groups;
alter table nguoidung rename to users;
alter table bangphanquyen rename to group_permissions;
alter table thamso rename to system_parameters;
alter table tieccuoi rename to wedding_bookings;
alter table ct_monan rename to booking_menu_items;
alter table ct_dichvu rename to booking_services;
alter table phieuthutiencoc rename to deposit_receipts;
alter table phieuphatsinh rename to incidental_receipts;
alter table ct_phatsinh rename to incidental_receipt_items;
alter table hoadon rename to invoices;
alter table phieuhuy rename to cancellation_receipts;
alter table baocaodoanhso rename to monthly_revenue_reports;
alter table ct_baocaods rename to monthly_revenue_report_items;

alter index idx_sanh_loaisanh_id rename to idx_halls_loaisanh_id;
alter index idx_monan_trang_thai rename to idx_menu_items_trang_thai;
alter index idx_dichvu_trang_thai rename to idx_services_trang_thai;
alter index idx_nguoidung_nhomnguoidung_id rename to idx_users_nhomnguoidung_id;
alter index idx_tieccuoi_sanh_id rename to idx_wedding_bookings_sanh_id;
alter index idx_tieccuoi_ca_id rename to idx_wedding_bookings_ca_id;
alter index idx_tieccuoi_ngay_dai_tiec rename to idx_wedding_bookings_ngay_dai_tiec;
alter index idx_tieccuoi_trang_thai rename to idx_wedding_bookings_trang_thai;
alter index ux_tieccuoi_active_slot rename to ux_wedding_bookings_active_slot;
alter index idx_ct_monan_tieccuoi_id rename to idx_booking_menu_items_tieccuoi_id;
alter index idx_ct_dichvu_tieccuoi_id rename to idx_booking_services_tieccuoi_id;
alter index idx_phieuthutiencoc_nguoidung_id rename to idx_deposit_receipts_nguoidung_id;
alter index idx_phieuphatsinh_tieccuoi_id rename to idx_incidental_receipts_tieccuoi_id;
alter index idx_phieuphatsinh_nguoidung_id rename to idx_incidental_receipts_nguoidung_id;
alter index idx_ct_phatsinh_phieuphatsinh_id rename to idx_incidental_receipt_items_phieuphatsinh_id;
alter index idx_hoadon_nguoidung_id rename to idx_invoices_nguoidung_id;
alter index idx_phieuhuy_nguoidung_id rename to idx_cancellation_receipts_nguoidung_id;
alter index idx_ct_baocaods_baocaodoanhso_id rename to idx_monthly_revenue_report_items_baocaodoanhso_id;
