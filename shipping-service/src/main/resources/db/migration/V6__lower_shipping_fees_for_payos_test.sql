update shipping_fee_rules
set fee = case region_code
    when 'HCM' then 1000
    when 'DN' then 1000
    when 'BD' then 1000
    when 'DNAI' then 1000
    when 'HUE' then 1000
    else 2000
end;
