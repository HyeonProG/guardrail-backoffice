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
    queryFn: () => getProducts({ status: 'PENDING', size: 20, sort: 'updatedAt,desc' }),
    enabled: canReviewApprovals
  });
  const categoriesQuery = useQuery({
    queryKey: ['dashboard', 'categories'],
    queryFn: () => getCategories({ size: 100 })
  });

  const approvedProducts = approvedProductsQuery.data?.content ?? [];
  const pendingProducts = pendingProductsQuery.data?.content ?? [];
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
      { label: '승인 완료 상품', value: approvedProducts.length, accent: 'from-emerald-500 to-teal-500' },
      ...(canReviewApprovals
        ? [{ label: '승인 요청 건수', value: pendingProducts.length, accent: 'from-amber-500 to-orange-500' }]
        : []),
      { label: '등록 카테고리', value: categories.length, accent: 'from-sky-500 to-indigo-500' }
    ],
    [approvedProducts.length, canReviewApprovals, pendingProducts.length, categories.length]
  );

  return (
    <div className="space-y-8">
      <section className="overflow-hidden rounded-[28px] border border-white/70 bg-[linear-gradient(135deg,#0f172a_0%,#1e293b_45%,#334155_100%)] p-8 text-white shadow-[0_30px_80px_rgba(15,23,42,0.18)]">
        <div className="grid gap-8 xl:grid-cols-[1.35fr_0.85fr]">
          <div className="animate-fade-up">
            <p className="text-sm font-medium uppercase tracking-[0.3em] text-slate-300">Guardrail Backoffice</p>
            <h1 className="mt-4 max-w-3xl text-4xl font-semibold leading-tight">
              상품 운영 현황을 한눈에 확인하는 대시보드
            </h1>
            <p className="mt-4 max-w-2xl text-base leading-7 text-slate-300">
              승인 완료 상품, 승인 대기 요청, 기준 정보 관리 화면으로 빠르게 이동하며 현재 운영 상태를 확인할 수 있습니다.
            </p>
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
          </div>

          <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-1">
            {stats.map((stat, index) => (
              <article
                key={stat.label}
                className="animate-fade-up rounded-3xl border border-white/10 bg-white/10 p-5 backdrop-blur-md"
                style={{ animationDelay: `${index * 80}ms` }}
              >
                <div className={`inline-flex rounded-full bg-gradient-to-r ${stat.accent} px-3 py-1 text-xs font-semibold`}>
                  LIVE
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
            className="animate-fade-up rounded-[24px] border border-slate-200/80 bg-white/90 p-5 shadow-sm transition duration-300 hover:-translate-y-1 hover:shadow-xl"
            style={{ animationDelay: `${120 + index * 70}ms` }}
            to={item.path}
          >
            <div className="inline-flex rounded-full bg-slate-950 px-3 py-1 text-xs font-semibold text-white">바로가기</div>
            <h2 className="mt-4 text-lg font-semibold text-slate-950">{item.label}</h2>
            <p className="mt-2 text-sm leading-6 text-slate-600">{item.description}</p>
          </Link>
        ))}
      </section>

      <section className={canReviewApprovals ? 'grid gap-6 xl:grid-cols-[1.3fr_0.7fr]' : 'grid gap-6'}>
        <div className="overflow-hidden rounded-[28px] border border-slate-200/80 bg-white/90 shadow-sm">
          <div className="flex items-center justify-between border-b border-slate-200/70 px-6 py-5">
            <div>
              <h2 className="text-xl font-semibold text-slate-950">상품 목록</h2>
            </div>
            <div className="flex items-center gap-3">
              <Link className="text-sm font-semibold text-slate-900 underline" to="/products/approved">
                전체 상품 보기
              </Link>
            </div>
          </div>
          {approvedProducts.length > 0 ? (
            <>
              <div className="grid gap-5 p-6 md:grid-cols-2 xl:grid-cols-4">
                {approvedProductCards.map((product) => (
                  <Link
                    key={product.id}
                    className="overflow-hidden rounded-[24px] border border-slate-200/80 bg-white shadow-sm transition duration-300 hover:-translate-y-1 hover:shadow-xl"
                    to={`/products/${product.id}`}
                  >
                    <div className="aspect-[4/3] overflow-hidden bg-slate-100">
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
                    <div className="space-y-3 p-4">
                      <h3 className="line-clamp-2 text-lg font-semibold text-slate-950">{product.name}</h3>
                    </div>
                  </Link>
                ))}
              </div>

            </>
          ) : null}

          {!approvedProductsQuery.isLoading && approvedProducts.length === 0 ? (
            <div className="px-6 py-12 text-sm text-slate-500">승인 완료된 상품이 아직 없습니다.</div>
          ) : null}
        </div>

        {canReviewApprovals ? (
          <div className="space-y-6">
            <section className="rounded-[28px] border border-slate-200/80 bg-white/90 p-6 shadow-sm">
              <div className="flex items-center justify-between gap-3">
                <div>
                  <h2 className="text-lg font-semibold text-slate-950">승인 요청 현황</h2>
                  <p className="mt-1 text-sm text-slate-500">관리자 검토를 기다리는 상품 요청 건수입니다.</p>
                </div>
                <Link className="text-sm font-semibold text-slate-900 underline" to="/approval-requests">
                  검토하러 가기
                </Link>
              </div>
              <div className="mt-5 rounded-2xl bg-slate-950 px-5 py-6 text-white">
                <p className="text-sm text-slate-300">승인 대기 요청</p>
                <p className="mt-2 text-4xl font-semibold">{pendingProducts.length}</p>
              </div>
            </section>
          </div>
        ) : null}
      </section>

      <ErrorMessage error={approvedProductsQuery.error ?? pendingProductsQuery.error ?? categoriesQuery.error} />
    </div>
  );
}
