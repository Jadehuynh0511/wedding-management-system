-- V2 seeds the catalog data required by the plan:
-- 5 hall types, 2 sample sessions, 20 services and 100 foods.

insert into loaisanh (ten_loai_sanh, don_gia_ban_toi_thieu, mo_ta)
values
  ('Ruby', 3200000, 'Loại sảnh tiêu chuẩn, phù hợp tiệc quy mô vừa.'),
  ('Sapphire', 4200000, 'Loại sảnh nâng cấp với trang trí hiện đại.'),
  ('Emerald', 5500000, 'Loại sảnh cao cấp cho tiệc cưới sang trọng.'),
  ('Diamond', 6800000, 'Loại sảnh premium với sân khấu và ánh sáng lớn.'),
  ('Royal', 8200000, 'Loại sảnh VIP cho tiệc cưới quy mô lớn.');

insert into ca (ten_ca, gio_bat_dau, gio_ket_thuc, mo_ta)
values
  ('Ca trưa', '11:00', '15:00', 'Khung giờ trưa chuẩn cho tiệc cưới.'),
  ('Ca tối', '17:00', '21:30', 'Khung giờ tối chuẩn cho tiệc cưới.');

insert into dichvu (ten_dich_vu, loai_dich_vu, don_vi_tinh, don_gia_hien_tai, trang_thai, mo_ta)
values
  ('Trang trí cổng hoa', 'Trang trí', 'gói', 5000000, 'HOAT_DONG', 'Gói cổng hoa tiêu chuẩn.'),
  ('Trang trí sân khấu', 'Trang trí', 'gói', 12000000, 'HOAT_DONG', 'Backdrop và sân khấu cưới.'),
  ('Trang trí bàn gallery', 'Trang trí', 'gói', 2500000, 'HOAT_DONG', 'Khu vực trưng bày ảnh cưới.'),
  ('Trang trí bàn tiệc', 'Trang trí', 'gói', 2200000, 'HOAT_DONG', 'Setup trung tâm bàn và khăn phủ.'),
  ('MC chương trình', 'Nghi lễ', 'gói', 3500000, 'HOAT_DONG', 'MC dẫn chương trình tiệc.'),
  ('Ban nhạc acoustic', 'Giải trí', 'gói', 6500000, 'HOAT_DONG', 'Ban nhạc biểu diễn trong tiệc.'),
  ('Múa mở màn', 'Giải trí', 'gói', 7000000, 'HOAT_DONG', 'Tiết mục mở màn sân khấu.'),
  ('Ảo thuật giao lưu', 'Giải trí', 'gói', 5500000, 'HOAT_DONG', 'Hoạt náo và giao lưu cùng khách mời.'),
  ('Gói âm thanh ánh sáng', 'Kỹ thuật', 'gói', 8000000, 'HOAT_DONG', 'Âm thanh và đèn cơ bản.'),
  ('Màn hình LED', 'Kỹ thuật', 'gói', 10000000, 'HOAT_DONG', 'Màn hình LED trình chiếu.'),
  ('Quay phim full HD', 'Media', 'gói', 9000000, 'HOAT_DONG', 'Quay phim toàn bộ sự kiện.'),
  ('Chụp ảnh phóng sự', 'Media', 'gói', 8500000, 'HOAT_DONG', 'Chụp ảnh phóng sự cưới.'),
  ('Photobooth', 'Media', 'gói', 6000000, 'HOAT_DONG', 'Khu vực chụp ảnh lưu niệm.'),
  ('Bánh cưới', 'Ẩm thực bổ sung', 'chiếc', 3000000, 'HOAT_DONG', 'Bánh cưới nhiều tầng.'),
  ('Tháp champagne', 'Ẩm thực bổ sung', 'gói', 3500000, 'HOAT_DONG', 'Setup tháp champagne.'),
  ('Pháo hoa lạnh', 'Hiệu ứng', 'gói', 2800000, 'HOAT_DONG', 'Hiệu ứng sân khấu an toàn trong nhà.'),
  ('Xe đưa đón cô dâu', 'Phương tiện', 'gói', 4000000, 'HOAT_DONG', 'Xe đưa đón trong ngày cưới.'),
  ('In thiệp mời', 'In ấn', 'gói', 4500000, 'HOAT_DONG', 'Thiệp mời tiêu chuẩn cho khách mời.'),
  ('Hoa cầm tay cô dâu', 'Hoa tươi', 'bó', 800000, 'HOAT_DONG', 'Hoa cầm tay theo concept tiệc.'),
  ('Quà cảm ơn khách', 'Quà tặng', 'gói', 2500000, 'HOAT_DONG', 'Quà tặng nhỏ cho khách tham dự.');

-- generate_series keeps the 100-item seed deterministic and easy to maintain.
insert into monan (ten_mon_an, loai_mon, don_gia_hien_tai, trang_thai, mo_ta)
select
  format('Món ăn %s', lpad(gs::text, 3, '0')),
  case (gs - 1) % 5
    when 0 then 'Khai vị'
    when 1 then 'Món chính'
    when 2 then 'Lẩu'
    when 3 then 'Tráng miệng'
    else 'Đồ uống'
  end,
  (120000 + (gs * 5000))::numeric(18, 2),
  case
    when gs % 10 = 0 then 'HET'
    else 'CON'
  end,
  'Dữ liệu mẫu khởi tạo cho danh mục món ăn.'
from generate_series(1, 100) as gs;
