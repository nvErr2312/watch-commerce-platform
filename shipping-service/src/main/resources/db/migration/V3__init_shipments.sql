create table shipments (
    order_id bigint primary key,
    tracking_code varchar(255),
    status varchar(50) not null,
    created_at timestamptz
);
