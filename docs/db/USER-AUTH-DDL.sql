create table users (
    id uuid primary key,
    created_at timestamp not null,
    updated_at timestamp not null,
    deleted boolean not null default false,
    email varchar(255) not null unique,
    name varchar(255) not null,
    role varchar(255) not null,
    status varchar(255) not null
);

create table user_password_histories (
    id uuid primary key,
    created_at timestamp not null,
    updated_at timestamp not null,
    user_id uuid not null,
    password_hash varchar(255) not null,
    temporary boolean not null,
    expired_at timestamp
);

create table user_login_histories (
    id uuid primary key,
    created_at timestamp not null,
    updated_at timestamp not null,
    user_id uuid not null,
    login_type varchar(255) not null,
    login_result varchar(255) not null,
    ip_address varchar(255) not null,
    logged_in_at timestamp not null
);

create table user_sessions (
    id uuid primary key,
    created_at timestamp not null,
    updated_at timestamp not null,
    user_id uuid not null,
    access_token_id varchar(255) not null,
    refresh_token_hash varchar(255) not null,
    device_type varchar(255) not null,
    ip_address varchar(255) not null,
    status varchar(255) not null,
    refreshed_at timestamp not null,
    expired_at timestamp not null
);

create index idx_users_email on users (email);
create index idx_user_password_histories_user_id on user_password_histories (user_id);
create index idx_user_login_histories_user_id on user_login_histories (user_id);
create index idx_user_sessions_user_id on user_sessions (user_id);
create index idx_user_sessions_status on user_sessions (status);
