-- Aligns Inventory's product_id with Product Service's UUID identity space
-- (previously a bigint mock catalog: 10/11/12, unrelated to real products).
alter table inventory_items alter column product_id type varchar(36) using product_id::varchar;

delete from inventory_items;

insert into inventory_items (product_id, available_quantity) values
    ('bde7b1d3-1b3e-47ac-88dd-9574df739979', 40),  -- Casio G-Shock GA-2100
    ('3dd817bc-19a0-4904-bebf-be776258ba67', 1),    -- Patek Philippe Nautilus 5711
    ('c9962b2e-c171-4da8-8e83-41b3e95c3850', 15),   -- Seiko Presage Cocktail Time
    ('8dfd4bb5-440f-4196-b97f-40ed15078121', 5),    -- Rolex Submariner Date
    ('f4d1c974-2e87-435a-b600-4c883f72450c', 10),   -- Omega Speedmaster Professional
    ('52b6f738-1518-4779-a7cd-4f91d5993b64', 20),   -- Seiko Prospex Diver
    ('c594f9c4-d138-40f2-a0be-8f6612b72eee', 2),     -- Hublot Big Bang Unico
    ('c293d597-c394-4d56-89cf-7ce85fbebb17', 8),     -- Tag Heuer Carrera Calibre 16
    ('c03ba0bf-43ad-4dc5-a5c1-64a54638093e', 50);    -- Casio Edifice Chronograph
