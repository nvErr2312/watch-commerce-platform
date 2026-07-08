create table inventory_items (
    product_id bigint primary key,
    available_quantity integer not null
);

create table inventory_reservations (
    order_id bigint primary key,
    status varchar(50) not null,
    updated_at timestamptz
);
