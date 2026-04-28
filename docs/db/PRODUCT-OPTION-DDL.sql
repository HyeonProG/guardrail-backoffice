create table product_options (
    id uuid primary key,
    created_at timestamp not null,
    updated_at timestamp not null,
    deleted boolean not null default false,
    product_id uuid not null,
    name varchar(255) not null,
    sort_order integer not null,
    status varchar(255) not null
);

create table product_option_items (
    id uuid primary key,
    created_at timestamp not null,
    updated_at timestamp not null,
    deleted boolean not null default false,
    product_option_id uuid not null,
    name varchar(255) not null,
    additional_price integer not null,
    sort_order integer not null,
    status varchar(255) not null
);

create index idx_product_options_product_id on product_options (product_id);
create index idx_product_options_status on product_options (status);
create index idx_product_options_deleted on product_options (deleted);
create unique index uk_product_options_product_id_name_not_deleted
    on product_options (product_id, name)
    where deleted = false;
create unique index uk_product_options_product_id_sort_order_not_deleted
    on product_options (product_id, sort_order)
    where deleted = false;

create index idx_product_option_items_product_option_id
    on product_option_items (product_option_id);
create index idx_product_option_items_status on product_option_items (status);
create index idx_product_option_items_deleted on product_option_items (deleted);
create unique index uk_product_option_items_option_id_name_not_deleted
    on product_option_items (product_option_id, name)
    where deleted = false;
create unique index uk_product_option_items_option_id_sort_order_not_deleted
    on product_option_items (product_option_id, sort_order)
    where deleted = false;

-- 옵션 그룹은 product_options.product_id = products.id 값으로 연결한다.
-- 옵션값은 product_option_items.product_option_id = product_options.id 값으로 연결한다.
-- additional_price는 애플리케이션 검증과 DB check 제약으로 0 이상만 허용한다.
alter table product_option_items
    add constraint chk_product_option_items_additional_price
    check (additional_price >= 0);
