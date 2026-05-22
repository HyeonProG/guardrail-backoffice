import { useMemo, type SVGProps } from 'react';
import { useQueries, useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { getCategories } from '@/entities/category/api/categoryApi';
import { getFileAttachments } from '@/entities/file/api/fileAttachmentApi';
import { getProducts } from '@/entities/product/api/productApi';
import type { ProductStatus } from '@/entities/product/model/types';
import { authStorage } from '@/features/auth/model/authStorage';
import { resolveFileUrl } from '@/shared/lib/fileUrl';
import { formatDateTime, shortId } from '@/shared/lib/format';
import { getProductStatusLabel } from '@/shared/lib/productText';
import { Button } from '@/shared/ui/Button';
import { ErrorMessage } from '@/shared/ui/ErrorMessage';
import type { UserRole } from '@/entities/user/model/types';

type StatCard = {
  label: string;
  value: number;
  helper: string;
  tone: 'success' | 'warning' | 'danger' | 'info';
  icon: (props: SVGProps<SVGSVGElement>) => JSX.Element;
  href: string;
};

const iconProps = {
  fill: 'none',
  stroke: 'currentColor',
  strokeLinecap: 'round',
  strokeLinejoin: 'round',
  strokeWidth: 2,
  viewBox: '0 0 24 24'
} as const;

function CheckCircleIcon(props: SVGProps<SVGSVGElement>) {
  return (
    <svg {...iconProps} {...props}>
      <path d="M9 12.5 11 14.5 15.5 9.5" />
      <path d="M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
    </svg>
  );
}

function ClockIcon(props: SVGProps<SVGSVGElement>) {
  return (
    <svg {...iconProps} {...props}>
      <path d="M12 6v6l4 2" />
      <path d="M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
    </svg>
  );
}

function XCircleIcon(props: SVGProps<SVGSVGElement>) {
  return (
    <svg {...iconProps} {...props}>
      <path d="m9 9 6 6" />
      <path d="m15 9-6 6" />
      <path d="M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0Z" />
    </svg>
  );
}

function FolderIcon(props: SVGProps<SVGSVGElement>) {
  return (
    <svg {...iconProps} {...props}>
      <path d="M3 7.5A2.5 2.5 0 0 1 5.5 5H9l2 2h7.5A2.5 2.5 0 0 1 21 9.5v7A2.5 2.5 0 0 1 18.5 19h-13A2.5 2.5 0 0 1 3 16.5v-9Z" />
    </svg>
  );
}

function statusBadgeClass(status: ProductStatus) {
  if (status === 'APPROVED') {
    return 'status-badge-success';
  }
  if (status === 'PENDING') {
    return 'status-badge-warning';
  }
  if (status === 'REJECTED') {
    return 'status-badge-danger';
  }
  return 'status-badge-muted';
}

function statToneClass(tone: StatCard['tone']) {
  const classes = {
    success: 'bg-emerald-50 text-emerald-700 ring-emerald-100',
    warning: 'bg-orange-50 text-orange-700 ring-orange-100',
    danger: 'bg-rose-50 text-rose-700 ring-rose-100',
    info: 'bg-sky-50 text-sky-700 ring-sky-100'
  };

  return classes[tone];
}

function statLineColor(tone: StatCard['tone']) {
  const colors = {
    success: '#10b981',
    warning: '#f97316',
    danger: '#ef4444',
    info: '#38bdf8'
  };

  return colors[tone];
}

export function DashboardPage() {
  const role = authStorage.getRole() as UserRole | null;
  const canReviewApprovals = role === 'ADMIN' || role === 'OPERATOR';

  const approvedProductsQuery = useQuery({
    queryKey: ['dashboard', 'approved-products'],
    queryFn: () => getProducts({ status: 'APPROVED', page: 0, size: 4, sort: 'updatedAt,desc' })
  });
  const pendingProductsQuery = useQuery({
    queryKey: ['dashboard', 'pending-products'],
    queryFn: () => getProducts({ status: 'PENDING', page: 0, size: 4, sort: 'updatedAt,desc' }),
    enabled: canReviewApprovals
  });
  const rejectedProductsQuery = useQuery({
    queryKey: ['dashboard', 'rejected-products'],
    queryFn: () => getProducts({ status: 'REJECTED', page: 0, size: 1, sort: 'updatedAt,desc' }),
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
  const rejectedProductCount = rejectedProductsQuery.data?.totalElements ?? 0;
  const categories = categoriesQuery.data?.content ?? [];
  const approvedProductImageQueries = useQueries({
    queries: approvedProducts.map((product) => ({
      queryKey: ['dashboard', 'approved-products', product.id, 'attachments'],
      queryFn: () => getFileAttachments('PRODUCT', product.id),
      staleTime: 60_000
    }))
  });
  const pendingProductImageQueries = useQueries({
    queries: pendingProducts.map((product) => ({
      queryKey: ['dashboard', 'pending-products', product.id, 'attachments'],
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
  const pendingProductRows = pendingProducts.map((product, index) => {
    const attachments = [...(pendingProductImageQueries[index]?.data ?? [])].sort((a, b) => a.sortOrder - b.sortOrder);
    const representativeImage = attachments[0];

    return {
      ...product,
      representativeImageUrl: resolveFileUrl(representativeImage?.filePath)
    };
  });

  const stats = useMemo<StatCard[]>(
    () => [
      {
        label: '승인 완료 상품',
        value: approvedProductCount,
        helper: '최종 운영 상품',
        tone: 'success',
        icon: CheckCircleIcon,
        href: '/products/approved'
      },
      {
        label: '승인 요청',
        value: pendingProductCount,
        helper: canReviewApprovals ? '확인 대기 중' : '관리자 검토 대상',
        tone: 'warning',
        icon: ClockIcon,
        href: canReviewApprovals ? '/approval-requests' : '/products'
      },
      {
        label: '반려 상품',
        value: rejectedProductCount,
        helper: '수정 후 재심사 필요',
        tone: 'danger',
        icon: XCircleIcon,
        href: '/products'
      },
      {
        label: '등록 카테고리',
        value: categories.length,
        helper: '전체 카테고리 수',
        tone: 'info',
        icon: FolderIcon,
        href: '/categories'
      }
    ],
    [approvedProductCount, canReviewApprovals, pendingProductCount, rejectedProductCount, categories.length]
  );

  const recentActivities = [
    ...pendingProducts.slice(0, 2).map((product) => ({
      id: product.id,
      label: `${product.name} 승인 요청 접수`,
      meta: formatDateTime(product.updatedAt),
      tone: 'warning' as const
    })),
    ...approvedProducts.slice(0, 3).map((product) => ({
      id: product.id,
      label: `${product.name} 승인 완료`,
      meta: formatDateTime(product.updatedAt),
      tone: 'success' as const
    }))
  ].slice(0, 5);

  return (
    <div className="space-y-6">
      <section className="page-header">
        <div>
          <p className="section-kicker">Commerce Operations</p>
          <h1 className="page-title">상품 운영 현황 대시보드</h1>
        </div>
        <div className="flex items-center gap-3">
          <Link to="/products/new">
            <Button type="button">상품 등록</Button>
          </Link>
          {canReviewApprovals ? (
            <Link to="/approval-requests">
              <Button type="button" variant="secondary">
                승인 요청 검토
              </Button>
            </Link>
          ) : null}
        </div>
      </section>

      <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
        {stats.map((stat, index) => {
          const Icon = stat.icon;

          return (
            <Link
              key={stat.label}
              className="surface-card animate-fade-up group block p-5 transition duration-300 hover:-translate-y-1 hover:shadow-[var(--shadow-card-hover)]"
              style={{ animationDelay: `${index * 60}ms` }}
              to={stat.href}
            >
              <div className="flex items-start justify-between gap-4">
                <div>
                  <p className="text-sm font-bold text-[color:var(--color-muted)]">{stat.label}</p>
                  <p className="mt-3 text-4xl font-black tracking-tight text-[color:var(--color-text)]">{stat.value}</p>
                  <p className="mt-2 text-xs font-semibold text-[color:var(--color-muted)]">{stat.helper}</p>
                </div>
                <span
                  className={`inline-flex h-16 w-16 items-center justify-center rounded-3xl ring-8 transition duration-300 group-hover:scale-105 ${statToneClass(stat.tone)}`}
                >
                  <Icon className="h-7 w-7" aria-hidden="true" />
                </span>
              </div>
              <div className="mt-5 h-8 overflow-hidden">
                <svg className="h-full w-full" preserveAspectRatio="none" viewBox="0 0 120 32">
                  <path
                    d="M2 24 C20 22, 23 18, 36 21 S53 30, 66 20 S81 11, 96 17 S110 21, 118 12"
                    fill="none"
                    stroke={statLineColor(stat.tone)}
                    strokeLinecap="round"
                    strokeWidth="3"
                  />
                </svg>
              </div>
            </Link>
          );
        })}
      </section>

      <section className="grid gap-6 xl:grid-cols-[1fr_360px]">
        <div className="table-shell">
          <div className="table-toolbar">
            <div className="flex items-center gap-2">
              <h2 className="table-title">승인 요청 목록</h2>
              <span className="status-badge-info">{pendingProductCount}</span>
            </div>
            {canReviewApprovals ? (
              <Link className="ghost-link" to="/approval-requests">
                전체 보기
              </Link>
            ) : null}
          </div>
          {canReviewApprovals && pendingProductRows.length > 0 ? (
            <table className="table-base min-w-[780px]">
              <thead>
                <tr>
                  <th>요청 ID</th>
                  <th>상품 정보</th>
                  <th>상태</th>
                  <th>요청일</th>
                  <th>작업</th>
                </tr>
              </thead>
              <tbody>
                {pendingProductRows.map((product) => (
                  <tr key={product.id}>
                    <td className="font-mono text-xs text-slate-500">{shortId(product.id)}</td>
                    <td>
                      <div className="flex items-center gap-3">
                        {product.representativeImageUrl ? (
                          <img
                            alt={product.name}
                            className="h-12 w-12 rounded-xl border border-slate-200 object-cover"
                            src={product.representativeImageUrl}
                          />
                        ) : (
                          <div className="flex h-12 w-12 items-center justify-center rounded-xl border border-dashed border-slate-200 bg-slate-50 text-[11px] text-slate-400">
                            없음
                          </div>
                        )}
                        <div>
                          <p className="font-bold text-slate-950">{product.name}</p>
                          <p className="mt-1 font-mono text-xs text-slate-500">{shortId(product.id)}</p>
                        </div>
                      </div>
                    </td>
                    <td>
                      <span className={statusBadgeClass(product.status)}>{getProductStatusLabel(product.status)}</span>
                    </td>
                    <td className="text-slate-600">{formatDateTime(product.updatedAt)}</td>
                    <td>
                      <Link to={`/approval-requests/${product.id}`}>
                        <Button type="button" className="h-9 px-3" variant="secondary">
                          검토하기
                        </Button>
                      </Link>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : (
            <div className="p-6 text-sm font-medium text-[color:var(--color-muted)]">
              {canReviewApprovals ? '현재 승인 대기 중인 상품이 없습니다.' : '승인 요청은 관리자/운영자만 확인할 수 있습니다.'}
            </div>
          )}
        </div>

        <aside className="surface-card p-5">
          <h2 className="table-title">최근 활동</h2>
          <div className="mt-5 space-y-4">
            {recentActivities.length > 0 ? (
              recentActivities.map((activity) => (
                <div
                  key={`${activity.id}-${activity.label}`}
                  className="flex gap-3 border-b border-[color:var(--color-border)] pb-4 last:border-0"
                >
                  <span
                    className={`mt-0.5 inline-flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-sm font-black ${
                      activity.tone === 'success'
                        ? 'bg-emerald-50 text-emerald-700 status-pulse'
                        : 'bg-amber-50 text-amber-700'
                    }`}
                  >
                    {activity.tone === 'success' ? '✓' : '!'}
                  </span>
                  <div>
                    <p className="text-sm font-bold text-[color:var(--color-text)]">{activity.label}</p>
                    <p className="mt-1 text-xs font-medium text-[color:var(--color-muted)]">{activity.meta}</p>
                  </div>
                </div>
              ))
            ) : (
              <p className="text-sm font-medium text-[color:var(--color-muted)]">최근 활동이 없습니다.</p>
            )}
          </div>
        </aside>
      </section>

      <section className="table-shell">
        <div className="table-toolbar">
          <h2 className="table-title">최근 등록 상품</h2>
          <Link className="ghost-link" to="/products/approved">
            전체 보기
          </Link>
        </div>
        {approvedProductCards.length > 0 ? (
          <div className="grid gap-4 p-5 md:grid-cols-2 xl:grid-cols-4">
            {approvedProductCards.map((product) => (
              <Link
                key={product.id}
                className="group overflow-hidden rounded-2xl border border-[color:var(--color-border)] bg-[color:var(--color-surface)] shadow-sm transition duration-300 hover:-translate-y-1 hover:shadow-[var(--shadow-card-hover)]"
                to={`/products/${product.id}`}
              >
                <div className="aspect-[4/3] overflow-hidden bg-[color:var(--color-surface-muted)]">
                  {product.representativeImageUrl ? (
                    <img
                      alt={product.name}
                      className="h-full w-full object-cover transition duration-300 group-hover:scale-[1.04]"
                      src={product.representativeImageUrl}
                    />
                  ) : (
                    <div className="flex h-full w-full items-center justify-center text-sm font-bold text-[color:var(--color-muted)]">
                      이미지 없음
                    </div>
                  )}
                </div>
                <div className="p-4">
                  <h3 className="line-clamp-1 text-sm font-bold text-[color:var(--color-text)]">{product.name}</h3>
                  <span className="status-badge-success mt-3">승인 완료</span>
                </div>
              </Link>
            ))}
          </div>
        ) : (
          <div className="p-6 text-sm font-medium text-[color:var(--color-muted)]">승인 완료된 상품이 아직 없습니다.</div>
        )}
      </section>

      <ErrorMessage
        error={
          approvedProductsQuery.error ??
          pendingProductsQuery.error ??
          rejectedProductsQuery.error ??
          categoriesQuery.error
        }
      />
    </div>
  );
}
