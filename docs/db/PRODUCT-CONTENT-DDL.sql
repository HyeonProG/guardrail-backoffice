create table product_content_drafts (
    id uuid primary key,
    created_at timestamp not null,
    updated_at timestamp not null,
    deleted boolean not null default false,
    product_id uuid not null,
    content varchar(2000) not null,
    source varchar(255) not null,
    status varchar(255) not null,
    requested_by_actor_id uuid not null,
    submitted_by_actor_id uuid,
    approved_by_actor_id uuid,
    reject_reason varchar(1000)
);

create table product_content_histories (
    id uuid primary key,
    created_at timestamp not null,
    updated_at timestamp not null,
    draft_id uuid not null,
    product_id uuid not null,
    actor_id uuid not null,
    type varchar(255) not null,
    reason varchar(1000)
);

create index idx_product_content_drafts_product_id on product_content_drafts (product_id);
create index idx_product_content_drafts_status on product_content_drafts (status);
create index idx_product_content_drafts_deleted on product_content_drafts (deleted);
create index idx_product_content_histories_draft_id on product_content_histories (draft_id);
create index idx_product_content_histories_product_id on product_content_histories (product_id);
create index idx_product_content_histories_actor_id on product_content_histories (actor_id);

-- 상품 설명 초안과 이력은 products.id, users.id를 객체 연관관계 없이 UUID 값으로 참조한다.
-- 승인된 초안의 content만 products.description에 반영한다.
