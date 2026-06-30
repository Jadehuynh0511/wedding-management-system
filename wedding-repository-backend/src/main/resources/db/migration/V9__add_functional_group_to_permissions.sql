-- V9 stores functional_group directly in the permission catalog so phase 4 APIs can
-- return the full locked RBAC metadata from DB instead of hardcoding it in code.

alter table permissions
add column functional_group varchar(20);

update permissions
set functional_group = case
    when permission_code in ('STAFF_ACCOUNT_MANAGE', 'USER_GROUP_MANAGE', 'AUDIT_LOG_VIEW') then 'HE_THONG'
    else 'NGHIEP_VU'
end;

alter table permissions
alter column functional_group set not null;

alter table permissions
add constraint chk_permissions_functional_group
check (functional_group in ('NGHIEP_VU', 'HE_THONG'));
