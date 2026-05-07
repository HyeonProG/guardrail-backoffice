import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { getFileAttachments } from '@/entities/file/api/fileAttachmentApi';
import { getProduct, getProductHistories, updateProductStatus } from '@/entities/product/api/productApi';
import { getCategories } from '@/entities/category/api/categoryApi';
import { formatDateTime, shortId } from '@/shared/lib/format';
import { resolveFileUrl } from '@/shared/lib/fileUrl';
import { getProductHistoryTypeLabel, getProductStatusLabel } from '@/shared/lib/productText';
import { requireActorId } from '@/shared/lib/session';
import { Button } from '@/shared/ui/Button';
import { ErrorMessage } from '@/shared/ui/ErrorMessage';
import { Modal } from '@/shared/ui/Modal';
import { TextField } from '@/shared/ui/TextField';

/** 승인 요청 상품 상세 페이지 */
export function ProductApprovalDetailPage() {
  const { productId = '' } = useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [previewIndex, setPreviewIndex] = useState(0);
  const [rejectReason, setRejectReason] = useState('');
  const [approveConfirmOpen, setApproveConfirmOpen] = useState(false);
  const [rejectConfirmOpen, setRejectConfirmOpen] = useState(false);
  const [completedAction, setCompletedAction] = useState<'APPROVED' | 'REJECTED' | null>(null);
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
    queryKey: ['categories', 'review'],
    queryFn: () => getCategories({ size: 100 })
  });
  const attachmentsQuery = useQuery({
    queryKey: ['file-attachments', 'PRODUCT', productId],
    queryFn: () => getFileAttachments('PRODUCT', productId),
    enabled: Boolean(productId)
  });

  const product = productQuery.data;
  const attachments = [...(attachmentsQuery.data ?? [])].sort((a, b) => a.sortOrder - b.sortOrder);
  const categoryName =
    categoriesQuery.data?.content.find((category) => category.id === product?.categoryId)?.name ?? '-';
  const imageItems = attachments
    .map((attachment) => ({
      id: attachment.id,
      url: resolveFileUrl(attachment.filePath),
      name: attachment.originalFileName
    }))
    .filter((item) => Boolean(item.url));

  useEffect(() => {
    setPreviewIndex(0);
  }, [productId]);

  const refresh = async () => {
    await queryClient.invalidateQueries({ queryKey: ['products'] });
    await queryClient.invalidateQueries({ queryKey: ['products', productId] });
    await queryClient.invalidateQueries({ queryKey: ['products', productId, 'histories'] });
  };

  const approveMutation = useMutation({
    mutationFn: () =>
      updateProductStatus(productId, {
        status: 'APPROVED',
        actorId: requireActorId()
      }),
    onSuccess: async () => {
      await refresh();
      setApproveConfirmOpen(false);
      setCompletedAction('APPROVED');
    }
  });

  const rejectMutation = useMutation({
    mutationFn: () =>
      updateProductStatus(productId, {
        status: 'REJECTED',
        actorId: requireActorId(),
        reason: rejectReason.trim()
      }),
    onSuccess: async () => {
      await refresh();
      setRejectConfirmOpen(false);
      setCompletedAction('REJECTED');
    }
  });

  return (
    <div className="space-y-6">
      <section className="page-header">
        <div>
          <p className="section-kicker">Review Detail</p>
          <h2 className="page-title">승인 요청 상품 상세</h2>
        </div>
        <Link
          className="inline-flex h-11 items-center justify-center rounded-xl border border-slate-200 bg-white/90 px-4 text-sm font-semibold text-slate-800 shadow-sm transition hover:-translate-y-0.5 hover:border-slate-300 hover:bg-white hover:shadow-md"
          to="/approval-requests"
        >
          승인 요청 관리로 돌아가기
        </Link>
      </section>

      <ErrorMessage error={productQuery.error ?? attachmentsQuery.error ?? historiesQuery.error ?? categoriesQuery.error} />

      <section className="grid gap-5 xl:grid-cols-[minmax(0,1fr)_380px]">
        <div className="space-y-5">
          <div className="surface-card-muted p-5">
            <h3 className="text-base font-semibold text-ink">상품 기본 정보</h3>
            <div className="mt-4 grid gap-4 md:grid-cols-2">
              <div className="rounded-lg border border-border bg-slate-50 p-4">
                <p className="text-xs font-medium text-slate-500">상품명</p>
                <p className="mt-2 text-sm font-medium text-ink">{product?.name ?? '-'}</p>
              </div>
              <div className="rounded-lg border border-border bg-slate-50 p-4">
                <p className="text-xs font-medium text-slate-500">카테고리</p>
                <p className="mt-2 text-sm font-medium text-ink">{categoryName}</p>
              </div>
              <div className="rounded-lg border border-border bg-slate-50 p-4">
                <p className="text-xs font-medium text-slate-500">상태</p>
                <p className="mt-2 text-sm font-medium text-ink">{product ? getProductStatusLabel(product.status) : '-'}</p>
              </div>
            </div>
            <div className="mt-4 rounded-lg border border-border bg-slate-50 p-4">
              <p className="text-xs font-medium text-slate-500">상세 설명</p>
              <p className="mt-2 whitespace-pre-wrap text-sm leading-7 text-ink">{product?.description || '-'}</p>
            </div>
            <div className="mt-4 rounded-lg border border-border bg-slate-50 p-4">
              <p className="text-xs font-medium text-slate-500">선택 항목</p>
              <p className="mt-2 whitespace-pre-wrap text-sm leading-7 text-ink">
                {product?.selectedOptions.length
                  ? product.selectedOptions
                      .map((option) => `${option.productOptionName}: ${option.productOptionItemName}`)
                      .join(' / ')
                  : '-'}
              </p>
            </div>
          </div>

          <div className="surface-card-muted p-5">
            <h3 className="text-base font-semibold text-ink">상품 이미지</h3>
            {imageItems.length === 0 ? (
              <div className="mt-4 rounded-lg border border-dashed border-border px-4 py-8 text-center text-sm text-slate-500">
                등록된 이미지가 없습니다.
              </div>
            ) : (
              <div className="mt-4 space-y-4">
                <div className="overflow-hidden rounded-xl border border-border">
                  <img
                    alt={imageItems[previewIndex]?.name ?? '상품 이미지'}
                    className="aspect-[4/3] w-full object-cover"
                    src={imageItems[previewIndex]?.url ?? undefined}
                  />
                </div>
                {imageItems.length > 1 ? (
                  <div className="flex gap-2 overflow-x-auto pb-1">
                    {imageItems.map((image, index) => (
                      <button
                        key={image.id}
                        className={`relative h-16 w-16 shrink-0 overflow-hidden rounded-lg border ${
                          previewIndex === index ? 'border-slate-900 ring-2 ring-slate-200' : 'border-border'
                        }`}
                        type="button"
                        onClick={() => setPreviewIndex(index)}
                      >
                        <img alt={image.name} className="h-full w-full object-cover" src={image.url ?? undefined} />
                      </button>
                    ))}
                  </div>
                ) : null}
              </div>
            )}
          </div>
        </div>

        <div className="space-y-5">
          <div className="surface-card-muted p-5">
            <h3 className="text-base font-semibold text-ink">기본 정보</h3>
            <div className="mt-4 space-y-3 text-sm text-slate-600">
              <p>상품 번호: {product ? shortId(product.id) : '-'}</p>
              <p>마지막 수정일: {product ? formatDateTime(product.updatedAt) : '-'}</p>
              <p>카테고리: {categoryName}</p>
            </div>
          </div>

          <div className="surface-card p-5">
            <h3 className="text-base font-semibold text-ink">승인 처리</h3>
            <div className="mt-4 space-y-3">
              <div className="rounded-2xl border border-slate-200 bg-slate-50/80 p-4 text-sm text-slate-700">
                현재 상태: <span className="font-semibold text-slate-950">{product ? getProductStatusLabel(product.status) : '-'}</span>
              </div>
              <Button
                type="button"
                disabled={approveMutation.isPending || rejectMutation.isPending}
                onClick={() => setApproveConfirmOpen(true)}
              >
                승인
              </Button>
              <TextField
                label="반려 사유"
                placeholder="반려 사유를 입력해 주세요."
                value={rejectReason}
                onChange={(event) => setRejectReason(event.target.value)}
              />
              <Button
                type="button"
                variant="secondary"
                disabled={approveMutation.isPending || rejectMutation.isPending || !rejectReason.trim()}
                onClick={() => setRejectConfirmOpen(true)}
              >
                반려
              </Button>
              <ErrorMessage error={approveMutation.error ?? rejectMutation.error} />
            </div>
          </div>
        </div>
      </section>

      <section className="table-shell">
        <div className="border-b border-slate-200/70 px-5 py-4">
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
        open={approveConfirmOpen}
        title="상품 승인 확인"
        description="이 상품을 승인하시겠습니까? 승인 후에는 승인 완료 상품 목록에 반영됩니다."
        onClose={() => setApproveConfirmOpen(false)}
      >
        <div className="space-y-4">
          <div className="rounded-2xl border border-slate-200 bg-slate-50/80 p-4 text-sm leading-6 text-slate-700">
            승인 대상: <span className="font-semibold text-slate-950">{product?.name ?? '-'}</span>
          </div>
          <ErrorMessage error={approveMutation.error} />
          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={() => setApproveConfirmOpen(false)}>
              취소
            </Button>
            <Button type="button" disabled={approveMutation.isPending} onClick={() => approveMutation.mutate()}>
              승인하기
            </Button>
          </div>
        </div>
      </Modal>

      <Modal
        open={rejectConfirmOpen}
        title="상품 반려 확인"
        description="이 상품을 반려하시겠습니까? 반려 사유가 상품 이력에 기록됩니다."
        onClose={() => setRejectConfirmOpen(false)}
      >
        <div className="space-y-4">
          <div className="rounded-2xl border border-red-100 bg-[linear-gradient(180deg,#fef2f2_0%,#fff7f7_100%)] p-4 text-sm leading-6 text-red-700">
            반려 대상: <span className="font-semibold text-red-800">{product?.name ?? '-'}</span>
            <br />
            사유: <span className="font-semibold">{rejectReason.trim() || '-'}</span>
          </div>
          <ErrorMessage error={rejectMutation.error} />
          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={() => setRejectConfirmOpen(false)}>
              취소
            </Button>
            <Button type="button" variant="danger" disabled={rejectMutation.isPending} onClick={() => rejectMutation.mutate()}>
              반려하기
            </Button>
          </div>
        </div>
      </Modal>

      <Modal
        open={completedAction !== null}
        title={completedAction === 'APPROVED' ? '상품 승인 완료' : '상품 반려 완료'}
        description={
          completedAction === 'APPROVED'
            ? '승인 처리가 완료되었습니다. 승인 요청 관리 목록으로 돌아갑니다.'
            : '반려 처리가 완료되었습니다. 승인 요청 관리 목록으로 돌아갑니다.'
        }
        onClose={() => {
          setCompletedAction(null);
          navigate('/approval-requests', { replace: true });
        }}
      >
        <div className="space-y-4">
          <div className="rounded-2xl border border-slate-200 bg-slate-50/80 p-4 text-sm leading-6 text-slate-700">
            처리 상품: <span className="font-semibold text-slate-950">{product?.name ?? '-'}</span>
          </div>
          <div className="flex justify-end">
            <Button
              type="button"
              onClick={() => {
                setCompletedAction(null);
                navigate('/approval-requests', { replace: true });
              }}
            >
              목록으로 돌아가기
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
