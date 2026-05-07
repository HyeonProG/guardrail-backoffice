import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, useSearchParams } from 'react-router-dom';
import { getCategories } from '@/entities/category/api/categoryApi';
import { createProductOption, updateProductOption } from '@/entities/productOption/api/productOptionApi';
import type { ProductOption } from '@/entities/productOption/model/types';
import type { UserRole } from '@/entities/user/model/types';
import { authStorage } from '@/features/auth/model/authStorage';
import { productOptionSchema, type ProductOptionFormValues } from '@/pages/productOptions/productOptionSchema';
import { formatDateTime, shortId } from '@/shared/lib/format';
import { requireActorId } from '@/shared/lib/session';
import { Button } from '@/shared/ui/Button';
import { ErrorMessage } from '@/shared/ui/ErrorMessage';
import { Modal } from '@/shared/ui/Modal';
import { Pagination } from '@/shared/ui/Pagination';
import { TextField } from '@/shared/ui/TextField';
import { getProductOptions } from '@/entities/productOption/api/productOptionApi';

/** 상품 옵션 목록 페이지 */
export function ProductOptionsPage() {
  const pageSize = 10;
  const currentRole = (authStorage.getRole() as UserRole | null) ?? null;
  const canManageOptionMaster = currentRole === 'ADMIN' || currentRole === 'OPERATOR';
  const [searchParams, setSearchParams] = useSearchParams();
  const categoryId = searchParams.get('categoryId') ?? '';
  const queryClient = useQueryClient();
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [editingOption, setEditingOption] = useState<ProductOption | null>(null);
  const [page, setPage] = useState(0);
  const [sortDirection, setSortDirection] = useState<'desc' | 'asc'>('desc');

  const categoriesQuery = useQuery({
    queryKey: ['categories', 'option-selector'],
    queryFn: () => getCategories({ size: 100 })
  });
  const optionsQuery = useQuery({
    queryKey: ['product-options', categoryId],
    queryFn: () => getProductOptions(categoryId),
    enabled: Boolean(categoryId)
  });

  const createForm = useForm<ProductOptionFormValues>({
    resolver: zodResolver(productOptionSchema),
    defaultValues: { name: '' }
  });
  const editForm = useForm<ProductOptionFormValues>({
    resolver: zodResolver(productOptionSchema),
    defaultValues: { name: '' }
  });

  const categories = categoriesQuery.data?.content ?? [];
  const selectedCategory = useMemo(
    () => categories.find((category) => category.id === categoryId) ?? null,
    [categories, categoryId]
  );
  const options = optionsQuery.data ?? [];
  const sortedOptions = useMemo(
    () =>
      [...options].sort((a, b) => {
        const timeA = new Date(a.updatedAt).getTime();
        const timeB = new Date(b.updatedAt).getTime();
        return sortDirection === 'desc' ? timeB - timeA : timeA - timeB;
      }),
    [options, sortDirection]
  );
  const pagedOptions = useMemo(
    () => sortedOptions.slice(page * pageSize, (page + 1) * pageSize),
    [sortedOptions, page]
  );
  const totalPages = Math.ceil(sortedOptions.length / pageSize);

  const refresh = () => {
    queryClient.invalidateQueries({ queryKey: ['product-options', categoryId] });
  };

  const createMutation = useMutation({
    mutationFn: (values: ProductOptionFormValues) =>
      createProductOption(categoryId, { ...values, status: 'ACTIVE', actorId: requireActorId() }),
    onSuccess: () => {
      setCreateModalOpen(false);
      createForm.reset({ name: '' });
      refresh();
    }
  });

  const editMutation = useMutation({
    mutationFn: (values: ProductOptionFormValues) => {
      if (!editingOption) {
        throw new Error('수정 대상 옵션이 없습니다.');
      }
      return updateProductOption(categoryId, editingOption.id, { ...values, actorId: requireActorId() });
    },
    onSuccess: () => {
      setEditModalOpen(false);
      setEditingOption(null);
      editForm.reset({ name: '' });
      refresh();
    }
  });

  const startEdit = (option: ProductOption) => {
    setEditingOption(option);
    editForm.reset({ name: option.name });
    setEditModalOpen(true);
  };

  return (
    <div className="space-y-6">
      <section className="page-header">
        <div>
          <p className="section-kicker">Option Matrix</p>
          <h2 className="page-title">상품 옵션 관리</h2>
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
          <label className="text-sm font-medium text-slate-700">
            대상 카테고리
            <select
              className="ml-2 h-11 min-w-64 rounded-2xl border border-slate-200 bg-white/90 px-4 text-sm shadow-sm"
              value={categoryId}
              onChange={(event) => {
                const nextCategoryId = event.target.value;
                setPage(0);
                setSearchParams(nextCategoryId ? { categoryId: nextCategoryId } : {});
              }}
            >
              <option value="">카테고리 선택</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </label>
          {canManageOptionMaster ? (
            <Button type="button" disabled={!categoryId} onClick={() => setCreateModalOpen(true)}>
              옵션 생성
            </Button>
          ) : null}
        </div>
      </section>

      {selectedCategory ? (
        <div className="surface-section text-sm text-slate-600">
          선택 카테고리: <span className="font-medium text-slate-900">{selectedCategory.name}</span>
        </div>
      ) : (
        <div className="surface-section border-dashed text-center text-sm text-slate-500">
          카테고리를 먼저 선택하면 해당 카테고리에 사용할 옵션을 관리할 수 있습니다.
        </div>
      )}

      {categoryId ? (
        <section className="table-shell">
          <table className="table-base min-w-[780px]">
            <thead>
              <tr>
                <th className="px-4 py-3">ID</th>
                <th className="px-4 py-3">옵션명</th>
                {currentRole !== 'STAFF' ? <th className="px-4 py-3">상태</th> : null}
                <th className="px-4 py-3">수정일</th>
                <th className="px-4 py-3">액션</th>
              </tr>
            </thead>
            <tbody>
              {pagedOptions.map((option) => (
                <tr key={option.id}>
                  <td className="px-4 py-3 font-mono text-xs text-slate-600">{shortId(option.id)}</td>
                  <td className="px-4 py-3 font-medium text-ink">{option.name}</td>
                  {currentRole !== 'STAFF' ? <td className="px-4 py-3">{option.status}</td> : null}
                  <td className="px-4 py-3 text-slate-600">{formatDateTime(option.updatedAt)}</td>
                  <td className="px-4 py-3">
                    <div className="flex flex-wrap gap-2">
                      <Link
                        className="inline-flex h-11 items-center justify-center rounded-xl border border-slate-200 bg-white/90 px-4 text-sm font-semibold text-slate-800 shadow-sm transition duration-200 hover:-translate-y-0.5 hover:border-slate-300 hover:bg-white hover:shadow-md"
                        to={`/product-options/${option.id}?categoryId=${categoryId}`}
                      >
                        상세
                      </Link>
                    {canManageOptionMaster ? (
                      <Button type="button" variant="secondary" onClick={() => startEdit(option)}>
                        수정
                      </Button>
                    ) : null}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {optionsQuery.isLoading ? <div className="p-5 text-sm text-slate-600">조회 중입니다.</div> : null}
          {sortedOptions.length === 0 && !optionsQuery.isLoading ? (
            <div className="p-5 text-sm text-slate-600">등록된 옵션이 없습니다.</div>
          ) : null}
          {sortedOptions.length > 0 ? (
            <div className="border-t border-slate-200 px-4 py-4">
              <Pagination currentPage={page} totalPages={totalPages} onPageChange={setPage} />
            </div>
          ) : null}
          <ErrorMessage error={optionsQuery.error} />
        </section>
      ) : null}

      {canManageOptionMaster ? (
        <Modal
          open={createModalOpen}
          title="옵션 생성"
          description="선택한 카테고리에 사용할 옵션을 추가합니다."
          onClose={() => {
            setCreateModalOpen(false);
            createForm.reset({ name: '' });
          }}
        >
          <form className="space-y-4" onSubmit={createForm.handleSubmit((values) => createMutation.mutate(values))}>
            <TextField label="옵션 이름" error={createForm.formState.errors.name?.message} {...createForm.register('name')} />
            <p className="rounded-md border border-dashed border-border bg-slate-50 px-3 py-2 text-xs text-slate-500">
              정렬 순서는 생성 시 자동으로 부여됩니다.
            </p>
            <ErrorMessage error={createMutation.error} />
            <div className="flex justify-end gap-2 border-t border-slate-200 pt-4">
              <Button type="button" variant="secondary" onClick={() => setCreateModalOpen(false)}>
                취소
              </Button>
              <Button disabled={createMutation.isPending} type="submit">
                생성
              </Button>
            </div>
          </form>
        </Modal>
      ) : null}

      {canManageOptionMaster ? (
        <Modal
          open={editModalOpen}
          title="옵션 수정"
          description="옵션 이름을 수정합니다."
          onClose={() => {
            setEditModalOpen(false);
            setEditingOption(null);
            editForm.reset({ name: '' });
          }}
        >
          <form className="space-y-4" onSubmit={editForm.handleSubmit((values) => editMutation.mutate(values))}>
            <TextField label="옵션 이름" error={editForm.formState.errors.name?.message} {...editForm.register('name')} />
            <ErrorMessage error={editMutation.error} />
            <div className="flex justify-end gap-2 border-t border-slate-200 pt-4">
              <Button
                type="button"
                variant="secondary"
                onClick={() => {
                  setEditModalOpen(false);
                  setEditingOption(null);
                  editForm.reset({ name: '' });
                }}
              >
                취소
              </Button>
              <Button disabled={editMutation.isPending} type="submit">
                수정
              </Button>
            </div>
          </form>
        </Modal>
      ) : null}
    </div>
  );
}
