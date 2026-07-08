create table order_read_models (
    id bigint primary key,
    user_id bigint not null,
    status varchar(50) not null,
    shipping_fee numeric(19, 2) not null,
    total_amount numeric(19, 2) not null,
    shipping_address varchar(255) not null,
    payment_url varchar(255),
    updated_at timestamptz
);
