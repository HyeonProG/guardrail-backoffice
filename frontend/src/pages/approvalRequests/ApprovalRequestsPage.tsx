import { useQueries, useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { useState } from 'react';
import { getFileAttachments } from '@/entities/file/api/fileAttachmentApi';
import { getProducts } from '@/entities/product/api/productApi';
import { formatDateTime, shortId } from '@/shared/lib/format';
import { resolveFileUrl } from '@/shared/lib/fileUrl';
import { getProductStatusLabel } from '@/shared/lib/productText';
import { ErrorMessage } from '@/shared/ui/ErrorMessage';
import { Pagination } from '@/shared/ui/Pagination';

/** 승인 요청 관리 페이지 */
export function ApprovalRequestsPage() {
  const [page, setPage] = useState(0);

  const pendingProductsQuery = useQuery({
    queryKey: ['products', 'approval-requests', page],
    queryFn: () => getProducts({ status: 'PENDING', page, size: 10 })
  });

  const products = pendingProductsQuery.data?.content ?? [];
  const attachmentQueries = useQueries({
    queries: products.map((product) => ({
      queryKey: ['file-attachments', 'PRODUCT', product.id],
      queryFn: () => getFileAttachments('PRODUCT', product.id),
      enabled: Boolean(product.id)
    }))
  });
  const imageByProductId = products.reduce<Record<string, string | null>>((acc, product, index) => {
    const attachments = attachmentQueries[index]?.data ?? [];
    const representative = [...attachments].sort((a, b) => a.sortOrder - b.sortOrder)[0] ?? null;
    acc[product.id] = resolveFileUrl(representative?.filePath);
    return acc;
  }, {});
  return (
    <div className="space-y-6">
      <section className="page-header">
        <div>
          <p className="section-kicker">Review Queue</p>
          <h2 className="page-title">승인 요청 관리</h2>
          <p className="page-description">직원이 등록한 상품을 검토하고 승인 또는 반려합니다.</p>
        </div>
      </section>

      <section className="grid gap-5">
        <div className="table-shell">
          <table className="table-base min-w-[760px]">
            <thead>
              <tr>
                <th className="px-4 py-3">이미지</th>
                <th className="px-4 py-3">상품 번호</th>
                <th className="px-4 py-3">상품명</th>
                <th className="px-4 py-3">상태</th>
                <th className="px-4 py-3">마지막 수정일</th>
                <th className="px-4 py-3">액션</th>
              </tr>
            </thead>
            <tbody>
              {products.map((product) => (
                <tr key={product.id}>
                  <td className="px-4 py-3">
                    {imageByProductId[product.id] ? (
                      <img
                        alt={`${product.name} 대표 이미지`}
                        className="h-14 w-14 rounded-md border border-border object-cover"
                        src={imageByProductId[product.id] ?? undefined}
                      />
                    ) : (
                      <div className="flex h-14 w-14 items-center justify-center rounded-md border border-dashed border-border text-[11px] text-slate-400">
                        없음
                      </div>
                    )}
                  </td>
                  <td className="px-4 py-3 font-mono text-xs text-slate-600">{shortId(product.id)}</td>
                  <td className="px-4 py-3 font-medium text-ink">{product.name}</td>
                  <td className="px-4 py-3">{getProductStatusLabel(product.status)}</td>
                  <td className="px-4 py-3 text-slate-600">{formatDateTime(product.updatedAt)}</td>
                  <td className="px-4 py-3">
                    <div className="flex flex-wrap gap-2">
                      <Link
                        className="inline-flex h-10 items-center justify-center rounded-xl border border-slate-200 bg-white/90 px-4 text-sm font-semibold text-slate-800 shadow-sm transition hover:-translate-y-0.5 hover:border-slate-300 hover:bg-white hover:shadow-md"
                        to={`/approval-requests/${product.id}`}
                      >
                        상세
                      </Link>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {products.length > 0 ? (
            <div className="border-t border-slate-200/70 px-6 py-4">
              <Pagination
                currentPage={page}
                totalPages={pendingProductsQuery.data?.totalPages ?? 0}
                onPageChange={setPage}
              />
            </div>
          ) : null}
          {products.length === 0 && !pendingProductsQuery.isLoading ? (
            <div className="p-5 text-sm text-slate-600">현재 승인 대기 중인 상품이 없습니다.</div>
          ) : null}
        </div>
      </section>

      <ErrorMessage error={pendingProductsQuery.error} />
    </div>
  );
}
