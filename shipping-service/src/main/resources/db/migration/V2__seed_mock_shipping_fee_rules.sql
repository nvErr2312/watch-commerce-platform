create table shipping_fee_rules (
    region_code varchar(50) primary key,
    region_name varchar(255) not null,
    fee numeric(19, 2) not null
);

insert into shipping_fee_rules (region_code, region_name, fee)
values
    ('HCM', 'Ho Chi Minh', 30000),
    ('HN', 'Ha Noi', 40000),
    ('DN', 'Da Nang', 35000),
    ('DEFAULT', 'Default region', 45000)
on conflict (region_code) do nothing;
