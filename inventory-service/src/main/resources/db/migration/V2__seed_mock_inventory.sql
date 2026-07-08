insert into inventory_items (product_id, available_quantity)
values
    (10, 5),
    (11, 10),
    (12, 3)
on conflict (product_id) do nothing;
