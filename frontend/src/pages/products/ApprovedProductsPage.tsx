import { useQueries, useQuery } from '@tanstack/react-query';
import { useState } from 'react';
import { Link } from 'react-router-dom';
import { getCategories } from '@/entities/category/api/categoryApi';
import { getFileAttachments } from '@/entities/file/api/fileAttachmentApi';
import { getProducts } from '@/entities/product/api/productApi';
import { resolveFileUrl } from '@/shared/lib/fileUrl';
import { ErrorMessage } from '@/shared/ui/ErrorMessage';
import { Pagination } from '@/shared/ui/Pagination';

/** 승인 완료 상품 목록 페이지 */
export function ApprovedProductsPage() {
  const [page, setPage] = useState(0);
  const [sortDirection, setSortDirection] = useState<'desc' | 'asc'>('desc');

  const productsQuery = useQuery({
    queryKey: ['products', 'approved', page, sortDirection],
    queryFn: () => getProducts({ page, size: 10, sort: `updatedAt,${sortDirection}`, approvedOnly: true })
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
      queryKey: ['products', 'approved', product.id, 'attachments'],
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
          <p className="section-kicker">Approved Catalog</p>
          <h2 className="page-title">승인 완료 상품</h2>
          <p className="page-description">승인이 완료된 상품을 카드형 목록으로 확인합니다.</p>
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
            className="inline-flex h-11 items-center justify-center rounded-2xl border border-slate-200 bg-white/90 px-4 text-sm font-semibold text-slate-700 shadow-sm hover:-translate-y-0.5 hover:bg-slate-50"
            to="/products"
          >
            승인 전 상품 관리
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
                  className="overflow-hidden rounded-[24px] border border-white/80 bg-[linear-gradient(180deg,rgba(255,255,255,0.98)_0%,rgba(248,250,252,0.94)_100%)] shadow-[0_18px_45px_rgba(15,23,42,0.08)] transition duration-300 hover:-translate-y-1 hover:shadow-[0_24px_60px_rgba(15,23,42,0.14)]"
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
                        className="inline-flex h-9 items-center justify-center rounded-xl border border-slate-200 bg-white/90 px-3 text-sm font-semibold text-slate-700 shadow-sm transition hover:-translate-y-0.5 hover:bg-slate-50"
                        to={`/products/${product.id}`}
                      >
                        상세
                      </Link>
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
          <div className="p-5 text-sm text-slate-600">조회된 승인 완료 상품이 없습니다.</div>
        ) : null}
      </section>

      <ErrorMessage error={productsQuery.error ?? categoriesQuery.error} />
    </div>
  );
}
