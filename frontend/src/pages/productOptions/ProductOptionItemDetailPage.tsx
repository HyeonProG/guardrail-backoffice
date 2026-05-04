import { useMemo } from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import { getProductOption } from '@/entities/productOption/api/productOptionApi';
import type { UserRole } from '@/entities/user/model/types';
import { authStorage } from '@/features/auth/model/authStorage';
import { formatDateTime, shortId } from '@/shared/lib/format';
import { ErrorMessage } from '@/shared/ui/ErrorMessage';

/** 선택 항목 읽기 전용 상세 페이지 */
export function ProductOptionItemDetailPage() {
  const currentRole = (authStorage.getRole() as UserRole | null) ?? null;
  const { productOptionId = '', productOptionItemId = '' } = useParams();
  const [searchParams] = useSearchParams();
  const categoryId = searchParams.get('categoryId') ?? '';

  const optionQuery = useQuery({
    queryKey: ['product-options', categoryId, productOptionId],
    queryFn: () => getProductOption(categoryId, productOptionId),
    enabled: Boolean(categoryId && productOptionId)
  });

  const option = optionQuery.data;
  const item = useMemo(
    () => option?.items.find((candidate) => candidate.id === productOptionItemId) ?? null,
    [option?.items, productOptionItemId]
  );

  return (
    <div className="space-y-6">
      <section className="flex items-center justify-between gap-3">
        <div>
          <h2 className="text-xl font-semibold text-ink">선택 항목 상세</h2>
          <p className="mt-1 text-sm text-slate-600">선택 항목 정보를 읽기 전용으로 확인합니다.</p>
        </div>
        <Link
          className="text-sm font-medium text-slate-900 underline"
          to={`/product-options/${productOptionId}?categoryId=${categoryId}`}
        >
          옵션 상세로 돌아가기
        </Link>
      </section>

      <ErrorMessage error={optionQuery.error} />

      {item ? (
        <section className="rounded-lg border border-border bg-white p-6 shadow-sm">
          <div className="grid gap-4 md:grid-cols-2">
            <div className="rounded-lg border border-border bg-slate-50 p-4">
              <p className="text-xs font-medium text-slate-500">옵션 이름</p>
              <p className="mt-2 text-base font-semibold text-slate-950">{option?.name ?? '-'}</p>
            </div>
            <div className="rounded-lg border border-border bg-slate-50 p-4">
              <p className="text-xs font-medium text-slate-500">선택 항목 이름</p>
              <p className="mt-2 text-base font-semibold text-slate-950">{item.name}</p>
            </div>
            <div className="rounded-lg border border-border bg-slate-50 p-4">
              <p className="text-xs font-medium text-slate-500">선택 항목 ID</p>
              <p className="mt-2 font-mono text-sm text-slate-700">{shortId(item.id)}</p>
            </div>
            <div className="rounded-lg border border-border bg-slate-50 p-4">
              <p className="text-xs font-medium text-slate-500">추가 금액</p>
              <p className="mt-2 text-base font-semibold text-slate-950">{item.additionalPrice.toLocaleString()}원</p>
            </div>
            {currentRole !== 'STAFF' ? (
              <div className="rounded-lg border border-border bg-slate-50 p-4">
                <p className="text-xs font-medium text-slate-500">상태</p>
                <p className="mt-2 text-base font-semibold text-slate-950">{item.status}</p>
              </div>
            ) : null}
            <div className="rounded-lg border border-border bg-slate-50 p-4 md:col-span-2">
              <p className="text-xs font-medium text-slate-500">수정일</p>
              <p className="mt-2 text-base font-semibold text-slate-950">{formatDateTime(item.updatedAt)}</p>
            </div>
          </div>
        </section>
      ) : (
        !optionQuery.isLoading && (
          <div className="rounded-lg border border-border bg-white px-5 py-8 text-sm text-slate-600 shadow-sm">
            조회된 선택 항목이 없습니다.
          </div>
        )
      )}
    </div>
  );
}
