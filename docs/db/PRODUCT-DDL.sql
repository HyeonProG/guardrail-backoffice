create table products (
    id uuid primary key,
    created_at timestamp not null,
    updated_at timestamp not null,
    deleted boolean not null default false,
    category_id uuid not null,
    name varchar(255) not null,
    description varchar(2000) not null,
    status varchar(255) not null
);

create table product_histories (
    id uuid primary key,
    created_at timestamp not null,
    updated_at timestamp not null,
    product_id uuid not null,
    actor_id uuid not null,
    type varchar(255) not null,
    reason varchar(1000)
);

create table product_selected_options (
    id uuid primary key,
    created_at timestamp not null,
    updated_at timestamp not null,
    product_id uuid not null,
    product_option_id uuid not null,
    product_option_name varchar(255) not null,
    product_option_item_id uuid not null,
    product_option_item_name varchar(255) not null,
    sort_order integer not null
);

create index idx_products_category_id on products (category_id);
create index idx_products_status on products (status);
create index idx_products_deleted on products (deleted);
create index idx_product_histories_product_id on product_histories (product_id);
create index idx_product_histories_actor_id on product_histories (actor_id);
create index idx_product_selected_options_product_id on product_selected_options (product_id);
create index idx_product_selected_options_product_option_id on product_selected_options (product_option_id);
create index idx_product_selected_options_product_option_item_id on product_selected_options (product_option_item_id);
create unique index uq_product_selected_options_product_item
    on product_selected_options (product_id, product_option_item_id);

-- 상품 이미지는 file_attachments.target_type = 'PRODUCT',
-- file_attachments.target_id = products.id 조합으로 연결한다.
