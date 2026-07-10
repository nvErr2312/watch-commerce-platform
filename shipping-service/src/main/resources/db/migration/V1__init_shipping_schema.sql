create table shipping_fee_calculations (
    order_id bigint primary key,
    subtotal_amount numeric(19, 2) not null,
    shipping_fee numeric(19, 2) not null,
    total_amount numeric(19, 2) not null,
    shipping_address varchar(255),
    created_at timestamp with time zone
);

create table shipping_fee_rules (
    region_code varchar(50) primary key,
    region_name varchar(255) not null,
    fee numeric(19, 2) not null
);

insert into shipping_fee_rules (region_code, region_name, fee)
values
    ('HCM', 'Ho Chi Minh', 1000),
    ('HN', 'Ha Noi', 2000),
    ('DN', 'Da Nang', 1000),
    ('DEFAULT', 'Default region', 2000),
    ('HP', 'Hai Phong', 2000),
    ('CT', 'Can Tho', 2000),
    ('BD', 'Binh Duong', 1000),
    ('DNAI', 'Dong Nai', 1000),
    ('VT', 'Ba Ria Vung Tau', 2000),
    ('HUE', 'Hue', 1000),
    ('QN', 'Quang Ninh', 2000),
    ('NT', 'Nha Trang', 2000),
    ('DL', 'Da Lat', 2000);

create table shipments (
    order_id bigint primary key,
    tracking_code varchar(255),
    status varchar(50) not null,
    created_at timestamp with time zone
);

create sequence if not exists association_value_entry_seq start with 1 increment by 50;
create sequence if not exists domain_event_entry_seq start with 1 increment by 50;

create table association_value_entry (
    id bigint not null primary key,
    association_key varchar(255) not null,
    association_value varchar(255),
    saga_id varchar(255) not null,
    saga_type varchar(255)
);

create index idx_assoc_value_saga_type_key_value
    on association_value_entry (saga_type, association_key, association_value);

create index idx_assoc_value_saga_id_type
    on association_value_entry (saga_id, saga_type);

create table saga_entry (
    saga_id varchar(255) not null primary key,
    revision varchar(255),
    saga_type varchar(255),
    serialized_saga bytea
);

create table token_entry (
    processor_name varchar(255) not null,
    segment integer not null,
    owner varchar(255),
    timestamp varchar(255) not null,
    token bytea,
    token_type varchar(255),
    primary key (processor_name, segment)
);

create table domain_event_entry (
    global_index bigint not null primary key,
    aggregate_identifier varchar(255) not null,
    sequence_number bigint not null,
    type varchar(255),
    event_identifier varchar(255) not null unique,
    meta_data bytea,
    payload bytea not null,
    payload_revision varchar(255),
    payload_type varchar(255) not null,
    time_stamp varchar(255) not null
);

create unique index idx_domain_event_aggregate_sequence
    on domain_event_entry (aggregate_identifier, sequence_number);

create table snapshot_event_entry (
    aggregate_identifier varchar(255) not null,
    sequence_number bigint not null,
    type varchar(255) not null,
    event_identifier varchar(255) not null unique,
    meta_data bytea,
    payload bytea not null,
    payload_revision varchar(255),
    payload_type varchar(255) not null,
    time_stamp varchar(255) not null,
    primary key (aggregate_identifier, sequence_number, type)
);

create table dead_letter_entry (
    dead_letter_id varchar(255) not null primary key,
    processing_group varchar(255) not null,
    sequence_identifier varchar(255) not null,
    sequence_index bigint not null,
    message_type varchar(255) not null,
    event_identifier varchar(255) not null,
    time_stamp varchar(255) not null,
    payload_type varchar(255) not null,
    payload_revision varchar(255),
    payload bytea not null,
    meta_data bytea,
    type varchar(255),
    aggregate_identifier varchar(255),
    sequence_number bigint,
    token_type varchar(255),
    token bytea,
    enqueued_at timestamp(6) with time zone not null,
    last_touched timestamp(6) with time zone,
    processing_started timestamp(6) with time zone,
    cause_type varchar(255),
    cause_message varchar(1023),
    diagnostics bytea
);

create index idx_dead_letter_processing_group
    on dead_letter_entry (processing_group);

create index idx_dead_letter_processing_group_sequence
    on dead_letter_entry (processing_group, sequence_identifier);

create unique index idx_dead_letter_processing_group_sequence_index
    on dead_letter_entry (processing_group, sequence_identifier, sequence_index);
