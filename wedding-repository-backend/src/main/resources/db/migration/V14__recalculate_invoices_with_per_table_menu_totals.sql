with recalculated_menu_totals as (
    select
        inv.id as invoice_id,
        coalesce(sum(bmi.line_total * wb.table_count), 0)::numeric(18, 2) as corrected_menu_items_total_amount
    from invoices inv
    join wedding_bookings wb on wb.id = inv.wedding_booking_id
    left join booking_menu_items bmi on bmi.wedding_booking_id = wb.id
    group by inv.id
)
update invoices inv
set
    menu_items_total_amount = r.corrected_menu_items_total_amount,
    final_amount =
        greatest(
            inv.hall_total_amount
                + r.corrected_menu_items_total_amount
                + inv.services_total_amount
                + inv.incidentals_total_amount
                - inv.deposit_amount,
            0::numeric
        )
        + inv.late_payment_penalty_amount
from recalculated_menu_totals r
where inv.id = r.invoice_id
  and (
      inv.menu_items_total_amount <> r.corrected_menu_items_total_amount
      or inv.final_amount <> (
          greatest(
              inv.hall_total_amount
                  + r.corrected_menu_items_total_amount
                  + inv.services_total_amount
                  + inv.incidentals_total_amount
                  - inv.deposit_amount,
              0::numeric
          )
          + inv.late_payment_penalty_amount
      )
  );
