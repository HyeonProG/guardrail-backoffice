import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { useEffect, useMemo, useState } from 'react';
import { getCategories } from '@/entities/category/api/categoryApi';
import { getFileAttachments } from '@/entities/file/api/fileAttachmentApi';
import {
  deleteProduct,
  getProduct,
  getProductHistories,
  updateProductStatus
} from '@/entities/product/api/productApi';
import type { ProductStatus } from '@/entities/product/model/types';
import type { UserRole } from '@/entities/user/model/types';
import { authStorage } from '@/features/auth/model/authStorage';
import { resolveFileUrl } from '@/shared/lib/fileUrl';
import { formatDateTime, shortId } from '@/shared/lib/format';
import { getProductHistoryTypeLabel, getProductStatusLabel } from '@/shared/lib/productText';
import { requireActorId } from '@/shared/lib/session';
import { Button } from '@/shared/ui/Button';
import { ErrorMessage } from '@/shared/ui/ErrorMessage';
import { Modal } from '@/shared/ui/Modal';
import { TextField } from '@/shared/ui/TextField';

const productStatuses: ProductStatus[] = ['DRAFT', 'PENDING', 'APPROVED', 'REJECTED', 'INACTIVE'];

export function ProductDetailPage() {
  const { productId = '' } = useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const currentRole = (authStorage.getRole() as UserRole | null) ?? null;
  const canManageDangerousActions = currentRole === 'ADMIN' || currentRole === 'OPERATOR';
  const [nextStatus, setNextStatus] = useState<ProductStatus>('PENDING');
  const [reason, setReason] = useState('');
  const [imageIndex, setImageIndex] = useState(0);
  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);

  const productQuery = useQuery({
    queryKey: ['products', productId],
    queryFn: () => getProduct(productId),
    enabled: Boolean(productId)
  });
  const historiesQuery = useQuery({
    queryKey: ['products', productId, 'histories'],
    queryFn: () => getProductHistories(productId),
    enabled: Boolean(productId)
  });
  const categoriesQuery = useQuery({
    queryKey: ['categories', 'active-options'],
    queryFn: () => getCategories({ status: 'ACTIVE', size: 100 })
  });
  const attachmentsQuery = useQuery({
    queryKey: ['products', productId, 'attachments'],
    queryFn: () => getFileAttachments('PRODUCT', productId),
    enabled: Boolean(productId)
  });

  const refresh = () => {
    queryClient.invalidateQueries({ queryKey: ['products'] });
    queryClient.invalidateQueries({ queryKey: ['products', productId] });
  };
  const statusMutation = useMutation({
    mutationFn: () =>
      updateProductStatus(productId, {
        status: nextStatus,
        actorId: requireActorId(),
        reason: reason || undefined
      }),
    onSuccess: () => {
      setReason('');
      refresh();
      queryClient.invalidateQueries({ queryKey: ['products', productId, 'histories'] });
    }
  });
  const deleteMutation = useMutation({
    mutationFn: () => deleteProduct(productId),
    onSuccess: () => navigate('/products', { replace: true })
  });

  const categories = categoriesQuery.data?.content ?? [];
  const attachments = useMemo(
    () => [...(attachmentsQuery.data ?? [])].sort((a, b) => a.sortOrder - b.sortOrder),
    [attachmentsQuery.data]
  );
  const canStaffEditPendingProduct =
    currentRole === 'STAFF' &&
    (productQuery.data?.status === 'DRAFT' ||
      productQuery.data?.status === 'PENDING' ||
      productQuery.data?.status === 'REJECTED');
  const currentImage = attachments[imageIndex] ?? null;
  const currentCategoryName =
    categories.find((category) => category.id === productQuery.data?.categoryId)?.name ?? '-';
  const canDeleteProduct =
    productQuery.data?.status === 'DRAFT' ||
    productQuery.data?.status === 'PENDING' ||
    productQuery.data?.status === 'REJECTED';
  const canShowActionPanel = canManageDangerousActions;

  useEffect(() => {
    if (imageIndex >= attachments.length) {
      setImageIndex(0);
    }
  }, [attachments.length, imageIndex]);

  return (
    <div className="space-y-6">
      <section className="page-header">
        <div>
          <p className="section-kicker">Product Detail</p>
          <h2 className="page-title">상품 상세</h2>
          <p className="page-description">상품 정보와 이미지, 선택 항목, 변경 이력을 한 화면에서 확인합니다.</p>
        </div>
        <div className="flex items-center gap-3">
          {canManageDangerousActions || canStaffEditPendingProduct ? (
            <Link className="ghost-link" to={`/products/${productId}/edit`}>
              상품 수정
            </Link>
          ) : null}
          <Link className="ghost-link" to={`/product-options?categoryId=${productQuery.data?.categoryId ?? ''}`}>
            상품 옵션 관리
          </Link>
          {canDeleteProduct ? (
            <Button
              type="button"
              variant="danger"
              disabled={deleteMutation.isPending}
              onClick={() => setDeleteConfirmOpen(true)}
            >
              삭제
            </Button>
          ) : null}
        </div>
      </section>

      <ErrorMessage error={productQuery.error ?? historiesQuery.error ?? categoriesQuery.error ?? attachmentsQuery.error} />

      {productQuery.data ? (
        <section className={canShowActionPanel ? 'grid gap-5 xl:grid-cols-[1.1fr_0.9fr]' : 'grid gap-5'}>
          <div className="table-shell">
            <div className="flex items-center justify-between border-b border-slate-200/70 px-6 py-5">
              <div>
                <p className="text-xs font-medium uppercase tracking-wide text-slate-400">Product Overview</p>
                <h3 className="mt-2 text-2xl font-semibold text-slate-950">{productQuery.data.name}</h3>
              </div>
              <span className="rounded-full bg-slate-950 px-4 py-2 text-xs font-semibold text-white">
                {getProductStatusLabel(productQuery.data.status)}
              </span>
            </div>

            <div className="grid gap-6 p-6 lg:grid-cols-[1.05fr_0.95fr]">
              <div className="space-y-4">
                <div className="relative overflow-hidden rounded-[24px] border border-slate-200 bg-slate-100">
                  <div className="aspect-[4/3]">
                    {currentImage ? (
                      <img
                        alt={productQuery.data.name}
                        className="h-full w-full object-cover"
                        src={resolveFileUrl(currentImage.filePath) ?? undefined}
                      />
                    ) : (
                      <div className="flex h-full w-full items-center justify-center text-sm font-medium text-slate-400">
                        등록된 이미지가 없습니다.
                      </div>
                    )}
                  </div>
                  {attachments.length > 1 ? (
                    <>
                      <button
                        className="absolute left-3 top-1/2 inline-flex h-10 w-10 -translate-y-1/2 items-center justify-center rounded-full bg-black/60 text-lg text-white transition hover:bg-black/75"
                        type="button"
                        onClick={() => setImageIndex((current) => (current === 0 ? attachments.length - 1 : current - 1))}
                      >
                        ‹
                      </button>
                      <button
                        className="absolute right-3 top-1/2 inline-flex h-10 w-10 -translate-y-1/2 items-center justify-center rounded-full bg-black/60 text-lg text-white transition hover:bg-black/75"
                        type="button"
                        onClick={() => setImageIndex((current) => (current + 1) % attachments.length)}
                      >
                        ›
                      </button>
                    </>
                  ) : null}
                </div>

                {attachments.length > 1 ? (
                  <div className="grid grid-cols-5 gap-3">
                    {attachments.map((attachment, index) => (
                      <button
                        key={attachment.id}
                        className={`overflow-hidden rounded-2xl border ${
                          index === imageIndex ? 'border-slate-950 ring-2 ring-slate-200' : 'border-slate-200'
                        }`}
                        type="button"
                        onClick={() => setImageIndex(index)}
                      >
                        <div className="aspect-square bg-slate-100">
                          <img
                            alt={`${productQuery.data.name} ${index + 1}`}
                            className="h-full w-full object-cover"
                            src={resolveFileUrl(attachment.filePath) ?? undefined}
                          />
                        </div>
                      </button>
                    ))}
                  </div>
                ) : null}
              </div>

              <div className="space-y-4">
                <div className="grid gap-3 sm:grid-cols-2">
                  <div className="rounded-2xl border border-slate-200 bg-slate-50/80 p-4">
                    <p className="text-xs font-medium uppercase tracking-wide text-slate-500">카테고리</p>
                    <p className="mt-2 text-base font-semibold text-slate-950">{currentCategoryName}</p>
                  </div>
                  <div className="rounded-2xl border border-slate-200 bg-slate-50/80 p-4">
                    <p className="text-xs font-medium uppercase tracking-wide text-slate-500">상품 번호</p>
                    <p className="mt-2 font-mono text-sm text-slate-700">{shortId(productQuery.data.id)}</p>
                  </div>
                  <div className="rounded-2xl border border-slate-200 bg-slate-50/80 p-4">
                    <p className="text-xs font-medium uppercase tracking-wide text-slate-500">수정일</p>
                    <p className="mt-2 text-base font-semibold text-slate-950">{formatDateTime(productQuery.data.updatedAt)}</p>
                  </div>
                </div>

                <div className="rounded-[24px] border border-slate-200/80 bg-[linear-gradient(180deg,#ffffff_0%,#f8fafc_100%)] p-5 shadow-sm">
                  <div className="flex items-center justify-between gap-3">
                    <h3 className="text-base font-semibold text-slate-950">선택 항목</h3>
                    <span className="text-xs font-medium text-slate-400">{productQuery.data.selectedOptions.length}개</span>
                  </div>
                  {productQuery.data.selectedOptions.length > 0 ? (
                    <div className="mt-4 flex flex-wrap gap-2">
                      {productQuery.data.selectedOptions.map((selectedOption) => (
                        <div
                          key={`${selectedOption.productOptionId}-${selectedOption.productOptionItemId}`}
                          className="rounded-full border border-slate-200 bg-slate-50 px-3 py-2 text-sm text-slate-700"
                        >
                          <span className="font-medium text-slate-950">{selectedOption.productOptionName}</span>
                          <span className="mx-1 text-slate-400">·</span>
                          <span>{selectedOption.productOptionItemName}</span>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="mt-4 text-sm text-slate-500">선택된 항목이 없습니다.</p>
                  )}
                </div>

                <div className="rounded-[24px] border border-slate-200/80 bg-[linear-gradient(180deg,#ffffff_0%,#f8fafc_100%)] p-5 shadow-sm">
                  <h3 className="text-base font-semibold text-slate-950">상품 설명</h3>
                  <p className="mt-3 whitespace-pre-wrap text-sm leading-7 text-slate-600">
                    {productQuery.data.description?.trim() || '등록된 설명이 없습니다.'}
                  </p>
                </div>
              </div>
            </div>
          </div>

          {canShowActionPanel ? (
            <div className="space-y-5">
              {canManageDangerousActions ? (
                <>
                  <div className="surface-card p-6">
                    <h3 className="text-base font-semibold text-ink">기본 정보 수정</h3>
                    <p className="mt-2 text-sm leading-6 text-slate-600">
                      운영자와 관리자는 별도 수정 페이지에서 상품명, 카테고리, 설명을 변경할 수 있습니다.
                    </p>
                    <div className="mt-4">
                      <Link
                        className="inline-flex h-11 items-center justify-center rounded-2xl border border-sky-900/90 bg-[linear-gradient(135deg,#0f172a_0%,#1e3a8a_100%)] px-4 text-sm font-semibold text-white shadow-lg hover:-translate-y-0.5"
                        to={`/products/${productId}/edit`}
                      >
                        수정 페이지로 이동
                      </Link>
                    </div>
                  </div>
                  <div className="surface-card p-6">
                    <h3 className="text-base font-semibold text-ink">상태 변경</h3>
                    <div className="mt-4 space-y-3">
                      <p className="text-sm text-slate-600">현재 상태: {getProductStatusLabel(productQuery.data.status)}</p>
                      <select
                        className="h-12 w-full rounded-2xl border border-slate-200 bg-white/90 px-4 text-sm shadow-sm"
                        value={nextStatus}
                        onChange={(event) => setNextStatus(event.target.value as ProductStatus)}
                      >
                        {productStatuses.map((status) => (
                          <option key={status} value={status}>
                            {getProductStatusLabel(status)}
                          </option>
                        ))}
                      </select>
                      <TextField label="사유" value={reason} onChange={(event) => setReason(event.target.value)} />
                      <ErrorMessage error={statusMutation.error} />
                      <Button type="button" disabled={statusMutation.isPending} onClick={() => statusMutation.mutate()}>
                        상태 변경
                      </Button>
                    </div>
                  </div>
                </>
              ) : null}
              <ErrorMessage error={deleteMutation.error} />
            </div>
          ) : null}
        </section>
      ) : null}

      <section className="table-shell">
        <div className="border-b border-slate-200/70 px-6 py-5">
          <h3 className="text-base font-semibold text-ink">변경 이력</h3>
        </div>
        <table className="table-base min-w-[760px]">
          <thead>
            <tr>
              <th className="px-4 py-3">이력 번호</th>
              <th className="px-4 py-3">변경 내용</th>
              <th className="px-4 py-3">담당자</th>
              <th className="px-4 py-3">사유</th>
              <th className="px-4 py-3">일시</th>
            </tr>
          </thead>
          <tbody>
            {(historiesQuery.data ?? []).map((history) => (
              <tr key={history.id}>
                <td className="px-4 py-3 font-mono text-xs text-slate-600">{shortId(history.id)}</td>
                <td className="px-4 py-3">{getProductHistoryTypeLabel(history.type)}</td>
                <td className="px-4 py-3">{history.actorName}</td>
                <td className="px-4 py-3 text-slate-600">{history.reason ?? '-'}</td>
                <td className="px-4 py-3 text-slate-600">{formatDateTime(history.createdAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      <Modal
        open={deleteConfirmOpen}
        title="상품 삭제 확인"
        description="이 상품을 완전히 삭제하시겠습니까? 상품 정보, 이미지, 이력이 함께 제거되며 되돌릴 수 없습니다."
        onClose={() => setDeleteConfirmOpen(false)}
      >
        <div className="space-y-4">
          <div className="rounded-[20px] border border-red-100 bg-[linear-gradient(180deg,#fef2f2_0%,#fff7f7_100%)] p-4 text-sm leading-6 text-red-700 shadow-sm">
            삭제 가능 상태인 상품만 제거할 수 있습니다. 실행 후에는 복구할 수 없습니다.
          </div>
          <ErrorMessage error={deleteMutation.error} />
          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={() => setDeleteConfirmOpen(false)}>
              취소
            </Button>
            <Button type="button" variant="danger" disabled={deleteMutation.isPending} onClick={() => deleteMutation.mutate()}>
              삭제하기
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
