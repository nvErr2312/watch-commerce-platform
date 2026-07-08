create table payment_methods (
    code varchar(50) primary key,
    display_name varchar(255) not null,
    base_url varchar(255) not null,
    active boolean not null
);

insert into payment_methods (code, display_name, base_url, active)
values
    ('QR', 'Mock QR Payment', 'http://localhost:8086/mock-payments/qr', true),
    ('BANK', 'Mock Bank Transfer', 'http://localhost:8086/mock-payments/bank', true),
    ('WALLET', 'Mock Wallet', 'http://localhost:8086/mock-payments/wallet', true)
on conflict (code) do nothing;
