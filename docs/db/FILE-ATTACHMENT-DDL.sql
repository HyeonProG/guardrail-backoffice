create table file_attachments (
    id uuid primary key,
    created_at timestamp not null,
    updated_at timestamp not null,
    deleted boolean not null default false,
    target_type varchar(255) not null,
    target_id uuid not null,
    file_name varchar(255) not null,
    original_file_name varchar(255) not null,
    file_path varchar(1024) not null,
    file_size bigint not null,
    content_type varchar(255) not null,
    sort_order integer not null,
    status varchar(255) not null
);

create index idx_file_attachments_target on file_attachments (target_type, target_id);
create index idx_file_attachments_status on file_attachments (status);
create unique index uk_file_attachments_target_sort_order_not_deleted
    on file_attachments (target_type, target_id, sort_order)
    where deleted = false;
