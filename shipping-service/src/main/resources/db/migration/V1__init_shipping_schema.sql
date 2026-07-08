create table shipping_fee_calculations (
    order_id bigint primary key,
    subtotal_amount numeric(19, 2) not null,
    shipping_fee numeric(19, 2) not null,
    total_amount numeric(19, 2) not null,
    shipping_address varchar(255),
    created_at timestamptz
);
