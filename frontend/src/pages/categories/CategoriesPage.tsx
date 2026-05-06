import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import {
  createCategory,
  deleteCategory,
  getDeletedCategories,
  getCategories,
  restoreCategory,
  updateCategory,
  updateCategoryStatus
} from '@/entities/category/api/categoryApi';
import type { Category, CategoryStatus } from '@/entities/category/model/types';
import type { UserRole } from '@/entities/user/model/types';
import { authStorage } from '@/features/auth/model/authStorage';
import { categorySchema, type CategoryFormValues } from '@/pages/categories/categorySchema';
import { Button } from '@/shared/ui/Button';
import { ErrorMessage } from '@/shared/ui/ErrorMessage';
import { Modal } from '@/shared/ui/Modal';
import { Pagination } from '@/shared/ui/Pagination';
import { TextField } from '@/shared/ui/TextField';
import { formatDateTime, shortId } from '@/shared/lib/format';

const categoryStatuses: CategoryStatus[] = ['ACTIVE', 'INACTIVE'];

export function CategoriesPage() {
  const pageSize = 10;
  const queryClient = useQueryClient();
  const currentRole = (authStorage.getRole() as UserRole | null) ?? null;
  const canManageDangerousActions = currentRole === 'ADMIN' || currentRole === 'OPERATOR';
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [page, setPage] = useState(0);
  const [currentParentId, setCurrentParentId] = useState<string | null>(null);
  const [sortDirection, setSortDirection] = useState<'desc' | 'asc'>('desc');
  const [statusFilter, setStatusFilter] = useState<CategoryStatus | ''>(
    currentRole === 'STAFF' ? 'ACTIVE' : ''
  );
  const [editingCategory, setEditingCategory] = useState<Category | null>(null);
  const [editModalOpen, setEditModalOpen] = useState(false);
  const [restoreModalOpen, setRestoreModalOpen] = useState(false);
  const [confirmState, setConfirmState] = useState<{
    title: string;
    description: string;
    actionLabel: string;
    onConfirm: () => void;
  } | null>(null);

  const categoriesQuery = useQuery({
    queryKey: ['categories', statusFilter, page, sortDirection, currentParentId],
    queryFn: () =>
      getCategories({
        status: statusFilter,
        page,
        size: pageSize,
        sort: `updatedAt,${sortDirection}`,
        parentId: currentParentId ?? undefined,
        rootOnly: currentParentId === null
      })
  });
  const activeCategoriesQuery = useQuery({
    queryKey: ['categories', 'active-options'],
    queryFn: () => getCategories({ status: 'ACTIVE', size: 100 })
  });
  const allCategoriesQuery = useQuery({
    queryKey: ['categories', 'all-options'],
    queryFn: () => getCategories({ size: 200, sort: 'name,asc' })
  });
  const deletedCategoriesQuery = useQuery({
    queryKey: ['categories', 'deleted'],
    queryFn: getDeletedCategories,
    enabled: canManageDangerousActions && restoreModalOpen
  });

  const categories = categoriesQuery.data?.content ?? [];
  const visibleCategories =
    currentRole === 'STAFF' ? categories.filter((category) => category.status === 'ACTIVE') : categories;
  const allCategories = allCategoriesQuery.data?.content ?? [];
  const parentOptions = useMemo(
    () => (activeCategoriesQuery.data?.content ?? []).filter((category) => category.id !== editingCategory?.id),
    [activeCategoriesQuery.data?.content, editingCategory?.id]
  );
  const currentParent = allCategories.find((category) => category.id === currentParentId) ?? null;
  const breadcrumb = useMemo(() => {
    const chain: Category[] = [];
    let parentId = currentParentId;

    while (parentId) {
      const current = allCategories.find((category) => category.id === parentId);
      if (!current) {
        break;
      }

      chain.unshift(current);
      parentId = current.parentId;
    }

    return chain;
  }, [allCategories, currentParentId]);

  const form = useForm<CategoryFormValues>({
    resolver: zodResolver(categorySchema),
    defaultValues: { parentId: '', name: '' }
  });
  const editForm = useForm<CategoryFormValues>({
    resolver: zodResolver(categorySchema),
    defaultValues: { parentId: '', name: '' }
  });

  const refresh = () => {
    queryClient.invalidateQueries({ queryKey: ['categories'] });
  };

  const saveMutation = useMutation({
    mutationFn: (values: CategoryFormValues) => createCategory(values),
    onSuccess: () => {
      setCreateModalOpen(false);
      setPage(0);
      form.reset({ parentId: currentParentId ?? '', name: '' });
      refresh();
    }
  });
  const editMutation = useMutation({
    mutationFn: (values: CategoryFormValues) => {
      if (!editingCategory) {
        throw new Error('수정 대상 카테고리가 없습니다.');
      }
      return updateCategory(editingCategory.id, values);
    },
    onSuccess: () => {
      setEditModalOpen(false);
      setEditingCategory(null);
      editForm.reset({ parentId: '', name: '' });
      refresh();
    }
  });
  const statusMutation = useMutation({
    mutationFn: ({ categoryId, status }: { categoryId: string; status: CategoryStatus }) =>
      updateCategoryStatus(categoryId, status),
    onSuccess: refresh
  });
  const deleteMutation = useMutation({ mutationFn: deleteCategory, onSuccess: refresh });
  const restoreMutation = useMutation({
    mutationFn: restoreCategory,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['categories', 'deleted'] });
      refresh();
    }
  });

  const startEdit = (category: Category) => {
    setEditingCategory(category);
    editForm.reset({ parentId: category.parentId ?? '', name: category.name });
    setEditModalOpen(true);
  };

  const moveToDepth = (parentId: string | null) => {
    setCurrentParentId(parentId);
    setPage(0);
  };

  return (
    <div className="space-y-6">
      <section className="page-header">
        <div>
          <p className="section-kicker">Category Tree</p>
          <h2 className="page-title">카테고리 관리</h2>
          <div className="mt-2 flex flex-wrap items-center gap-2 text-sm text-slate-600">
            {currentParentId !== null ? (
              <>
                <Button
                  type="button"
                  variant="secondary"
                  onClick={() => moveToDepth(currentParent?.parentId ?? null)}
                >
                  뒤로가기
                </Button>
                <span>/</span>
              </>
            ) : null}
            <button
              type="button"
              className={currentParentId === null ? 'font-semibold text-ink' : 'hover:text-ink'}
              onClick={() => moveToDepth(null)}
            >
              최상위
            </button>
            {breadcrumb.map((category) => (
              <div key={category.id} className="flex items-center gap-2">
                <span>/</span>
                <button
                  type="button"
                  className={category.id === currentParentId ? 'font-semibold text-ink' : 'hover:text-ink'}
                  onClick={() => moveToDepth(category.id)}
                >
                  {category.name}
                </button>
              </div>
            ))}
          </div>
          <p className="mt-2 text-sm text-slate-600">
            {currentParent ? `${currentParent.name} 하위 카테고리 목록입니다.` : '최상위 카테고리 목록입니다.'}
          </p>
        </div>
        <div className="flex items-center gap-3">
          {currentRole !== 'STAFF' ? (
            <label className="text-sm font-medium text-slate-700">
              상태
              <select
                className="ml-2 h-11 rounded-2xl border border-slate-200 bg-white/90 px-4 text-sm shadow-sm"
                value={statusFilter}
                onChange={(event) => {
                  setPage(0);
                  setStatusFilter(event.target.value as CategoryStatus | '');
                }}
              >
                <option value="">전체</option>
                {categoryStatuses.map((status) => (
                  <option key={status} value={status}>
                    {status}
                  </option>
                ))}
              </select>
            </label>
          ) : null}
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
          {canManageDangerousActions ? (
            <Button
              type="button"
              onClick={() => {
                form.reset({ parentId: currentParentId ?? '', name: '' });
                setCreateModalOpen(true);
              }}
            >
              카테고리 생성
            </Button>
          ) : null}
          {canManageDangerousActions ? (
            <Button
              type="button"
              variant="secondary"
              onClick={() => setRestoreModalOpen(true)}
            >
              숨긴 카테고리 복구
            </Button>
          ) : null}
        </div>
      </section>

      <section>
        <div className="table-shell">
          <table className="table-base min-w-[760px]">
            <thead>
              <tr>
                <th className="px-4 py-3">ID</th>
                <th className="px-4 py-3">이름</th>
                <th className="px-4 py-3">상위 카테고리</th>
                {currentRole !== 'STAFF' ? <th className="px-4 py-3">상태</th> : null}
                <th className="px-4 py-3">수정일</th>
                <th className="px-4 py-3">액션</th>
              </tr>
            </thead>
            <tbody>
              {visibleCategories.map((category) => (
                <tr key={category.id}>
                  <td className="px-4 py-3 font-mono text-xs text-slate-600">{shortId(category.id)}</td>
                  <td className="px-4 py-3 font-medium text-ink">{category.name}</td>
                  <td className="px-4 py-3 text-slate-600">
                    {(activeCategoriesQuery.data?.content ?? []).find((parent) => parent.id === category.parentId)?.name ?? '-'}
                  </td>
                  {currentRole !== 'STAFF' ? <td className="px-4 py-3">{category.status}</td> : null}
                  <td className="px-4 py-3 text-slate-600">{formatDateTime(category.updatedAt)}</td>
                  <td className="space-x-2 px-4 py-3">
                    <Button type="button" variant="secondary" onClick={() => moveToDepth(category.id)}>
                      하위 보기
                    </Button>
                    {canManageDangerousActions ? (
                      <>
                        <Button type="button" variant="secondary" onClick={() => startEdit(category)}>
                          수정
                        </Button>
                        <Button
                          type="button"
                          variant="secondary"
                          onClick={() =>
                            setConfirmState({
                              title: `카테고리 ${category.status === 'ACTIVE' ? '비활성화' : '활성화'} 확인`,
                              description: `${category.name} 카테고리를 ${category.status === 'ACTIVE' ? '비활성' : '활성'} 상태로 변경합니다.`,
                              actionLabel: category.status === 'ACTIVE' ? '비활성화' : '활성화',
                              onConfirm: () =>
                                statusMutation.mutate({
                                  categoryId: category.id,
                                  status: category.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'
                                })
                            })
                          }
                        >
                          {category.status === 'ACTIVE' ? '비활성' : '활성'}
                        </Button>
                        <Button
                          type="button"
                          variant="ghost"
                          onClick={() =>
                            setConfirmState({
                              title: '카테고리 삭제 확인',
                              description: `${category.name} 카테고리를 삭제합니다. 삭제 후에는 복구 화면에서만 되돌릴 수 있습니다.`,
                              actionLabel: '삭제하기',
                              onConfirm: () => deleteMutation.mutate(category.id)
                            })
                          }
                        >
                          삭제
                        </Button>
                      </>
                    ) : null}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          {categoriesQuery.isLoading ? <div className="p-5 text-sm text-slate-600">조회 중입니다.</div> : null}
          {visibleCategories.length === 0 && !categoriesQuery.isLoading ? (
            <div className="p-5 text-sm text-slate-600">조회된 카테고리가 없습니다.</div>
          ) : null}
          {visibleCategories.length > 0 ? (
            <div className="border-t border-slate-200 px-4 py-4">
              <Pagination
                currentPage={page}
                totalPages={categoriesQuery.data?.totalPages ?? 0}
                onPageChange={setPage}
              />
            </div>
          ) : null}
        </div>
      </section>
      <ErrorMessage error={categoriesQuery.error ?? statusMutation.error ?? deleteMutation.error} />

      {canManageDangerousActions ? (
        <Modal
          open={createModalOpen}
          title="카테고리 생성"
          description="상위 카테고리와 카테고리명을 입력해 새 카테고리를 생성합니다."
          onClose={() => {
            setCreateModalOpen(false);
            form.reset({ parentId: currentParentId ?? '', name: '' });
          }}
        >
          <form
            className="space-y-4"
            onSubmit={form.handleSubmit((values) => {
              const targetName = values.name.trim();

              setConfirmState({
                title: '카테고리 생성 확인',
                description: `"${targetName}" 카테고리를 생성하시겠습니까?`,
                actionLabel: '생성하기',
                onConfirm: () => saveMutation.mutate(values)
              });
            })}
          >
            <label className="block">
              <span className="mb-2 block text-sm font-semibold tracking-[0.01em] text-slate-700">상위 카테고리</span>
              <select className="h-12 w-full rounded-2xl border border-slate-200 bg-white/90 px-4 text-sm shadow-sm" {...form.register('parentId')}>
                <option value="">최상위 카테고리</option>
                {parentOptions.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </select>
            </label>
            <TextField label="카테고리명" error={form.formState.errors.name?.message} {...form.register('name')} />
            <ErrorMessage error={saveMutation.error} />
            <div className="flex justify-end gap-2 border-t border-slate-200 pt-4">
              <Button
                type="button"
                variant="secondary"
                onClick={() => {
                  setCreateModalOpen(false);
                  form.reset({ parentId: currentParentId ?? '', name: '' });
                }}
              >
                취소
              </Button>
              <Button disabled={saveMutation.isPending} type="submit">
                생성
              </Button>
            </div>
          </form>
        </Modal>
      ) : null}

      <Modal
        open={Boolean(confirmState)}
        title={confirmState?.title ?? ''}
        description={confirmState?.description}
        zIndex={80}
        onClose={() => setConfirmState(null)}
      >
        <div className="flex justify-end gap-2">
          <Button type="button" variant="secondary" onClick={() => setConfirmState(null)}>
            취소
          </Button>
          <Button
            type="button"
            onClick={() => {
              confirmState?.onConfirm();
              setConfirmState(null);
            }}
          >
            {confirmState?.actionLabel ?? '확인'}
          </Button>
        </div>
      </Modal>

      <Modal
        open={editModalOpen}
        title="카테고리 수정"
        description="카테고리를 수정합니다."
        zIndex={70}
        onClose={() => {
          setEditModalOpen(false);
          setEditingCategory(null);
          editForm.reset({ parentId: '', name: '' });
        }}
      >
        <form
          className="space-y-4"
          onSubmit={editForm.handleSubmit((values) => {
            const targetName = values.name.trim();
            setConfirmState({
              title: '카테고리 수정 확인',
              description: `"${targetName}" 카테고리로 수정하시겠습니까?`,
              actionLabel: '수정하기',
              onConfirm: () => editMutation.mutate(values)
            });
          })}
        >
          <label className="block">
              <span className="mb-2 block text-sm font-semibold tracking-[0.01em] text-slate-700">상위 카테고리</span>
              <select className="h-12 w-full rounded-2xl border border-slate-200 bg-white/90 px-4 text-sm shadow-sm" {...editForm.register('parentId')}>
              <option value="">최상위 카테고리</option>
              {parentOptions.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </label>
          <TextField label="카테고리명" error={editForm.formState.errors.name?.message} {...editForm.register('name')} />
          <ErrorMessage error={editMutation.error} />
          <div className="flex justify-end gap-2 border-t border-slate-200 pt-4">
            <Button
              type="button"
              variant="secondary"
              onClick={() => {
                setEditModalOpen(false);
                setEditingCategory(null);
                editForm.reset({ parentId: '', name: '' });
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

      <Modal
        open={restoreModalOpen}
        title="숨긴 카테고리 복구"
        description="삭제되어 목록에서 보이지 않는 카테고리를 다시 사용할 수 있게 복구합니다."
        zIndex={60}
        onClose={() => setRestoreModalOpen(false)}
      >
        <div className="space-y-4">
          {deletedCategoriesQuery.isLoading ? (
            <p className="text-sm text-slate-600">복구할 수 있는 카테고리를 불러오는 중입니다.</p>
          ) : null}
          {(deletedCategoriesQuery.data ?? []).map((category) => (
            <div
              key={category.id}
              className="flex items-center justify-between gap-4 rounded-lg border border-border px-4 py-3"
            >
              <div>
                <p className="font-medium text-ink">{category.name}</p>
                <p className="mt-1 text-xs text-slate-500">
                  {shortId(category.id)} · {formatDateTime(category.updatedAt)}
                </p>
              </div>
              <Button
                type="button"
                variant="secondary"
                disabled={restoreMutation.isPending}
                onClick={() =>
                  setConfirmState({
                    title: '카테고리 복구 확인',
                    description: `${category.name} 카테고리를 복구합니다.`,
                    actionLabel: '복구하기',
                    onConfirm: () => restoreMutation.mutate(category.id)
                  })
                }
              >
                복구
              </Button>
            </div>
          ))}
          {!deletedCategoriesQuery.isLoading && (deletedCategoriesQuery.data ?? []).length === 0 ? (
            <p className="text-sm text-slate-600">지금은 다시 복구할 수 있는 카테고리가 없습니다.</p>
          ) : null}
          <ErrorMessage error={deletedCategoriesQuery.error ?? restoreMutation.error} />
        </div>
      </Modal>
    </div>
  );
}
