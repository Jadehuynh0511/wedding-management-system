-- THAMSO is modeled as a singleton row so the application can load the active business rules in one query.
insert into thamso (
  id,
  ti_le_coc_toi_thieu,
  phat_tre_enabled,
  ti_le_phat_tre,
  so_ngay_huy,
  ti_le_hoan_coc
)
values
  (1, 50.00, true, 1.00, 15, 50.00);
