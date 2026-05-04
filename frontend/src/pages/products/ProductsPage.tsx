import { useQueries, useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { getCategories } from '@/entities/category/api/categoryApi';
import { getFileAttachments } from '@/entities/file/api/fileAttachmentApi';
import { getProducts } from '@/entities/product/api/productApi';
import { authStorage } from '@/features/auth/model/authStorage';
import type { UserRole } from '@/entities/user/model/types';
import { resolveFileUrl } from '@/shared/lib/fileUrl';
import { ErrorMessage } from '@/shared/ui/ErrorMessage';
import { Button } from '@/shared/ui/Button';

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
        size: 20,
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
      <section className="flex items-center justify-between gap-3">
        <div>
          <h2 className="text-xl font-semibold text-ink">상품 관리</h2>
          <p className="mt-1 text-sm text-slate-500">등록 중이거나 승인 대기 중인 상품을 확인하고 관리합니다.</p>
        </div>
        <div className="flex items-center gap-3">
          <label className="text-sm font-medium text-slate-700">
            정렬
            <select
              className="ml-2 h-10 rounded-md border border-border bg-white px-3 text-sm"
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
            className="inline-flex h-10 items-center justify-center rounded-md bg-ink px-4 text-sm font-medium text-white hover:bg-slate-800"
            to="/products/new"
          >
            상품 생성
          </Link>
          <Link
            className="inline-flex h-10 items-center justify-center rounded-md border border-border px-4 text-sm font-medium text-ink hover:bg-slate-50"
            to="/products/approved"
          >
            승인 완료 상품 보기
          </Link>
        </div>
      </section>

      <section className="overflow-hidden rounded-[28px] border border-slate-200/80 bg-white/90 shadow-sm">
        {products.length > 0 ? (
          <>
            <div className="grid gap-5 p-6 md:grid-cols-2 xl:grid-cols-4">
              {productCards.map((product) => (
                <article
                  key={product.id}
                  className="overflow-hidden rounded-[24px] border border-slate-200/80 bg-white shadow-sm transition duration-300 hover:-translate-y-1 hover:shadow-xl"
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
                    <div>
                      <p className="text-xs font-medium uppercase tracking-wide text-slate-400">
                        {product.categoryName || '카테고리 없음'}
                      </p>
                      <h3 className="mt-2 line-clamp-2 text-lg font-semibold text-slate-950">{product.name}</h3>
                    </div>
                    <div className="flex flex-wrap gap-2">
                      <Link
                        className="inline-flex h-9 items-center justify-center rounded-md border border-border px-3 text-sm font-medium text-ink hover:bg-slate-50"
                        to={`/products/${product.id}`}
                      >
                        상세
                      </Link>
                      <Link
                        className="inline-flex h-9 items-center justify-center rounded-md border border-border px-3 text-sm font-medium text-ink hover:bg-slate-50"
                        to={`/product-options?categoryId=${product.categoryId}`}
                      >
                        옵션 관리
                      </Link>
                      {canReviewApprovals && product.status === 'PENDING' ? (
                        <Link
                          className="inline-flex h-9 items-center justify-center rounded-md bg-slate-900 px-3 text-sm font-medium text-white hover:bg-slate-700"
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

            <div className="flex items-center justify-end border-t border-slate-200/70 px-6 py-4">
              <div className="flex items-center gap-2">
                <Button
                  type="button"
                  variant="secondary"
                  disabled={page === 0}
                  onClick={() => setPage((current) => Math.max(0, current - 1))}
                >
                  이전
                </Button>
                <span className="min-w-16 text-center text-sm font-medium text-slate-700">{page + 1}</span>
                <Button
                  type="button"
                  variant="secondary"
                  disabled={productsQuery.data ? page + 1 >= productsQuery.data.totalPages : true}
                  onClick={() => setPage((current) => current + 1)}
                >
                  다음
                </Button>
              </div>
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
