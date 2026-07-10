insert into shipping_fee_rules (region_code, region_name, fee)
values
    ('HP', 'Hai Phong', 2000),
    ('CT', 'Can Tho', 2000),
    ('BD', 'Binh Duong', 1000),
    ('DNAI', 'Dong Nai', 1000),
    ('VT', 'Ba Ria Vung Tau', 2000),
    ('HUE', 'Hue', 1000),
    ('QN', 'Quang Ninh', 2000),
    ('NT', 'Nha Trang', 2000),
    ('DL', 'Da Lat', 2000)
on conflict (region_code) do nothing;
