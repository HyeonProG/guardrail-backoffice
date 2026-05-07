import { useMemo } from 'react';
import { useQueries, useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { getCategories } from '@/entities/category/api/categoryApi';
import { getFileAttachments } from '@/entities/file/api/fileAttachmentApi';
import { getProducts } from '@/entities/product/api/productApi';
import { authStorage } from '@/features/auth/model/authStorage';
import { resolveFileUrl } from '@/shared/lib/fileUrl';
import { Button } from '@/shared/ui/Button';
import { ErrorMessage } from '@/shared/ui/ErrorMessage';
import type { UserRole } from '@/entities/user/model/types';

export function DashboardPage() {
  const role = authStorage.getRole() as UserRole | null;
  const canReviewApprovals = role === 'ADMIN' || role === 'OPERATOR';

  const approvedProductsQuery = useQuery({
    queryKey: ['dashboard', 'approved-products'],
    queryFn: () => getProducts({ status: 'APPROVED', page: 0, size: 4, sort: 'updatedAt,desc' })
  });
  const pendingProductsQuery = useQuery({
    queryKey: ['dashboard', 'pending-products'],
    queryFn: () => getProducts({ status: 'PENDING', page: 0, size: 10, sort: 'updatedAt,desc' }),
    enabled: canReviewApprovals
  });
  const categoriesQuery = useQuery({
    queryKey: ['dashboard', 'categories'],
    queryFn: () => getCategories({ size: 100 })
  });

  const approvedProducts = approvedProductsQuery.data?.content ?? [];
  const pendingProducts = pendingProductsQuery.data?.content ?? [];
  const approvedProductCount = approvedProductsQuery.data?.totalElements ?? 0;
  const pendingProductCount = pendingProductsQuery.data?.totalElements ?? 0;
  const categories = categoriesQuery.data?.content ?? [];
  const approvedProductImageQueries = useQueries({
    queries: approvedProducts.map((product) => ({
      queryKey: ['dashboard', 'approved-products', product.id, 'attachments'],
      queryFn: () => getFileAttachments('PRODUCT', product.id),
      staleTime: 60_000
    }))
  });
  const approvedProductCards = approvedProducts.map((product, index) => {
    const attachments = [...(approvedProductImageQueries[index]?.data ?? [])].sort(
      (a, b) => a.sortOrder - b.sortOrder
    );
    const representativeImage = attachments[0];

    return {
      ...product,
      representativeImageUrl: resolveFileUrl(representativeImage?.filePath)
    };
  });
  const quickLinks = [
    {
      label: '상품 등록 시작',
      path: '/products/new',
      description: '카테고리와 옵션을 선택해 신규 상품 등록을 시작합니다.'
    },
    ...(canReviewApprovals
      ? [
          {
            label: '승인 요청 확인',
            path: '/approval-requests',
            description: '직원이 제출한 승인 대기 상품을 검토합니다.'
          }
        ]
      : []),
    {
      label: '카테고리 관리',
      path: '/categories',
      description: '전시 구조와 부모/하위 카테고리를 정리합니다.'
    },
    {
      label: '상품 옵션 관리',
      path: '/product-options',
      description: '카테고리별 옵션과 선택 항목을 관리합니다.'
    }
  ];

  const stats = useMemo(
    () => [
      { label: '승인 완료 상품', value: approvedProductCount, accent: 'from-emerald-500 to-teal-500', badge: '상품' },
      ...(canReviewApprovals
        ? [{ label: '승인 요청 건수', value: pendingProductCount, accent: 'from-amber-500 to-orange-500', badge: '요청' }]
        : []),
      { label: '등록 카테고리', value: categories.length, accent: 'from-sky-500 to-indigo-500', badge: '기준 정보' }
    ],
    [approvedProductCount, canReviewApprovals, pendingProductCount, categories.length]
  );

  return (
    <div className="space-y-8">
      <section className="overflow-hidden rounded-[28px] border border-white/70 bg-[linear-gradient(135deg,#0f172a_0%,#1e293b_45%,#334155_100%)] p-8 text-white shadow-[0_30px_80px_rgba(15,23,42,0.18)]">
        <div className="grid gap-8 xl:grid-cols-[1.35fr_0.85fr]">
          <div className="animate-fade-up">
            <p className="text-sm font-medium uppercase tracking-[0.3em] text-slate-300">Guardrail Backoffice</p>
            <h1 className="mt-4 max-w-3xl text-4xl font-semibold leading-tight">
              상품 운영 현황 대시보드
            </h1>
            <div className="mt-8 flex flex-wrap gap-3">
              <Link
                className="inline-flex items-center rounded-full bg-white px-5 py-3 text-sm font-semibold text-slate-950 transition duration-300 hover:-translate-y-0.5 hover:shadow-lg"
                to="/products/new"
              >
                상품 등록하러 가기
              </Link>
              {canReviewApprovals ? (
                <Link
                  className="inline-flex items-center rounded-full border border-white/20 px-5 py-3 text-sm font-semibold text-white/90 transition duration-300 hover:border-white/40 hover:bg-white/10"
                  to="/approval-requests"
                >
                  승인 요청 검토
                </Link>
              ) : null}
            </div>
            <div className="mt-10">
              <div className="flex items-center justify-between gap-3">
                <h2 className="text-sm font-semibold uppercase tracking-[0.22em] text-slate-300">최근 등록 상품</h2>
                <Link
                  className="text-sm font-medium text-slate-300 transition hover:text-white"
                  to="/products/approved"
                >
                  전체 보기
                </Link>
              </div>
              {approvedProducts.length > 0 ? (
                <div className="mt-4 grid grid-cols-2 gap-3 xl:grid-cols-4 xl:max-w-[760px]">
                  {approvedProductCards.map((product) => (
                    <Link
                      key={product.id}
                      className="overflow-hidden rounded-[22px] border border-white/15 bg-white/10 shadow-[0_16px_40px_rgba(15,23,42,0.16)] transition duration-300 hover:-translate-y-1 hover:border-white/25 hover:bg-white/12"
                      to={`/products/${product.id}`}
                    >
                      <div className="aspect-[4/5] overflow-hidden bg-slate-800/60">
                        {product.representativeImageUrl ? (
                          <img
                            alt={product.name}
                            className="h-full w-full object-cover transition duration-300 hover:scale-[1.03]"
                            src={product.representativeImageUrl}
                          />
                        ) : (
                          <div className="flex h-full w-full items-center justify-center text-sm font-medium text-slate-400">
                            이미지 없음
                          </div>
                        )}
                      </div>
                      <div className="p-4">
                        <h3 className="line-clamp-2 text-sm font-semibold text-white">{product.name}</h3>
                      </div>
                    </Link>
                  ))}
                </div>
              ) : (
                !approvedProductsQuery.isLoading && (
                  <div className="mt-4 rounded-[24px] border border-dashed border-white/20 bg-white/5 px-5 py-8 text-sm text-slate-300">
                    승인 완료된 상품이 아직 없습니다.
                  </div>
                )
              )}
            </div>
          </div>

          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-1">
            {stats.map((stat, index) => (
              <article
                key={stat.label}
                className="animate-fade-up rounded-[28px] border border-white/15 bg-white/10 p-5 shadow-[0_16px_40px_rgba(15,23,42,0.16)] backdrop-blur-md"
                style={{ animationDelay: `${index * 80}ms` }}
              >
                <div className={`inline-flex rounded-full bg-gradient-to-r ${stat.accent} px-3 py-1 text-xs font-semibold`}>
                  {stat.badge}
                </div>
                <p className="mt-4 text-sm text-slate-300">{stat.label}</p>
                <p className="mt-2 text-3xl font-semibold text-white">{stat.value}</p>
              </article>
            ))}
          </div>
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        {quickLinks.map((item, index) => (
          <Link
            key={item.label}
            className="animate-fade-up rounded-[24px] border border-white/80 bg-[linear-gradient(180deg,rgba(255,255,255,0.96)_0%,rgba(248,250,252,0.92)_100%)] p-5 shadow-[0_18px_45px_rgba(15,23,42,0.08)] transition duration-300 hover:-translate-y-1 hover:shadow-[0_24px_60px_rgba(15,23,42,0.14)]"
            style={{ animationDelay: `${120 + index * 70}ms` }}
            to={item.path}
          >
            <div className="inline-flex rounded-full bg-slate-950 px-3 py-1 text-xs font-semibold text-white">바로가기</div>
            <h2 className="mt-4 text-lg font-semibold text-slate-950">{item.label}</h2>
            <p className="mt-2 text-sm leading-6 text-slate-600">{item.description}</p>
          </Link>
        ))}
      </section>

      <ErrorMessage error={approvedProductsQuery.error ?? pendingProductsQuery.error ?? categoriesQuery.error} />
    </div>
  );
}
