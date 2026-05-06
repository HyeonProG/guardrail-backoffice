import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect, useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import { Link, Navigate, useNavigate, useParams } from 'react-router-dom';
import { getCategories } from '@/entities/category/api/categoryApi';
import type { Category } from '@/entities/category/model/types';
import {
  deleteFileAttachment,
  getFileAttachments,
  uploadFileAttachment
} from '@/entities/file/api/fileAttachmentApi';
import { generateProductDescription, getProduct, updateProduct } from '@/entities/product/api/productApi';
import type { Product } from '@/entities/product/model/types';
import { getProductOptions } from '@/entities/productOption/api/productOptionApi';
import type { UserRole } from '@/entities/user/model/types';
import { authStorage } from '@/features/auth/model/authStorage';
import { productSchema, type ProductFormValues } from '@/pages/products/productSchema';
import { formatDateTime } from '@/shared/lib/format';
import { resolveFileUrl } from '@/shared/lib/fileUrl';
import { getProductStatusLabel } from '@/shared/lib/productText';
import { requireActorId } from '@/shared/lib/session';
import { Button } from '@/shared/ui/Button';
import { ErrorMessage } from '@/shared/ui/ErrorMessage';
import { Modal } from '@/shared/ui/Modal';
import { TextArea } from '@/shared/ui/TextArea';
import { TextField } from '@/shared/ui/TextField';

type PendingImage = {
  id: string;
  file: File;
};

export function ProductEditPage() {
  const { productId = '' } = useParams();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const currentRole = (authStorage.getRole() as UserRole | null) ?? null;
  const canEditRole =
    currentRole === 'ADMIN' || currentRole === 'OPERATOR' || currentRole === 'STAFF';
  const [categoryModalOpen, setCategoryModalOpen] = useState(false);
  const [categorySearch, setCategorySearch] = useState('');
  const [categoryParentId, setCategoryParentId] = useState<string | null>(null);
  const [pendingImages, setPendingImages] = useState<PendingImage[]>([]);
  const [attachmentNotice, setAttachmentNotice] = useState<string | null>(null);
  const [previewIndex, setPreviewIndex] = useState(0);
  const [updateConfirmOpen, setUpdateConfirmOpen] = useState(false);
  const [updateCompleteOpen, setUpdateCompleteOpen] = useState(false);
  const form = useForm<ProductFormValues>({
    resolver: zodResolver(productSchema),
    defaultValues: { categoryId: '', name: '', description: '', quantity: 0, selectedOptionItemIds: [] }
  });

  const selectedCategoryId = form.watch('categoryId');
  const descriptionValue = form.watch('description') ?? '';
  const selectedOptionItemIds = form.watch('selectedOptionItemIds') ?? [];

  const productQuery = useQuery({
    queryKey: ['products', productId],
    queryFn: () => getProduct(productId),
    enabled: Boolean(productId && canEditRole)
  });
  const categoriesQuery = useQuery({
    queryKey: ['categories', 'active-options'],
    queryFn: () => getCategories({ status: 'ACTIVE', size: 100 }),
    enabled: canEditRole
  });
  const optionPreviewQuery = useQuery({
    queryKey: ['product-options', selectedCategoryId, 'preview'],
    queryFn: () => getProductOptions(selectedCategoryId, 'ACTIVE'),
    enabled: Boolean(selectedCategoryId && canEditRole)
  });
  const attachmentsQuery = useQuery({
    queryKey: ['file-attachments', 'PRODUCT', productId],
    queryFn: () => getFileAttachments('PRODUCT', productId),
    enabled: Boolean(productId && canEditRole)
  });

  const selectedCategory = useMemo(
    () => (categoriesQuery.data?.content ?? []).find((category) => category.id === selectedCategoryId) ?? null,
    [categoriesQuery.data?.content, selectedCategoryId]
  );
  const allCategories = categoriesQuery.data?.content ?? [];
  const currentCategoryParent = allCategories.find((category) => category.id === categoryParentId) ?? null;
  const categoryBreadcrumb = useMemo(() => {
    const chain: Category[] = [];
    let parentId = categoryParentId;

    while (parentId) {
      const current = allCategories.find((category) => category.id === parentId);
      if (!current) {
        break;
      }

      chain.unshift(current);
      parentId = current.parentId;
    }

    return chain;
  }, [allCategories, categoryParentId]);
  const filteredCategories = useMemo(() => {
    const keyword = categorySearch.trim().toLowerCase();
    const categories = allCategories.filter((category) => category.parentId === categoryParentId);
    if (!keyword) {
      return categories;
    }
    return categories.filter((category) => category.name.toLowerCase().includes(keyword));
  }, [allCategories, categoryParentId, categorySearch]);
  const childCategoryIds = useMemo(
    () =>
      new Set(
        allCategories
          .filter((category) => category.parentId != null)
          .map((category) => category.parentId as string)
      ),
    [allCategories]
  );
  const optionGroups = optionPreviewQuery.data ?? [];
  const uploadedAttachments = [...(attachmentsQuery.data ?? [])].sort((a, b) => a.sortOrder - b.sortOrder);
  const pendingPreviewUrls = useMemo(
    () =>
      pendingImages.map((entry) => ({
        id: entry.id,
        url: URL.createObjectURL(entry.file),
        name: entry.file.name,
        pending: true as const
      })),
    [pendingImages]
  );
  const imagePreviewItems = useMemo(
    () => [
      ...uploadedAttachments.map((attachment) => ({
        id: attachment.id,
        url: resolveFileUrl(attachment.filePath) ?? '',
        name: attachment.originalFileName,
        pending: false as const
      })),
      ...pendingPreviewUrls
    ],
    [pendingPreviewUrls, uploadedAttachments]
  );
  useEffect(() => {
    if (productQuery.data) {
      form.reset({
        categoryId: productQuery.data.categoryId,
        name: productQuery.data.name,
        description: productQuery.data.description,
        quantity: productQuery.data.quantity,
        selectedOptionItemIds: productQuery.data.selectedOptions.map((option) => option.productOptionItemId)
      });
    }
  }, [form, productQuery.data]);

  useEffect(() => {
    if (!categoryModalOpen) {
      return;
    }

    if (!selectedCategoryId) {
      setCategoryParentId(null);
      return;
    }

    const selected = allCategories.find((category) => category.id === selectedCategoryId);
    setCategoryParentId(selected?.parentId ?? null);
  }, [allCategories, categoryModalOpen, selectedCategoryId]);

  useEffect(() => {
    return () => {
      pendingPreviewUrls.forEach((item) => URL.revokeObjectURL(item.url));
    };
  }, [pendingPreviewUrls]);

  useEffect(() => {
    if (previewIndex > Math.max(0, imagePreviewItems.length - 1)) {
      setPreviewIndex(Math.max(0, imagePreviewItems.length - 1));
    }
  }, [imagePreviewItems.length, previewIndex]);

  const updateMutation = useMutation({
    mutationFn: (values: ProductFormValues) =>
      updateProduct(productId, {
        ...values,
        description: values.description?.trim() ?? '',
        selectedOptionItemIds: values.selectedOptionItemIds,
        actorId: requireActorId()
      }),
    onSuccess: async (product) => {
      await registerPendingImages(product.id);
      await queryClient.invalidateQueries({ queryKey: ['products'] });
      await queryClient.invalidateQueries({ queryKey: ['products', productId] });
      await queryClient.invalidateQueries({ queryKey: ['file-attachments', 'PRODUCT', productId] });
      setAttachmentNotice('상품 정보를 저장했습니다.');
      setUpdateConfirmOpen(false);
      setUpdateCompleteOpen(true);
    }
  });

  const generateDescriptionMutation = useMutation({
    mutationFn: async () => {
      const keywordText = descriptionValue.trim();
      if (!keywordText) {
        throw new Error('키워드를 입력해 주세요.');
      }

      const product = await updateMutation.mutateAsync(form.getValues());
      return generateProductDescription(product.id, {
        actorId: requireActorId(),
        productName: form.getValues('name'),
        categoryName: selectedCategory?.name ?? '',
        optionSummary: '',
        featureKeywords: keywordText
          .split(/[\n,]/)
          .map((keyword) => keyword.trim())
          .filter(Boolean)
      });
    },
    onSuccess: async (product) => {
      form.setValue('description', product.description ?? '', { shouldDirty: true, shouldValidate: true });
      await queryClient.invalidateQueries({ queryKey: ['products', productId] });
      setAttachmentNotice('AI 문구를 설명란에 반영했습니다.');
    }
  });

  const deleteAttachmentMutation = useMutation({
    mutationFn: (fileAttachmentId: string) => deleteFileAttachment(fileAttachmentId),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['file-attachments', 'PRODUCT', productId] });
    }
  });

  async function registerPendingImages(targetProductId: string) {
    if (pendingImages.length === 0) {
      return;
    }

    const nextSortOrder = uploadedAttachments.length + 1;
    const results = await Promise.allSettled(
      pendingImages.map((entry, index) =>
        uploadFileAttachment({
          targetType: 'PRODUCT',
          targetId: targetProductId,
          sortOrder: nextSortOrder + index,
          file: entry.file
        })
      )
    );

    const failedCount = results.filter((result) => result.status === 'rejected').length;
    setPendingImages([]);

    if (failedCount > 0) {
      setAttachmentNotice(`이미지 ${failedCount}건 업로드에 실패했습니다. 다시 시도해 주세요.`);
      return;
    }

    setAttachmentNotice(`이미지 ${results.length}건을 등록했습니다.`);
  }

  function appendImages(files: File[]) {
    setPendingImages((current) => [
      ...current,
      ...files.map((file, index) => ({
        id: `${file.name}-${file.size}-${Date.now()}-${index}`,
        file
      }))
    ]);
  }

  const canEdit =
    currentRole === 'ADMIN' ||
    currentRole === 'OPERATOR' ||
    (currentRole === 'STAFF' &&
      productQuery.data != null &&
      ['DRAFT', 'PENDING', 'REJECTED'].includes(productQuery.data.status));

  if (!canEditRole) {
    return <Navigate to={`/products/${productId}`} replace />;
  }

  if (!productQuery.isLoading && productQuery.data && !canEdit) {
    return <Navigate to={`/products/${productId}`} replace />;
  }

  return (
    <div className="space-y-6">
      <section className="space-y-2">
        <div className="flex items-center justify-between gap-3">
          <div>
            <h2 className="text-xl font-semibold text-ink">상품 수정</h2>
            <p className="mt-1 text-sm text-slate-600">
              상품 기본 정보와 선택 항목, 이미지, 설명을 수정할 수 있습니다. 설명은 직접 작성하거나 AI 문구를 바로 반영할 수 있습니다.
            </p>
          </div>
          <Link className="text-sm font-medium text-slate-900 underline" to={`/products/${productId}`}>
            상품 상세로 돌아가기
          </Link>
        </div>
      </section>

      <ErrorMessage error={productQuery.error ?? categoriesQuery.error ?? optionPreviewQuery.error ?? attachmentsQuery.error} />

      <section className="rounded-2xl border border-border bg-white p-6 shadow-sm">
        <form
          className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_320px]"
          onSubmit={form.handleSubmit(async () => {
            const valid = await form.trigger();
            if (!valid) {
              return;
            }
            setUpdateConfirmOpen(true);
          })}
        >
          <div className="space-y-6">
            <div className="grid gap-4 xl:grid-cols-[minmax(0,1fr)_minmax(0,1fr)]">
              <div className="space-y-2">
                <span className="block text-sm font-medium text-slate-700">카테고리</span>
                <button
                  className="flex h-12 w-full items-center justify-between rounded-lg border border-border bg-white px-4 text-left text-sm text-ink transition hover:border-slate-400"
                  type="button"
                  onClick={() => {
                    setCategorySearch('');
                    setCategoryModalOpen(true);
                  }}
                >
                  <span>{selectedCategory?.name ?? '카테고리 선택'}</span>
                  <span className="text-slate-400">선택</span>
                </button>
                {form.formState.errors.categoryId?.message ? (
                  <span className="block text-xs font-medium text-red-600">{form.formState.errors.categoryId.message}</span>
                ) : null}
              </div>

              <TextField label="상품명" error={form.formState.errors.name?.message} {...form.register('name')} />
            </div>

            {selectedCategoryId ? (
              <div className="space-y-3 rounded-xl border border-border bg-slate-50 p-4">
                <div className="flex items-center justify-between gap-3">
                  <div>
                    <p className="text-sm font-medium text-ink">옵션 선택</p>
                    <p className="mt-1 text-xs text-slate-500">카테고리에 연결된 선택 항목을 고르고, 수정 내용은 저장 시 반영됩니다.</p>
                  </div>
                  <Link className="text-xs font-medium text-slate-900 underline" to={`/product-options?categoryId=${selectedCategoryId}`}>
                    옵션 관리
                  </Link>
                </div>
                {optionGroups.length === 0 ? (
                  <p className="text-sm text-slate-500">이 카테고리에 등록된 선택 항목이 없습니다.</p>
                ) : (
                  <div className="space-y-4">
                    {optionGroups.map((group) => (
                      <div key={group.id} className="space-y-2">
                        <p className="text-sm font-medium text-ink">{group.name}</p>
                        <div className="flex flex-wrap gap-2">
                          {group.items
                            .filter((item) => item.status === 'ACTIVE')
                            .map((item) => {
                              const checked = selectedOptionItemIds.includes(item.id);
                              return (
                                <label
                                  key={item.id}
                                  className={`inline-flex cursor-pointer items-center gap-2 rounded-full border px-3 py-2 text-sm transition ${
                                    checked
                                      ? 'border-slate-900 bg-slate-900 text-white'
                                      : 'border-border bg-white text-slate-700 hover:border-slate-400'
                                  }`}
                                >
                                  <input
                                    checked={checked}
                                    className="hidden"
                                    type="checkbox"
                                    onChange={(event) => {
                                      const current = form.getValues('selectedOptionItemIds') ?? [];
                                      form.setValue(
                                        'selectedOptionItemIds',
                                        event.target.checked
                                          ? [...current, item.id]
                                          : current.filter((value) => value !== item.id),
                                        { shouldDirty: true }
                                      );
                                    }}
                                  />
                                  <span>{item.name}</span>
                                </label>
                              );
                            })}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            ) : null}

            <div className="space-y-2">
              <div className="flex items-end justify-between gap-3">
                <div>
                  <span className="block text-sm font-medium text-slate-700">설명</span>
                </div>
                <Button
                  disabled={
                    updateMutation.isPending ||
                    generateDescriptionMutation.isPending ||
                    !selectedCategoryId ||
                    !form.watch('name').trim() ||
                    !descriptionValue.trim()
                  }
                  type="button"
                  variant="secondary"
                  onClick={() => void generateDescriptionMutation.mutateAsync()}
                >
                  AI 문구 만들기
                </Button>
              </div>
              <TextArea
                label="설명"
                hideLabel
                placeholder="설명을 직접 작성하거나, 키워드 몇 개를 입력해 AI 문구를 만들 수 있습니다."
                error={form.formState.errors.description?.message}
                {...form.register('description')}
              />
            </div>

            <div className="space-y-3 rounded-xl border border-border bg-slate-50 p-4">
              <div className="flex items-center justify-between gap-3">
                <div>
                  <p className="text-sm font-medium text-ink">상품 이미지</p>
                  <p className="mt-1 text-xs text-slate-500">이미지 추가, 미리보기, 삭제까지 한 화면에서 수정할 수 있습니다.</p>
                </div>
                <label className="inline-flex cursor-pointer items-center rounded-md bg-slate-900 px-4 py-2 text-sm font-medium text-white">
                  이미지 추가
                  <input
                    accept="image/png,image/jpeg,image/webp"
                    className="hidden"
                    multiple
                    type="file"
                    onChange={(event) => {
                      appendImages(Array.from(event.target.files ?? []));
                      event.target.value = '';
                    }}
                  />
                </label>
              </div>

              {imagePreviewItems.length === 0 ? (
                <div className="rounded-lg border border-dashed border-border bg-white px-4 py-8 text-center text-sm text-slate-500">
                  아직 등록된 이미지가 없습니다.
                </div>
              ) : (
                <div className="space-y-3">
                  <div className="overflow-hidden rounded-xl border border-border bg-white">
                    <div className="relative aspect-[16/10] bg-slate-100">
                      <img
                        alt={imagePreviewItems[previewIndex]?.name ?? '상품 이미지 미리보기'}
                        className="h-full w-full object-cover"
                        src={imagePreviewItems[previewIndex]?.url}
                      />
                      {imagePreviewItems.length > 1 ? (
                        <>
                          <button
                            className="absolute left-3 top-1/2 flex h-9 w-9 -translate-y-1/2 items-center justify-center rounded-full bg-white/90 text-lg text-ink shadow"
                            type="button"
                            onClick={() =>
                              setPreviewIndex((current) => (current === 0 ? imagePreviewItems.length - 1 : current - 1))
                            }
                          >
                            ‹
                          </button>
                          <button
                            className="absolute right-3 top-1/2 flex h-9 w-9 -translate-y-1/2 items-center justify-center rounded-full bg-white/90 text-lg text-ink shadow"
                            type="button"
                            onClick={() =>
                              setPreviewIndex((current) => (current === imagePreviewItems.length - 1 ? 0 : current + 1))
                            }
                          >
                            ›
                          </button>
                        </>
                      ) : null}
                    </div>
                    <div className="flex items-center justify-between border-t border-border px-4 py-3 text-sm">
                      <p className="truncate font-medium text-ink">
                        {previewIndex === 0 ? '대표 이미지' : `이미지 ${previewIndex + 1}`} · {imagePreviewItems[previewIndex]?.name}
                      </p>
                      <span className="text-xs text-slate-500">
                        {previewIndex + 1} / {imagePreviewItems.length}
                      </span>
                    </div>
                  </div>

                  {imagePreviewItems.length > 1 ? (
                    <div className="flex gap-2 overflow-x-auto pb-1">
                      {imagePreviewItems.map((item, index) => (
                        <button
                          key={item.id}
                          className={`relative h-16 w-16 shrink-0 overflow-hidden rounded-lg border ${
                            previewIndex === index ? 'border-slate-900 ring-2 ring-slate-200' : 'border-border'
                          }`}
                          type="button"
                          onClick={() => setPreviewIndex(index)}
                        >
                          <img alt={item.name} className="h-full w-full object-cover" src={item.url} />
                          {item.pending ? (
                            <span className="absolute bottom-1 right-1 rounded bg-slate-900/90 px-1.5 py-0.5 text-[10px] text-white">
                              예정
                            </span>
                          ) : null}
                        </button>
                      ))}
                    </div>
                  ) : null}

                  {uploadedAttachments.map((attachment, index) => (
                    <div key={attachment.id} className="flex items-center justify-between gap-4 rounded-lg border border-border bg-white px-4 py-3">
                      <div className="flex min-w-0 items-center gap-3">
                        <img
                          alt={attachment.originalFileName}
                          className="h-16 w-16 rounded-lg border border-border object-cover"
                          src={resolveFileUrl(attachment.filePath) ?? undefined}
                        />
                        <div className="min-w-0">
                          <p className="truncate text-sm font-medium text-ink">
                            {index === 0 ? '대표 이미지' : `추가 이미지 ${index}`} · {attachment.originalFileName}
                          </p>
                          <p className="mt-1 text-xs text-slate-500">{formatDateTime(attachment.createdAt)}</p>
                        </div>
                      </div>
                      <Button
                        disabled={deleteAttachmentMutation.isPending}
                        type="button"
                        variant="ghost"
                        onClick={() => deleteAttachmentMutation.mutate(attachment.id)}
                      >
                        삭제
                      </Button>
                    </div>
                  ))}

                  {pendingImages.map((entry, index) => (
                    <div key={entry.id} className="flex items-center justify-between gap-4 rounded-lg border border-dashed border-border bg-white px-4 py-3">
                      <div>
                        <p className="text-sm font-medium text-ink">
                          {uploadedAttachments.length === 0 && index === 0 ? '대표 예정 이미지' : '업로드 예정 이미지'} · {entry.file.name}
                        </p>
                        <p className="mt-1 text-xs text-slate-500">{Math.max(1, Math.round(entry.file.size / 1024))}KB · 저장 전</p>
                      </div>
                      <Button type="button" variant="ghost" onClick={() => setPendingImages((current) => current.filter((item) => item.id !== entry.id))}>
                        제거
                      </Button>
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          <div className="space-y-4">
            <div className="rounded-xl border border-border bg-slate-50 p-4">
              <p className="text-sm font-medium text-ink">현재 진행 상태</p>
              <div className="mt-3 space-y-2 text-sm text-slate-600">
                <p>선택 카테고리: {selectedCategory?.name ?? '-'}</p>
                <p>현재 상품 상태: {productQuery.data?.status ?? '-'}</p>
                <p>등록된 이미지: {uploadedAttachments.length}건</p>
                <p>업로드 예정 이미지: {pendingImages.length}건</p>
              </div>
            </div>

            <TextField
              label="수량"
              type="number"
              min={0}
              error={form.formState.errors.quantity?.message}
              {...form.register('quantity')}
            />

            <ErrorMessage
              error={
                updateMutation.error ??
                generateDescriptionMutation.error ??
                deleteAttachmentMutation.error
              }
            />
            {attachmentNotice ? <p className="text-xs font-medium text-slate-600">{attachmentNotice}</p> : null}
            <Button className="w-full" disabled={updateMutation.isPending} type="submit">
              변경 저장
            </Button>
            <p className="text-xs leading-6 text-slate-500">
              저장 시 현재 폼 내용과 선택 항목, 이미지 변경 내용이 함께 반영됩니다.
            </p>
          </div>
        </form>
      </section>

      <Modal
        open={categoryModalOpen}
        title="카테고리 선택"
        description="depth를 따라 이동하면서 카테고리를 선택할 수 있습니다."
        onClose={() => {
          setCategoryModalOpen(false);
          setCategorySearch('');
        }}
      >
        <div className="space-y-4">
          <div className="flex flex-wrap items-center gap-2 text-sm text-slate-600">
            {categoryParentId !== null ? (
              <>
                <Button
                  type="button"
                  variant="secondary"
                  onClick={() => setCategoryParentId(currentCategoryParent?.parentId ?? null)}
                >
                  뒤로가기
                </Button>
                <span>/</span>
              </>
            ) : null}
            <button
              type="button"
              className={categoryParentId === null ? 'font-semibold text-ink' : 'hover:text-ink'}
              onClick={() => setCategoryParentId(null)}
            >
              최상위
            </button>
            {categoryBreadcrumb.map((category) => (
              <div key={category.id} className="flex items-center gap-2">
                <span>/</span>
                <button
                  type="button"
                  className={category.id === categoryParentId ? 'font-semibold text-ink' : 'hover:text-ink'}
                  onClick={() => setCategoryParentId(category.id)}
                >
                  {category.name}
                </button>
              </div>
            ))}
          </div>
          <TextField
            label="카테고리 검색"
            placeholder="현재 depth에서 카테고리명 검색"
            value={categorySearch}
            onChange={(event) => setCategorySearch(event.target.value)}
          />
          <div className="max-h-[420px] overflow-y-auto rounded-lg border border-border">
            {filteredCategories.length === 0 ? (
              <div className="px-4 py-8 text-center text-sm text-slate-500">검색 결과가 없습니다.</div>
            ) : (
              <div className="divide-y divide-border">
                {filteredCategories.map((category) => (
                  <div
                    key={category.id}
                    className="flex items-center justify-between gap-3 px-4 py-3 transition hover:bg-slate-50"
                  >
                    <button
                      className="min-w-0 flex-1 text-left"
                      type="button"
                      onClick={() => {
                        form.setValue('categoryId', category.id, { shouldValidate: true });
                        setCategoryModalOpen(false);
                        setCategorySearch('');
                      }}
                    >
                      <p className="text-sm font-medium text-ink">{category.name}</p>
                      <p className="mt-1 text-xs text-slate-500">{category.status}</p>
                    </button>
                    <div className="flex items-center gap-2">
                      {childCategoryIds.has(category.id) ? (
                        <Button type="button" variant="secondary" onClick={() => setCategoryParentId(category.id)}>
                          하위 보기
                        </Button>
                      ) : null}
                      {selectedCategoryId === category.id ? (
                        <span className="rounded-full bg-slate-900 px-3 py-1 text-xs font-medium text-white">선택됨</span>
                      ) : null}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </Modal>

      <Modal
        open={updateConfirmOpen}
        title="상품 수정 확인"
        description="현재 입력한 내용으로 상품을 수정하시겠습니까?"
        onClose={() => setUpdateConfirmOpen(false)}
      >
        <div className="space-y-4">
          <div className="rounded-xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
            <p>
              상품명: <span className="font-medium text-slate-900">{form.getValues('name') || '-'}</span>
            </p>
            <p className="mt-2">
              카테고리: <span className="font-medium text-slate-900">{selectedCategory?.name ?? '-'}</span>
            </p>
            <p className="mt-2">
              이미지: <span className="font-medium text-slate-900">{uploadedAttachments.length + pendingImages.length}건</span>
            </p>
            <div className="mt-2">
              <p>
                설명:
                <span className="ml-1 font-medium text-slate-900">
                  {form.getValues('description')?.trim() || '입력된 설명 없음'}
                </span>
              </p>
            </div>
          </div>
          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={() => setUpdateConfirmOpen(false)}>
              취소
            </Button>
            <Button
              type="button"
              disabled={updateMutation.isPending}
              onClick={() => updateMutation.mutate(form.getValues())}
            >
              수정하기
            </Button>
          </div>
        </div>
      </Modal>

      <Modal
        open={updateCompleteOpen}
        title="상품 수정 완료"
        description="상품 정보 수정이 완료되었습니다."
        onClose={() => {
          setUpdateCompleteOpen(false);
          navigate(0);
        }}
      >
        <div className="space-y-4">
          <div className="rounded-xl border border-emerald-100 bg-emerald-50 p-4 text-sm leading-6 text-emerald-800">
            변경된 내용을 다시 불러와 화면에 반영합니다.
          </div>
          <div className="flex justify-end">
            <Button
              type="button"
              onClick={() => {
                setUpdateCompleteOpen(false);
                navigate(0);
              }}
            >
              확인
            </Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
