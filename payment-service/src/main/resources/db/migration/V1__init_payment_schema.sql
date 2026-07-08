create table payment_records (
    payment_id varchar(255) primary key,
    order_id bigint not null unique,
    amount numeric(19, 2) not null,
    payment_url varchar(255) not null,
    status varchar(50) not null,
    created_at timestamptz
);
