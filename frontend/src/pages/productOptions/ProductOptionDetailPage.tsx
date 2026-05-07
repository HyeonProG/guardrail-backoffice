import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import {
  createProductOptionItem,
  getProductOption,
  updateProductOption,
  updateProductOptionItem
} from '@/entities/productOption/api/productOptionApi';
import type { ProductOptionItem } from '@/entities/productOption/model/types';
import type { UserRole } from '@/entities/user/model/types';
import { authStorage } from '@/features/auth/model/authStorage';
import { productOptionItemSchema, productOptionSchema, type ProductOptionFormValues, type ProductOptionItemFormValues } from '@/pages/productOptions/productOptionSchema';
import { formatDateTime, shortId } from '@/shared/lib/format';
import { requireActorId } from '@/shared/lib/session';
import { Button } from '@/shared/ui/Button';
import { ErrorMessage } from '@/shared/ui/ErrorMessage';
import { Modal } from '@/shared/ui/Modal';
import { Pagination } from '@/shared/ui/Pagination';
import { TextField } from '@/shared/ui/TextField';

/** 상품 옵션 상세 페이지 */
export function ProductOptionDetailPage() {
  const pageSize = 10;
  const currentRole = (authStorage.getRole() as UserRole | null) ?? null;
  const canManageOptionMaster = currentRole === 'ADMIN' || currentRole === 'OPERATOR';
  const { productOptionId = '' } = useParams();
  const [searchParams] = useSearchParams();
  const categoryId = searchParams.get('categoryId') ?? '';
  const queryClient = useQueryClient();
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [createItemModalOpen, setCreateItemModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<ProductOptionItem | null>(null);
  const [editItemModalOpen, setEditItemModalOpen] = useState(false);
  const [itemPage, setItemPage] = useState(0);
  const [sortDirection, setSortDirection] = useState<'desc' | 'asc'>('desc');

  const optionQuery = useQuery({
    queryKey: ['product-options', categoryId, productOptionId],
    queryFn: () => getProductOption(categoryId, productOptionId),
    enabled: Boolean(categoryId && productOptionId)
  });

  const option = optionQuery.data;

  const optionForm = useForm<ProductOptionFormValues>({
    resolver: zodResolver(productOptionSchema),
    values: { name: option?.name ?? '' }
  });
  const createItemForm = useForm<ProductOptionItemFormValues>({
    resolver: zodResolver(productOptionItemSchema),
    defaultValues: { name: '' }
  });
  const editItemForm = useForm<ProductOptionItemFormValues>({
    resolver: zodResolver(productOptionItemSchema),
    values: { name: editingItem?.name ?? '' }
  });

  const sortedItems = useMemo(
    () =>
      [...(option?.items ?? [])].sort((a, b) => {
        const timeA = new Date(a.updatedAt).getTime();
        const timeB = new Date(b.updatedAt).getTime();
        return sortDirection === 'desc' ? timeB - timeA : timeA - timeB;
      }),
    [option?.items, sortDirection]
  );
  const pagedItems = useMemo(
    () => sortedItems.slice(itemPage * pageSize, (itemPage + 1) * pageSize),
    [sortedItems, itemPage]
  );
  const totalItemPages = Math.ceil(sortedItems.length / pageSize);

  const refresh = () => {
    queryClient.invalidateQueries({ queryKey: ['product-options', categoryId] });
    queryClient.invalidateQueries({ queryKey: ['product-options', categoryId, productOptionId] });
  };

  const updateOptionMutation = useMutation({
    mutationFn: (values: ProductOptionFormValues) =>
      updateProductOption(categoryId, productOptionId, { ...values, actorId: requireActorId() }),
    onSuccess: () => {
      setEditModalOpen(false);
      refresh();
    }
  });
  const createItemMutation = useMutation({
    mutationFn: (values: ProductOptionItemFormValues) =>
      createProductOptionItem(categoryId, productOptionId, { ...values, status: 'ACTIVE', actorId: requireActorId() }),
    onSuccess: () => {
      setCreateItemModalOpen(false);
      createItemForm.reset({ name: '' });
      refresh();
    }
  });
  const updateItemMutation = useMutation({
    mutationFn: (values: ProductOptionItemFormValues) => {
      if (!editingItem) {
        throw new Error('수정할 선택 항목을 찾을 수 없습니다.');
      }
      return updateProductOptionItem(categoryId, productOptionId, editingItem.id, {
        ...values,
        actorId: requireActorId()
      });
    },
    onSuccess: () => {
      setEditItemModalOpen(false);
      setEditingItem(null);
      refresh();
    }
  });

  return (
    <div className="space-y-6">
      <section className="page-header">
        <div>
          <p className="section-kicker">Option Detail</p>
          <h2 className="page-title">옵션 상세</h2>
        </div>
        <div className="flex items-center gap-3">
          <Link
            className="inline-flex h-11 items-center justify-center rounded-xl border border-slate-200 bg-white/90 px-4 text-sm font-semibold text-slate-800 shadow-sm transition duration-200 hover:-translate-y-0.5 hover:border-slate-300 hover:bg-white hover:shadow-md"
            to={`/product-options?categoryId=${categoryId}`}
          >
            옵션 목록으로
          </Link>
          {canManageOptionMaster ? (
            <Button type="button" onClick={() => setCreateItemModalOpen(true)}>
              선택 항목 추가
            </Button>
          ) : null}
        </div>
      </section>

      <ErrorMessage error={optionQuery.error} />

      {option ? (
        <>
          <section className="surface-card-muted p-5">
            <div className="flex items-start justify-between gap-4">
              <div className="space-y-2">
                <p className="text-xs font-medium uppercase tracking-wide text-slate-500">옵션 정보</p>
                <h3 className="text-2xl font-semibold text-slate-950">{option.name}</h3>
                <div className="flex flex-wrap gap-4 text-sm text-slate-600">
                  <span>ID {shortId(option.id)}</span>
                  {currentRole !== 'STAFF' ? <span>상태 {option.status}</span> : null}
                  <span>수정일 {formatDateTime(option.updatedAt)}</span>
                </div>
              </div>
              {canManageOptionMaster ? (
                <Button type="button" variant="secondary" onClick={() => setEditModalOpen(true)}>
                  수정
                </Button>
              ) : null}
            </div>
          </section>

          <section className="table-shell">
            <div className="border-b border-slate-200/70 px-5 py-4">
              <div className="flex items-center justify-between gap-3">
                <h3 className="text-base font-semibold text-ink">선택 항목 목록</h3>
                <label className="text-sm font-medium text-slate-700">
                  정렬
                  <select
                    className="ml-2 h-11 rounded-2xl border border-slate-200 bg-white/90 px-4 text-sm shadow-sm"
                    value={sortDirection}
                    onChange={(event) => {
                      setItemPage(0);
                      setSortDirection(event.target.value as 'desc' | 'asc');
                    }}
                  >
                    <option value="desc">최신 순</option>
                    <option value="asc">오래된 순</option>
                  </select>
                </label>
              </div>
            </div>
            <table className="table-base min-w-[760px]">
              <thead>
                <tr>
                  <th className="px-4 py-3">ID</th>
                  <th className="px-4 py-3">선택 항목 이름</th>
                  {currentRole !== 'STAFF' ? <th className="px-4 py-3">상태</th> : null}
                  <th className="px-4 py-3">수정일</th>
                  <th className="px-4 py-3">액션</th>
                </tr>
              </thead>
              <tbody>
                {pagedItems.map((item) => (
                  <tr key={item.id}>
                    <td className="px-4 py-3 font-mono text-xs text-slate-600">{shortId(item.id)}</td>
                    <td className="px-4 py-3 font-medium text-ink">{item.name}</td>
                    {currentRole !== 'STAFF' ? <td className="px-4 py-3">{item.status}</td> : null}
                    <td className="px-4 py-3 text-slate-600">{formatDateTime(item.updatedAt)}</td>
                    <td className="px-4 py-3">
                      <div className="flex flex-wrap gap-2">
                        <Link
                          className="inline-flex h-11 items-center justify-center rounded-xl border border-slate-200 bg-white/90 px-4 text-sm font-semibold text-slate-800 shadow-sm transition duration-200 hover:-translate-y-0.5 hover:border-slate-300 hover:bg-white hover:shadow-md"
                          to={`/product-options/${productOptionId}/items/${item.id}?categoryId=${categoryId}`}
                        >
                          상세
                        </Link>
                      {canManageOptionMaster ? (
                        <Button
                          type="button"
                          variant="secondary"
                          onClick={() => {
                            setEditingItem(item);
                            setEditItemModalOpen(true);
                          }}
                        >
                          수정
                        </Button>
                      ) : null}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            {sortedItems.length === 0 ? <div className="p-5 text-sm text-slate-600">등록된 선택 항목이 없습니다.</div> : null}
            {sortedItems.length > 0 ? (
              <div className="border-t border-slate-200 px-4 py-4">
                <Pagination
                  currentPage={itemPage}
                  totalPages={totalItemPages}
                  onPageChange={setItemPage}
                />
              </div>
            ) : null}
          </section>
        </>
      ) : null}

      <Modal open={editModalOpen} title="옵션 수정" description="옵션 이름을 수정합니다." onClose={() => setEditModalOpen(false)}>
        <form className="space-y-4" onSubmit={optionForm.handleSubmit((values) => updateOptionMutation.mutate(values))}>
          <TextField label="옵션 이름" error={optionForm.formState.errors.name?.message} {...optionForm.register('name')} />
          <ErrorMessage error={updateOptionMutation.error} />
          <div className="flex justify-end gap-2 border-t border-slate-200 pt-4">
            <Button type="button" variant="secondary" onClick={() => setEditModalOpen(false)}>
              취소
            </Button>
            <Button disabled={updateOptionMutation.isPending} type="submit">
              수정
            </Button>
          </div>
        </form>
      </Modal>

      {canManageOptionMaster ? (
        <Modal
          open={createItemModalOpen}
          title="선택 항목 추가"
          description="현재 옵션에 연결할 선택 항목을 추가합니다."
          onClose={() => {
            setCreateItemModalOpen(false);
            createItemForm.reset({ name: '' });
          }}
        >
          <form className="space-y-4" onSubmit={createItemForm.handleSubmit((values) => createItemMutation.mutate(values))}>
            <TextField label="선택 항목 이름" error={createItemForm.formState.errors.name?.message} {...createItemForm.register('name')} />
            <p className="rounded-md border border-dashed border-border bg-slate-50 px-3 py-2 text-xs text-slate-500">
              정렬 순서는 생성 시 자동으로 부여됩니다.
            </p>
            <ErrorMessage error={createItemMutation.error} />
            <div className="flex justify-end gap-2 border-t border-slate-200 pt-4">
              <Button type="button" variant="secondary" onClick={() => setCreateItemModalOpen(false)}>
                취소
              </Button>
              <Button disabled={createItemMutation.isPending} type="submit">
                생성
              </Button>
            </div>
          </form>
        </Modal>
      ) : null}

      {canManageOptionMaster ? (
        <Modal
          open={editItemModalOpen}
          title="선택 항목 수정"
          description="선택 항목 이름을 수정합니다."
          onClose={() => {
            setEditItemModalOpen(false);
            setEditingItem(null);
          }}
        >
          <form className="space-y-4" onSubmit={editItemForm.handleSubmit((values) => updateItemMutation.mutate(values))}>
            <TextField label="선택 항목 이름" error={editItemForm.formState.errors.name?.message} {...editItemForm.register('name')} />
            <ErrorMessage error={updateItemMutation.error} />
            <div className="flex justify-end gap-2 border-t border-slate-200 pt-4">
              <Button
                type="button"
                variant="secondary"
                onClick={() => {
                  setEditItemModalOpen(false);
                  setEditingItem(null);
                }}
              >
                취소
              </Button>
              <Button disabled={updateItemMutation.isPending} type="submit">
                수정
              </Button>
            </div>
          </form>
        </Modal>
      ) : null}
    </div>
  );
}
