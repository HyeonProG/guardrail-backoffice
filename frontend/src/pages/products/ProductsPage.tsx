import { useQueries, useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { getCategories } from '@/entities/category/api/categoryApi';
import { getFileAttachments } from '@/entities/file/api/fileAttachmentApi';
import { getProducts } from '@/entities/product/api/productApi';
import { authStorage } from '@/features/auth/model/authStorage';
import type { UserRole } from '@/entities/user/model/types';
import { resolveFileUrl } from '@/shared/lib/fileUrl';
import { getProductStatusLabel } from '@/shared/lib/productText';
import { ErrorMessage } from '@/shared/ui/ErrorMessage';
import { Pagination } from '@/shared/ui/Pagination';

export function ProductsPage() {
  const role = authStorage.getRole() as UserRole | null;
  const canReviewApprovals = role === 'ADMIN' || role === 'OPERATOR';
  const [page, setPage] = useState(0);
  const [sortDirection, setSortDirection] = useState<'desc' | 'asc'>('desc');

  const productsQuery = useQuery({
    queryKey: ['products', 'pre-approval', page, sortDirection],
    queryFn: () =>
      getProducts({
        page,
        size: 8,
        sort: `updatedAt,${sortDirection}`,
        approvedOnly: false,
        myOnly: true
      })
  });
  const categoriesQuery = useQuery({
    queryKey: ['categories', 'active-options'],
    queryFn: () => getCategories({ status: 'ACTIVE', size: 100 })
  });

  const products = productsQuery.data?.content ?? [];
  const categories = categoriesQuery.data?.content ?? [];
  const categoryNameMap = new Map(categories.map((category) => [category.id, category.name]));
  const productImageQueries = useQueries({
    queries: products.map((product) => ({
      queryKey: ['products', product.id, 'attachments'],
      queryFn: () => getFileAttachments('PRODUCT', product.id),
      staleTime: 60_000
    }))
  });
  const productCards = products.map((product, index) => {
    const attachments = [...(productImageQueries[index]?.data ?? [])].sort((a, b) => a.sortOrder - b.sortOrder);
    const representativeImage = attachments[0];

    return {
      ...product,
      categoryName: categoryNameMap.get(product.categoryId) ?? '',
      representativeImageUrl: resolveFileUrl(representativeImage?.filePath)
    };
  });

  return (
    <div className="space-y-6">
      <section className="page-header">
        <div>
          <p className="section-kicker">Product Queue</p>
          <h2 className="page-title">상품 관리</h2>
        </div>
        <div className="flex items-center gap-3">
          <label className="text-sm font-medium text-slate-700">
            정렬
            <select
              className="ml-2 h-11 rounded-2xl border border-slate-200 bg-white/90 px-4 text-sm shadow-sm"
              value={sortDirection}
              onChange={(event) => {
                setPage(0);
                setSortDirection(event.target.value as 'desc' | 'asc');
              }}
            >
              <option value="desc">최신 순</option>
              <option value="asc">오래된 순</option>
            </select>
          </label>
          <Link
            className="inline-flex h-11 items-center justify-center rounded-2xl border border-sky-900/90 bg-[linear-gradient(135deg,#0f172a_0%,#1e3a8a_100%)] px-4 text-sm font-semibold text-white shadow-lg hover:-translate-y-0.5"
            to="/products/new"
          >
            상품 생성
          </Link>
          <Link
            className="inline-flex h-11 items-center justify-center rounded-2xl border border-slate-200 bg-white/90 px-4 text-sm font-semibold text-slate-700 shadow-sm hover:-translate-y-0.5 hover:bg-slate-50"
            to="/products/approved"
          >
            승인 완료 상품 보기
          </Link>
        </div>
      </section>

      <section className="table-shell">
        {products.length > 0 ? (
          <>
            <div className="grid gap-5 p-6 md:grid-cols-2 xl:grid-cols-4">
              {productCards.map((product) => (
                <article
                  key={product.id}
                  className="product-card"
                >
                  <Link to={`/products/${product.id}`}>
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
                  </Link>
                  <div className="space-y-4 p-4">
                    <div className="flex items-start justify-between gap-3">
                      <div className="min-w-0">
                        <p className="text-xs font-medium uppercase tracking-wide text-slate-400">
                          {product.categoryName || '카테고리 없음'}
                        </p>
                        <h3 className="mt-2 line-clamp-2 text-lg font-semibold text-slate-950">{product.name}</h3>
                      </div>
                      <span
                        className={`shrink-0 rounded-full px-3 py-1 text-xs font-semibold ${
                          product.status === 'REJECTED'
                            ? 'border border-red-200 bg-red-50 text-red-700'
                            : product.status === 'PENDING'
                              ? 'border border-amber-200 bg-amber-50 text-amber-700'
                              : product.status === 'APPROVED'
                                ? 'border border-emerald-200 bg-emerald-50 text-emerald-700'
                                : 'border border-slate-200 bg-slate-50 text-slate-600'
                        }`}
                      >
                        {getProductStatusLabel(product.status)}
                      </span>
                    </div>
                    <div>
                      <p className="text-xs font-medium uppercase tracking-wide text-slate-400">
                        상품 상태
                      </p>
                      <p className="mt-2 text-sm font-medium text-slate-600">
                        {product.status === 'REJECTED'
                          ? '반려된 상품입니다. 내용을 수정한 뒤 다시 재심사를 요청할 수 있습니다.'
                          : getProductStatusLabel(product.status)}
                      </p>
                    </div>
                    <div className="flex flex-wrap gap-2">
                      <Link
                        className="inline-flex h-9 items-center justify-center rounded-xl border border-slate-200 bg-white/90 px-3 text-sm font-semibold text-slate-700 shadow-sm transition hover:-translate-y-0.5 hover:bg-slate-50"
                        to={`/products/${product.id}`}
                      >
                        상세
                      </Link>
                      {canReviewApprovals ? (
                        <Link
                          className="inline-flex h-9 items-center justify-center rounded-xl border border-slate-200 bg-white/90 px-3 text-sm font-semibold text-slate-700 shadow-sm transition hover:-translate-y-0.5 hover:bg-slate-50"
                          to={`/product-options?categoryId=${product.categoryId}`}
                        >
                          옵션 관리
                        </Link>
                      ) : null}
                      {canReviewApprovals && product.status === 'PENDING' ? (
                        <Link
                          className="inline-flex h-9 items-center justify-center rounded-xl border border-sky-900/90 bg-[linear-gradient(135deg,#0f172a_0%,#1e3a8a_100%)] px-3 text-sm font-semibold text-white shadow-lg hover:-translate-y-0.5"
                          to={`/approval-requests/${product.id}`}
                        >
                          승인 검토
                        </Link>
                      ) : null}
                    </div>
                  </div>
                </article>
              ))}
            </div>

            <div className="border-t border-slate-200/70 px-6 py-4">
              <Pagination
                currentPage={page}
                totalPages={productsQuery.data?.totalPages ?? 0}
                onPageChange={setPage}
              />
            </div>
          </>
        ) : null}
        {productsQuery.isLoading ? <div className="p-5 text-sm text-slate-600">조회 중입니다.</div> : null}
        {products.length === 0 && !productsQuery.isLoading ? (
          <div className="p-5 text-sm text-slate-600">조회된 상품이 없습니다.</div>
        ) : null}
      </section>

      <ErrorMessage error={productsQuery.error ?? categoriesQuery.error} />
    </div>
  );
}
