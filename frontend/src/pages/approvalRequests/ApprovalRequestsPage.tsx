import { useMutation, useQueries, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import { useEffect, useMemo, useState } from 'react';
import { getFileAttachments } from '@/entities/file/api/fileAttachmentApi';
import { getProducts, updateProductStatus } from '@/entities/product/api/productApi';
import type { Product } from '@/entities/product/model/types';
import { formatDateTime, shortId } from '@/shared/lib/format';
import { resolveFileUrl } from '@/shared/lib/fileUrl';
import { getProductStatusLabel } from '@/shared/lib/productText';
import { requireActorId } from '@/shared/lib/session';
import { Button } from '@/shared/ui/Button';
import { ErrorMessage } from '@/shared/ui/ErrorMessage';
import { Pagination } from '@/shared/ui/Pagination';
import { TextField } from '@/shared/ui/TextField';

/** 승인 요청 관리 페이지 */
export function ApprovalRequestsPage() {
  const queryClient = useQueryClient();
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [rejectReason, setRejectReason] = useState('');
  const [page, setPage] = useState(0);

  const pendingProductsQuery = useQuery({
    queryKey: ['products', 'approval-requests', page],
    queryFn: () => getProducts({ status: 'PENDING', page, size: 10 })
  });

  const refresh = () => {
    queryClient.invalidateQueries({ queryKey: ['products'] });
    queryClient.invalidateQueries({ queryKey: ['products', 'approval-requests'] });
    if (selectedProduct) {
      queryClient.invalidateQueries({ queryKey: ['products', selectedProduct.id] });
      queryClient.invalidateQueries({ queryKey: ['products', selectedProduct.id, 'histories'] });
    }
  };

  const approveMutation = useMutation({
    mutationFn: (productId: string) =>
      updateProductStatus(productId, {
        status: 'APPROVED',
        actorId: requireActorId()
      }),
    onSuccess: () => {
      setSelectedProduct(null);
      setRejectReason('');
      refresh();
    }
  });

  const rejectMutation = useMutation({
    mutationFn: ({ productId, reason }: { productId: string; reason: string }) =>
      updateProductStatus(productId, {
        status: 'REJECTED',
        actorId: requireActorId(),
        reason
      }),
    onSuccess: () => {
      setSelectedProduct(null);
      setRejectReason('');
      refresh();
    }
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
  const selectedProductImageList = useMemo(() => {
    if (!selectedProduct) {
      return [];
    }
    const productIndex = products.findIndex((product) => product.id === selectedProduct.id);
    if (productIndex < 0) {
      return [];
    }
    return [...(attachmentQueries[productIndex]?.data ?? [])]
      .sort((a, b) => a.sortOrder - b.sortOrder)
      .map((attachment) => ({
        id: attachment.id,
        url: resolveFileUrl(attachment.filePath),
        name: attachment.originalFileName
      }))
      .filter((item) => Boolean(item.url));
  }, [attachmentQueries, products, selectedProduct]);
  const [selectedPreviewIndex, setSelectedPreviewIndex] = useState(0);

  useEffect(() => {
    setSelectedPreviewIndex(0);
  }, [selectedProduct?.id]);

  return (
    <div className="space-y-6">
      <section>
        <h2 className="text-xl font-semibold text-ink">승인 요청 관리</h2>
        <p className="mt-1 text-sm text-slate-600">직원이 등록한 상품을 검토하고 승인 또는 반려합니다.</p>
      </section>

      <section className="grid gap-5 xl:grid-cols-[1fr_420px]">
        <div className="overflow-hidden rounded-lg border border-border bg-white shadow-sm">
          <table className="w-full min-w-[760px] text-left text-sm">
            <thead className="bg-slate-50 text-xs font-semibold uppercase text-slate-500">
              <tr>
                <th className="px-4 py-3">이미지</th>
                <th className="px-4 py-3">상품 번호</th>
                <th className="px-4 py-3">상품명</th>
                <th className="px-4 py-3">상태</th>
                <th className="px-4 py-3">마지막 수정일</th>
                <th className="px-4 py-3">액션</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
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
                      <Button type="button" variant="secondary" onClick={() => setSelectedProduct(product)}>
                        검토
                      </Button>
                      <Link
                        className="inline-flex h-10 items-center justify-center rounded-md border border-border px-4 text-sm font-medium text-ink hover:bg-slate-50"
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

        <div className="space-y-5">
          <div className="rounded-lg border border-border bg-white p-5 shadow-sm">
            <h3 className="text-base font-semibold text-ink">선택 상품 검토</h3>
            {selectedProduct ? (
              <div className="mt-4 space-y-4">
                <div className="space-y-1 text-sm text-slate-700">
                  {selectedProductImageList.length > 0 ? (
                    <div className="mb-3 space-y-3">
                      <div className="overflow-hidden rounded-lg border border-border">
                        <img
                          alt={`${selectedProduct.name} 이미지`}
                          className="aspect-[4/3] w-full object-cover"
                          src={selectedProductImageList[selectedPreviewIndex]?.url ?? undefined}
                        />
                      </div>
                      {selectedProductImageList.length > 1 ? (
                        <div className="flex gap-2 overflow-x-auto pb-1">
                          {selectedProductImageList.map((image, index) => (
                            <button
                              key={image.id}
                              className={`relative h-16 w-16 shrink-0 overflow-hidden rounded-lg border ${
                                selectedPreviewIndex === index
                                  ? 'border-slate-900 ring-2 ring-slate-200'
                                  : 'border-border'
                              }`}
                              type="button"
                              onClick={() => setSelectedPreviewIndex(index)}
                            >
                              <img alt={image.name} className="h-full w-full object-cover" src={image.url ?? undefined} />
                            </button>
                          ))}
                        </div>
                      ) : null}
                    </div>
                  ) : null}
                  <p>상품명: {selectedProduct.name}</p>
                  <p>상품 번호: {shortId(selectedProduct.id)}</p>
                  <p>상태: {getProductStatusLabel(selectedProduct.status)}</p>
                  <p>
                    선택 항목:{' '}
                    {selectedProduct.selectedOptions.length > 0
                      ? selectedProduct.selectedOptions
                          .map((option) => `${option.productOptionName}: ${option.productOptionItemName}`)
                          .join(' / ')
                      : '-'}
                  </p>
                  <p>설명 내용: {selectedProduct.description || '-'}</p>
                </div>
                <div className="flex flex-wrap gap-2">
                  <Button
                    type="button"
                    disabled={approveMutation.isPending}
                    onClick={() => approveMutation.mutate(selectedProduct.id)}
                  >
                    승인
                  </Button>
                </div>
                <div className="space-y-3">
                  <TextField label="반려 사유" value={rejectReason} onChange={(event) => setRejectReason(event.target.value)} />
                  <Button
                    type="button"
                    variant="secondary"
                    disabled={rejectMutation.isPending || !rejectReason.trim()}
                    onClick={() =>
                      rejectMutation.mutate({
                        productId: selectedProduct.id,
                        reason: rejectReason
                      })
                    }
                  >
                    반려
                  </Button>
                </div>
                <ErrorMessage error={approveMutation.error ?? rejectMutation.error} />
              </div>
            ) : (
              <p className="mt-3 text-sm text-slate-500">승인 대기 상품을 선택하면 여기서 승인 또는 반려할 수 있습니다.</p>
            )}
          </div>
        </div>
      </section>

      <ErrorMessage error={pendingProductsQuery.error} />
    </div>
  );
}
