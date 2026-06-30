-- V15 adds a read-only billing permission so invoice lookup/detail can be
-- granted independently from invoice creation.

insert into permissions (
  permission_code,
  display_name,
  module_key,
  description,
  functional_group
)
values (
  'INVOICE_VIEW',
  'Xem hóa đơn thanh toán',
  'BILLING',
  'Xem danh sách và chi tiết hóa đơn thanh toán của tiệc cưới.',
  'NGHIEP_VU'
)
on conflict (permission_code) do nothing;

insert into group_permissions (user_group_id, permission_id)
select
  admin_group.id,
  permission_item.id
from user_groups admin_group
join permissions permission_item on permission_item.permission_code = 'INVOICE_VIEW'
where admin_group.group_name = 'ADMIN'
on conflict do nothing;
