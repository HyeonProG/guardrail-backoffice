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

-- 로컬 개발 및 백오피스 초기 로그인 테스트용 샘플 관리자 계정
-- email: admin@guardrail.com
-- password: admin1234!
insert into users (
    id,
    created_at,
    updated_at,
    deleted,
    email,
    name,
    role,
    status
) values (
    '11111111-1111-1111-1111-111111111111',
    now(),
    now(),
    false,
    'admin@guardrail.com',
    '시스템 관리자',
    'ADMIN',
    'ACTIVE'
);

insert into user_password_histories (
    id,
    created_at,
    updated_at,
    user_id,
    password_hash,
    temporary,
    expired_at
) values (
    '22222222-2222-2222-2222-222222222222',
    now(),
    now(),
    '11111111-1111-1111-1111-111111111111',
    '$2y$10$oI8/v6iRg/0iHkBjHKLHD.v7yNOzS3Tge0RJXk.cLtgdYn5BEczAm',
    false,
    null
);
