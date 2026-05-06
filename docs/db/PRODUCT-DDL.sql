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

create index idx_products_category_id on products (category_id);
create index idx_products_status on products (status);
create index idx_products_deleted on products (deleted);
create index idx_product_histories_product_id on product_histories (product_id);
create index idx_product_histories_actor_id on product_histories (actor_id);

-- 상품 이미지는 file_attachments.target_type = 'PRODUCT',
-- file_attachments.target_id = products.id 조합으로 연결한다.
