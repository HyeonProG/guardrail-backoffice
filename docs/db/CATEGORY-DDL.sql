create table categories (
    id uuid primary key,
    created_at timestamp not null,
    updated_at timestamp not null,
    deleted boolean not null default false,
    parent_id uuid,
    name varchar(255) not null,
    status varchar(255) not null
);

create index idx_categories_parent_id on categories (parent_id);
create index idx_categories_status on categories (status);
create unique index uk_categories_root_name_not_deleted
    on categories (name)
    where parent_id is null and deleted = false;
create unique index uk_categories_parent_name_not_deleted
    on categories (parent_id, name)
    where parent_id is not null and deleted = false;
