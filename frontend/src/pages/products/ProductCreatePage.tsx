import { zodResolver } from '@hookform/resolvers/zod';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  deleteFileAttachment,
  getFileAttachments,
  uploadFileAttachment
} from '@/entities/file/api/fileAttachmentApi';
import { useForm } from 'react-hook-form';
import { getCategories } from '@/entities/category/api/categoryApi';
import type { Category } from '@/entities/category/model/types';
import {
  createProduct,
  generateProductDescription,
  getProduct,
  updateProduct,
  updateProductStatus
} from '@/entities/product/api/productApi';
import { getProductOptions } from '@/entities/productOption/api/productOptionApi';
import type { Product } from '@/entities/product/model/types';
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

export function ProductCreatePage() {
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [draftProduct, setDraftProduct] = useState<Product | null>(null);
  const [categoryModalOpen, setCategoryModalOpen] = useState(false);
  const [categorySearch, setCategorySearch] = useState('');
  const [categoryParentId, setCategoryParentId] = useState<string | null>(null);
  const [pendingImages, setPendingImages] = useState<PendingImage[]>([]);
  const [attachmentNotice, setAttachmentNotice] = useState<string | null>(null);
  const [previewIndex, setPreviewIndex] = useState(0);
  const [submitConfirmOpen, setSubmitConfirmOpen] = useState(false);
  const form = useForm<ProductFormValues>({
    resolver: zodResolver(productSchema),
    defaultValues: { categoryId: '', name: '', description: '', selectedOptionItemIds: [] }
  });
  const selectedCategoryId = form.watch('categoryId');
  const descriptionValue = form.watch('description') ?? '';
  const selectedOptionItemIds = form.watch('selectedOptionItemIds') ?? [];

  const categoriesQuery = useQuery({
    queryKey: ['categories', 'active-options'],
    queryFn: () => getCategories({ status: 'ACTIVE', size: 100 })
  });
  const optionPreviewQuery = useQuery({
    queryKey: ['product-options', selectedCategoryId, 'preview'],
    queryFn: () => getProductOptions(selectedCategoryId, 'ACTIVE'),
    enabled: Boolean(selectedCategoryId)
  });
  const attachmentsQuery = useQuery({
    queryKey: ['file-attachments', 'PRODUCT', draftProduct?.id],
    queryFn: () => getFileAttachments('PRODUCT', draftProduct?.id ?? ''),
    enabled: Boolean(draftProduct?.id)
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
    return () => {
      pendingPreviewUrls.forEach((item) => URL.revokeObjectURL(item.url));
    };
  }, [pendingPreviewUrls]);

  useEffect(() => {
    if (previewIndex > Math.max(0, imagePreviewItems.length - 1)) {
      setPreviewIndex(Math.max(0, imagePreviewItems.length - 1));
    }
  }, [imagePreviewItems.length, previewIndex]);

  useEffect(() => {
    form.setValue('selectedOptionItemIds', [], { shouldDirty: true });
  }, [form, selectedCategoryId]);

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

  const upsertProductMutation = useMutation({
    mutationFn: async (values: ProductFormValues) => {
      const payload = {
        ...values,
        description: values.description?.trim() ?? '',
        selectedOptionItemIds: values.selectedOptionItemIds,
        actorId: requireActorId()
      };

      const product = draftProduct
        ? await updateProduct(draftProduct.id, payload)
        : await createProduct(payload);

      await registerPendingImages(product.id);
      return await getProduct(product.id);
    },
    onSuccess: async (product) => {
      setDraftProduct(product);
      form.setValue('description', product.description ?? '');
      setAttachmentNotice(null);
      await queryClient.invalidateQueries({ queryKey: ['products'] });
      await queryClient.invalidateQueries({ queryKey: ['products', product.id] });
      await queryClient.invalidateQueries({ queryKey: ['file-attachments', 'PRODUCT', product.id] });
    }
  });

  const deleteAttachmentMutation = useMutation({
    mutationFn: (fileAttachmentId: string) => deleteFileAttachment(fileAttachmentId),
    onSuccess: async () => {
      if (draftProduct?.id) {
        await queryClient.invalidateQueries({ queryKey: ['file-attachments', 'PRODUCT', draftProduct.id] });
      }
    }
  });

  const submitProductMutation = useMutation({
    mutationFn: async () => {
      const valid = await form.trigger();
      if (!valid) {
        throw new Error('상품 기본 정보를 먼저 확인해 주세요.');
      }

      const product = await upsertProductMutation.mutateAsync(form.getValues());
      return updateProductStatus(product.id, {
        status: 'PENDING',
        actorId: requireActorId()
      });
    },
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ['products'] });
      setAttachmentNotice('승인 요청이 완료되었습니다.');
      setSubmitConfirmOpen(false);
      navigate('/products');
    }
  });

  async function registerPendingImages(productId: string) {
    if (pendingImages.length === 0) {
      return;
    }

    const nextSortOrder = uploadedAttachments.length + 1;
    const results = await Promise.allSettled(
      pendingImages.map((entry, index) =>
        uploadFileAttachment({
          targetType: 'PRODUCT',
          targetId: productId,
          sortOrder: nextSortOrder + index,
          file: entry.file
        })
      )
    );

    const failedCount = results.filter((result) => result.status === 'rejected').length;
    setPendingImages([]);

    if (failedCount > 0) {
      setAttachmentNotice(
        `상품은 저장되었지만 이미지 ${failedCount}건 업로드에 실패했습니다. 이미지 섹션에서 다시 추가해 주세요.`
      );
      return;
    }

    setAttachmentNotice(`이미지 ${results.length}건을 등록했습니다.`);
  }

  async function generateDescription() {
    const valid = await form.trigger(['categoryId', 'name', 'description']);
    if (!valid) {
      return;
    }
    await generateDescriptionMutation.mutateAsync();
  }

  const generateDescriptionMutation = useMutation({
    mutationFn: async () => {
      const keywordText = descriptionValue.trim();
      if (!keywordText) {
        throw new Error('키워드를 입력해 주세요.');
      }

      const values = form.getValues();
      const product = draftProduct
        ? draftProduct
        : await createProduct({
            ...values,
            description: values.description?.trim() ?? '',
            selectedOptionItemIds: values.selectedOptionItemIds,
            actorId: requireActorId()
          });

      if (!draftProduct) {
        await registerPendingImages(product.id);
      }

      return generateProductDescription(product.id, {
        actorId: requireActorId(),
        productName: values.name,
        categoryName: selectedCategory?.name ?? '',
        optionSummary: '',
        featureKeywords: keywordText
          .split(/[\n,]/)
          .map((keyword) => keyword.trim())
          .filter(Boolean)
      });
    },
    onSuccess: async (product) => {
      setDraftProduct(product);
      form.setValue('description', product.description ?? '', { shouldDirty: true, shouldValidate: true });
      setAttachmentNotice('AI 문구를 설명란에 반영했습니다.');
      await queryClient.invalidateQueries({ queryKey: ['products'] });
      await queryClient.invalidateQueries({ queryKey: ['products', product.id] });
      await queryClient.invalidateQueries({ queryKey: ['file-attachments', 'PRODUCT', product.id] });
    }
  });

  function appendImages(files: File[]) {
    setPendingImages((current) => [
      ...current,
      ...files.map((file, index) => ({
        id: `${file.name}-${file.size}-${Date.now()}-${index}`,
        file
      }))
    ]);
  }

  return (
    <div className="space-y-6">
      <section className="space-y-2">
        <div className="page-header">
          <div>
            <p className="section-kicker">New Product</p>
            <h2 className="page-title">상품 생성</h2>
          </div>
          <Link
            className="inline-flex h-11 items-center justify-center rounded-xl border border-slate-200 bg-white/90 px-4 text-sm font-semibold text-slate-800 shadow-sm transition hover:-translate-y-0.5 hover:border-slate-300 hover:bg-white hover:shadow-md"
            to="/products"
          >
            상품 목록으로
          </Link>
        </div>
      </section>

      <section className="surface-card p-6">
        <form
          className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_340px]"
          onSubmit={form.handleSubmit(() => submitProductMutation.mutate())}
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
                  <span className="block text-xs font-medium text-red-600">
                    {form.formState.errors.categoryId.message}
                  </span>
                ) : null}
              </div>

              <TextField
                label="상품명"
                error={form.formState.errors.name?.message}
                {...form.register('name')}
              />
            </div>

            {selectedCategoryId ? (
              <div className="space-y-3 rounded-xl border border-border bg-slate-50 p-4">
                <div className="flex items-center justify-between gap-3">
                  <div>
                    <p className="text-sm font-medium text-ink">옵션 선택</p>
                    <p className="mt-1 text-xs text-slate-500">
                      선택한 카테고리에 연결된 선택 항목을 고를 수 있습니다.
                    </p>
                  </div>
                  <Link
                    className="text-xs font-medium text-slate-900 underline"
                    to={`/product-options?categoryId=${selectedCategoryId}`}
                  >
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
                    upsertProductMutation.isPending ||
                    generateDescriptionMutation.isPending ||
                    !selectedCategoryId ||
                    !form.watch('name').trim() ||
                    !descriptionValue.trim()
                  }
                  type="button"
                  variant="secondary"
                  onClick={() => void generateDescription()}
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
                  <p className="mt-1 text-xs text-slate-500">
                    여러 장을 순서대로 추가할 수 있고, 첫 번째 이미지가 대표 이미지가 됩니다.
                  </p>
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
                              setPreviewIndex((current) =>
                                current === 0 ? imagePreviewItems.length - 1 : current - 1
                              )
                            }
                          >
                            ‹
                          </button>
                          <button
                            className="absolute right-3 top-1/2 flex h-9 w-9 -translate-y-1/2 items-center justify-center rounded-full bg-white/90 text-lg text-ink shadow"
                            type="button"
                            onClick={() =>
                              setPreviewIndex((current) =>
                                current === imagePreviewItems.length - 1 ? 0 : current + 1
                              )
                            }
                          >
                            ›
                          </button>
                        </>
                      ) : null}
                    </div>
                    <div className="flex items-center justify-between border-t border-border px-4 py-3 text-sm">
                      <p className="truncate font-medium text-ink">
                        {previewIndex === 0 ? '대표 이미지' : `이미지 ${previewIndex + 1}`} ·{' '}
                        {imagePreviewItems[previewIndex]?.name}
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
                    <div
                      key={attachment.id}
                      className="flex items-center justify-between gap-4 rounded-lg border border-border bg-white px-4 py-3"
                    >
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
                    <div
                      key={entry.id}
                      className="flex items-center justify-between gap-4 rounded-lg border border-dashed border-border bg-white px-4 py-3"
                    >
                      <div>
                        <p className="text-sm font-medium text-ink">
                          {uploadedAttachments.length === 0 && index === 0 ? '대표 예정 이미지' : '업로드 예정 이미지'} ·{' '}
                          {entry.file.name}
                        </p>
                        <p className="mt-1 text-xs text-slate-500">
                          {Math.max(1, Math.round(entry.file.size / 1024))}KB · 저장 전
                        </p>
                      </div>
                      <Button
                        type="button"
                        variant="ghost"
                        onClick={() =>
                          setPendingImages((current) => current.filter((item) => item.id !== entry.id))
                        }
                      >
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
                <p>현재 상태: {draftProduct ? getProductStatusLabel(draftProduct.status) : '등록 전'}</p>
                <p>등록된 이미지: {uploadedAttachments.length}건</p>
                <p>업로드 예정 이미지: {pendingImages.length}건</p>
              </div>
            </div>

            <ErrorMessage
              error={
                upsertProductMutation.error ??
                submitProductMutation.error ??
                categoriesQuery.error ??
                optionPreviewQuery.error ??
                attachmentsQuery.error ??
                deleteAttachmentMutation.error ??
                generateDescriptionMutation.error
              }
            />
            {attachmentNotice ? <p className="text-xs font-medium text-slate-600">{attachmentNotice}</p> : null}
            <Button
              className="w-full"
              disabled={submitProductMutation.isPending || upsertProductMutation.isPending}
              type="button"
              onClick={async () => {
                const valid = await form.trigger();
                if (!valid) {
                  return;
                }
                setSubmitConfirmOpen(true);
              }}
            >
              승인 요청
            </Button>
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
        open={submitConfirmOpen}
        title="승인 요청 확인"
        description="현재 입력한 상품 정보를 승인 요청 리스트로 올리시겠습니까?"
        onClose={() => setSubmitConfirmOpen(false)}
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
          </div>
          <div className="flex justify-end gap-3">
            <Button type="button" variant="secondary" onClick={() => setSubmitConfirmOpen(false)}>
              취소
            </Button>
            <Button
              type="button"
              disabled={submitProductMutation.isPending}
              onClick={() => submitProductMutation.mutate()}
            >
              확인
            </Button>
          </div>
        </div>
      </Modal>

    </div>
  );
}
